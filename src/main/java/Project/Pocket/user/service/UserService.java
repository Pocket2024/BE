package Project.Pocket.user.service;


import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.entity.ReviewRepository;
import Project.Pocket.Review.service.ReviewService;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.TicketCategory.entity.TicketCategoryRepository;
import Project.Pocket.follow.entity.FollowRepository;
import Project.Pocket.redis.CacheNames;
import Project.Pocket.redis.RedisDao;
import Project.Pocket.security.exception.CustomException;
import Project.Pocket.security.exception.ExceptionStatus;
import Project.Pocket.security.jwt.JwtProvider;
import Project.Pocket.security.service.UserDetailsImpl;
import Project.Pocket.user.dto.*;
//import Project.Pocket.user.dto.UserUpdateRequest;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRepository;
import Project.Pocket.user.entity.UserRoleEnum;
//import Project.Pocket.user.exception.UserNotFoundException;
import Project.Pocket.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisDao redisDao;
    private final FollowRepository followRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;






    @Transactional
    public ResponseEntity signup(@Validated  SignUpRequest signUpRequest, HttpServletRequest request) {
        String email = signUpRequest.getEmail();
        String nickname = signUpRequest.getNickName();
        String password = passwordEncoder.encode(signUpRequest.getPassword());


        //닉네임 중복 확인
        Optional<User> findNickname = userRepository.findByNickname(nickname);
        if (findNickname.isPresent()) {
            throw new CustomException(ExceptionStatus.DUPLICATED_NICKNAME);
        }
        // 이메일 중복 확인
        Optional<User> findEmail = userRepository.findByEmail(email);
        if (findEmail.isPresent()) {
            throw new CustomException(ExceptionStatus.DUPLICATED_EMAIL);
        }
        UserRoleEnum role = UserRoleEnum.MEMBER;
        User user = signUpRequest.toEntity(role, password);
        // 기본 프로필 이미지 설정
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        String defaultProfileImageUrl = baseUrl + "/images/default.png";
        user.setProfileImage(defaultProfileImageUrl);

        userRepository.save(user);
        UserDto userDto = user.toDto();
        return ResponseEntity.ok(userDto);
    }

    /**
     * 로그인 반환값으로 user를 userResponseDto 담아 반환하고  컨트롤러에서 반환된 객체를 이용하여 토큰 발행한다.
     */
    @Cacheable(cacheNames = CacheNames.LOGINUSER, key = "'login'+ #p0.getEmail()", unless = "#result== null")
    @Transactional
    public UserResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ExceptionStatus.WRONG_EMAIL)
        );
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ExceptionStatus.WRONG_PASSWORD);
        }
        return new UserResponse().of(user);// user객체를 dto에 담아서 반환
    }
    @CacheEvict(cacheNames = CacheNames.USERBYUSERNAME, key = "'login'+#p1")
    @Transactional
    public ResponseEntity logout(String accessToken, String email) {
        // 레디스에 accessToken 사용못하도록 등록
        Long expiration = jwtProvider.getExpiration(accessToken);
        redisDao.setBlackList(accessToken, "logout", expiration);
        if (redisDao.hasKey(email)) {
            redisDao.deleteRefreshToken(email);
        } else {
            throw new IllegalArgumentException("이미 로그아웃한 유저입니다.");
        }
        return ResponseEntity.ok("로그아웃 완료");
    }
    //로그인한 회원 정보 조회
    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return null;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();


    }

    //파일 저장 메서드
    public String saveProfileImage(MultipartFile file, HttpServletRequest request) throws IOException{
        if(file.isEmpty()){
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        //파일 이름과 경로 설정
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get("src/main/resources/static/images", fileName);

        //파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        return baseUrl + "/images/" + fileName;
    }

//    //회원 정보 수정
    public UserDto updateUser(Long userId, UserUpdateRequest request, HttpServletRequest httprequest) throws UserNotFoundException,IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id "+ userId));


        if (request.getNickName() != null) {
            user.setNickname(request.getNickName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }


        if(request.getPhoneNumber() != null){
            user.setPhoneNumber(request.getPhoneNumber());
        }


        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {

            String profileImagePath = saveProfileImage(request.getProfileImage(),httprequest );
            user.setProfileImage(profileImagePath);
     }
        //수정된 정보 저장
        userRepository.save(user);
        updateSecurityContext(user);
        return user.toDto();
    }
    private void updateSecurityContext(User updatedUser){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            userDetails.setUser(updatedUser);

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                authentication.getCredentials(),
                authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

    }
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
    public UserDto getUserDetails(Long userId){
        User user = getUserById(userId);
        UserDto userDto = user.toDto();
        //팔로워 + 팔로잉 수 추가 설정
        int followersCount = followRepository.countByFollowingId(user.getId());
        int followingsCount = followRepository.countByFollowerId(user.getId());
        userDto.setFollowersCount(followersCount);
        userDto.setFollowingsCount(followingsCount);
        //티켓 카테고리(Pocket) 수 추가
        int ticketCategoryCount = ticketCategoryRepository.countByUserId(user.getId());
        userDto.setTicketCategoryCount(ticketCategoryCount);
        //리뷰(Ticket) 수 추가
        int reviewCount = reviewRepository.countByUserId(user.getId());
        userDto.setReviewCount(reviewCount);
        //대표 리뷰(Ticket) 설정
        reviewRepository.findByUserIdAndIsFeaturedTrue(userId).ifPresent(featuredReview -> {
            Long featuredReviewId = featuredReview.getId();
            userDto.setFeaturedReviewId(featuredReviewId);
        });
        //팔로잉 여부 확인
        User currentUser = getCurrentUser();
        boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, user);
        userDto.setFollowedByCurrentUser(isFollowing);


        return userDto;

    }
}
