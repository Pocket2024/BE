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
import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRepository;
import Project.Pocket.user.entity.UserRoleEnum;
import Project.Pocket.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
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
        @Value("${aws.s3.bucket-name}")
        private  String bucketName;
        @Value("${aws.s3.region}")
        private  String region;
        private final S3Client s3Client;

    // 생성자 주입
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider,RedisDao redisDao, FollowRepository followRepository,TicketCategoryRepository ticketCategoryRepository,
                       ReviewRepository reviewRepository, ReviewService reviewService,  @Value("${aws.s3.bucket-name}")String bucketName,  @Value("${aws.s3.region}")String region,@Value("${aws.accessKeyId}") String accessKeyId,
                       @Value("${aws.secretAccessKey}") String secretAccessKey){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.redisDao = redisDao;
        this.followRepository = followRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
        this.bucketName = bucketName;
        this.region = region;
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }









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
                // 기본 프로필 이미지 설정 (S3 URL로)
                String defaultProfileImageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, "default.png");
                user.setProfileImage(defaultProfileImageUrl);


                userRepository.save(user);
                UserDto userDto = user.toDto();
                return ResponseEntity.ok(userDto);
            }


     // 로그인 반환값으로 user를 userResponseDto 담아 반환하고  컨트롤러에서 반환된 객체를 이용하여 토큰 발행한다.

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
    //로그인한 회원 조회
    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return null;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();


    }

    //파일 저장 메서드
//    public String saveProfileImage(MultipartFile file, HttpServletRequest request) throws IOException{
//        if(file.isEmpty()){
//            throw new IllegalArgumentException("Cannot upload an empty file");
//        }
//
//        //파일 이름과 경로 설정
//        String fileName = file.getOriginalFilename();
//        Path filePath = Paths.get("src/main/resources/static/images", fileName);
//
//        //파일 저장
//        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
//        return baseUrl + "/images/" + fileName;
//    }


    public String saveProfileImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        // 파일 이름 생성 및 S3에서의 키 설정
        String fileName = file.getOriginalFilename();
        String contentType = getContentType(file.getOriginalFilename());
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String key = "profiles/" + fileName; // S3 버킷 내의 저장 경로

        // S3에 파일 저장
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3", e);
        }

        // S3 URL 생성
        String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
        return s3Url;
    }








//    //회원 정보 수정
    public UserDto updateUser(Long userId, UserUpdateRequest request, HttpServletRequest httprequest) throws UserNotFoundException,IOException {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id "+ userId));


        if (request.getNickName() != null) {
            user.setNickname(request.getNickName());
        }
        if(request.getBio() != null){
            user.setBio(request.getBio());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }


        if(request.getPhoneNumber() != null){
            user.setPhoneNumber(request.getPhoneNumber());
        }


        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String profileImageUrl = saveProfileImage(request.getProfileImage());
            user.setProfileImage(profileImageUrl);
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

    // 파일 이름에 따라 Content-Type 반환
    private String getContentType(String fileName) {
        Map<String, String> extensionToContentType = new HashMap<>();
        extensionToContentType.put("jpg", "image/jpeg");
        extensionToContentType.put("jpeg", "image/jpeg");
        extensionToContentType.put("png", "image/png");

        String fileExtension = getFileExtension(fileName).toLowerCase();
        return extensionToContentType.getOrDefault(fileExtension, "application/octet-stream");
    }

    // 파일 이름에서 확장자 추출
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }


}
