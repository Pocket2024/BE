package Project.Pocket.user.service;


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
    @Value("${profile.images.dir}")
    private String profileImagesDir;
    @Value("${default.profile.image}")
    private String defaultProfileImage;






    @Transactional
    public ResponseEntity signup(@Validated  SignUpRequest signUpRequest) {
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
        user.setProfileImage(defaultProfileImage);

        userRepository.save(user);
        return ResponseEntity.ok("회원가입 성공");
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
    public String saveProfileImage(MultipartFile file) throws IOException{
        if(file.isEmpty()){
            throw new IllegalArgumentException("Cannot upload an empty file");
        }
        //파일 이름과 경로 설정
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(profileImagesDir, fileName);

        //파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

//    //회원 정보 수정
    public void updateUser(Long userId, UserUpdateRequest request) throws UserNotFoundException,IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id "+ userId));


        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if(request.getPhoneNumber() != null){
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String profileImagePath = saveProfileImage(request.getProfileImage());
            user.setProfileImage(profileImagePath);
     }
        //수정된 정보 저장
        userRepository.save(user);

        //User updatedUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        updateSecurityContext(user);
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
        return user.toDto();
    }
}
