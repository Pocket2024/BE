package Project.Pocket.Review.service;

import Project.Pocket.CustomImage.entity.CustomImage;
import Project.Pocket.CustomImage.entity.CustomImageRepository;
import Project.Pocket.Image.entity.Image;
import Project.Pocket.Image.entity.ImageDto;
import Project.Pocket.Image.entity.ImageRepository;
import Project.Pocket.Image.service.ImageService;
import Project.Pocket.Like.entity.Like;
import Project.Pocket.Like.entity.LikeRepository;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.dto.ReviewRequest;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.entity.ReviewRepository;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.TicketCategory.entity.TicketCategoryRepository;
import Project.Pocket.TicketCategory.service.TicketCategoryService;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRepository;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final TicketCategoryService ticketCategoryService;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final CustomImageRepository customImageRepository;
    // 현재 작업 디렉토리에서 'static/images' 디렉토리의 절대 경로를 설정
    private final Path imageDirectory;
    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    // S3 클라이언트 및 버킷 정보
    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.s3.region}")
    private String region;


    @Autowired
    public ReviewService(@Lazy TicketCategoryService ticketCategoryService, ReviewRepository reviewRepository, ImageRepository imageRepository, @Lazy UserService userService, LikeRepository likeRepository, CustomImageRepository customImageRepository, TicketCategoryRepository ticketCategoryRepository, UserRepository userRepository,
                         @Value("${aws.s3.bucket-name}") String bucketName, @Value("${aws.s3.region}") String region,
                         @Value("${aws.accessKeyId}") String accessKeyId, @Value("${aws.secretAccessKey}") String secretAccessKey) {

        this.ticketCategoryService = ticketCategoryService;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.likeRepository = likeRepository;
        this.customImageRepository = customImageRepository;
        this.imageDirectory = Paths.get("src", "main", "resources", "static", "images").toAbsolutePath();
        this.userRepository = userRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.bucketName = bucketName;
        this.region = region;
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }


    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return ""; // 파일이 없을 경우 빈 문자열 반환
        }

        // 파일 이름 생성
        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        String contentType = getContentType(imageFile.getOriginalFilename());

        try {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));

            // 파일이 업로드된 S3 URL 반환
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        } catch (S3Exception e) {
            throw new IOException("Failed to upload image to S3", e);
        }
    }


    public void deleteImageFile(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return; // 이미지 URL이 없으면 아무 것도 하지 않음
        }

        // URL에서 파일 이름 추출
        String fileName = extractFileNameFromUrl(imageUrl);

        if (fileName == null || fileName.trim().isEmpty()) {
            return; // 파일 이름이 유효하지 않으면 아무 것도 하지 않음
        }

        try {
            // S3에서 파일 삭제
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new IOException("Failed to delete image from S3", e);
        }
    }


    // URL에서 파일 이름을 추출하는 메서드
    private String extractFileNameFromUrl(String imageUrl) {
        // URL을 디코딩하고 '/'로 분리
        try {
            String decodedUrl = java.net.URLDecoder.decode(imageUrl, "UTF-8");
            return decodedUrl.substring(decodedUrl.lastIndexOf("/") + 1);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to decode URL", e);
        }
    }


    @Transactional
    public Review createReview(ReviewRequest reviewRequest, MultipartFile customImageFile) {
        // 사용자 ID와 카테고리 ID가 올바르게 전달되는지 확인
        TicketCategory ticketCategory = ticketCategoryService.getTicketCategoryById(reviewRequest.getTicketCategoryId());
        if (ticketCategory == null) {
            throw new IllegalArgumentException("Ticket category with id " + reviewRequest.getTicketCategoryId() + " not found!");
        }

        User user = userService.getCurrentUser();
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }

        // 새로운 리뷰 객체 생성 및 초기화
        Review review = new Review();
        review.setTicketCategory(ticketCategory);
        review.setContent(reviewRequest.getContent());
        review.setLocation(reviewRequest.getLocation());
        review.setDate(reviewRequest.getDate());
        review.setTitle(reviewRequest.getTitle());
        review.setSeat(reviewRequest.getSeat());
        review.setPrivate(reviewRequest.isPrivate());
        review.setOcr(reviewRequest.isOcr());
        review.setUser(user);

        // 리뷰를 먼저 저장하여 reviewId를 확보
        review = reviewRepository.save(review);

        // 이미지 파일들 저장 및 리뷰와 매칭
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : reviewRequest.getImages()) {
            try {
                String imageUrl = saveImage(file);
                Image image = new Image();
                image.setReview(review); // 이미지와 리뷰를 매칭
                image.setUrl(imageUrl);
                image = imageRepository.save(image); // 이미지 저장
                images.add(image);
            } catch (IOException e) {
                System.err.println("Failed to save image: " + e.getMessage());
            }
        }
        review.setImages(images); // 리뷰에 이미지 리스트 설정

        // 커스텀 이미지 저장 및 리뷰와 매칭
        if (customImageFile != null && !customImageFile.isEmpty()) {
            try {
                String customImageUrl = saveImage(customImageFile);
                CustomImage customImage = new CustomImage();
                customImage.setCustomImageUrl(customImageUrl);
                customImage = customImageRepository.save(customImage); // 커스텀 이미지 저장
                review.setCustomImage(customImage); // 리뷰에 커스텀 이미지 설정
            } catch (IOException e) {
                System.err.println("Failed to save custom image: " + e.getMessage());
            }
        }

        return reviewRepository.save(review); // 리뷰를 다시 저장하여 연관 관계 확정
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 리뷰와 사용자 확인
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found or user not authorized to delete it"));

        // 사용자와의 연관관계 끊기
        User user = review.getUser();
        if (user != null) {
            user.getReviews().remove(review); // 사용자의 리뷰 리스트에서 제거
            userRepository.save(user); // 변경사항 저장
        }

        // 티켓 카테고리와의 연관관계 끊기
        TicketCategory ticketCategory = review.getTicketCategory();
        if (ticketCategory != null) {
            ticketCategory.getReviews().remove(review); // 티켓 카테고리의 리뷰 리스트에서 제거
            ticketCategoryRepository.save(ticketCategory); // 변경사항 저장
        }

        // 연관된 이미지 삭제
        List<Image> images = review.getImages();
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                try {
                    deleteImageFile(image.getUrl()); // URL을 기반으로 파일 삭제
                    imageRepository.delete(image); // DB에서 이미지 삭제
                } catch (IOException e) {
                    System.err.println("Failed to delete image file: " + e.getMessage());
                }
            }
            review.getImages().clear(); // 리스트에서 이미지 제거
        } else {
            System.out.println("No images found to delete for review ID: " + reviewId);
        }

        // 좋아요 삭제
        Set<Like> likes = review.getLikes();
        if (likes != null && !likes.isEmpty()) {
            for (Like like : likes) {
                likeRepository.delete(like); // DB에서 좋아요 삭제
            }
            review.getLikes().clear(); // 리스트에서 좋아요 제거
        } else {
            System.out.println("No likes found to delete for review ID: " + reviewId);
        }

        // 커스텀 이미지 삭제
        CustomImage customImage = review.getCustomImage();
        if (customImage != null) {
            System.out.println("Attempting to delete custom image: " + customImage.getId() + ", URL: " + customImage.getCustomImageUrl());
            try {
                deleteImageFile(customImage.getCustomImageUrl()); // URL을 기반으로 파일 삭제
                customImageRepository.delete(customImage); // DB에서 커스텀 이미지 삭제
                System.out.println("Successfully deleted custom image: " + customImage.getId());
            } catch (IOException e) {
                System.err.println("Failed to delete custom image file: " + e.getMessage());
            }
            review.setCustomImage(null); // 리뷰와 커스텀 이미지의 연관 관계를 끊음
        } else {
            System.out.println("No custom image found to delete for review ID: " + reviewId);
        }

        // 리뷰 삭제
        reviewRepository.delete(review);

        // 엔티티 매니저 플러시
        entityManager.flush(); // 데이터베이스에서 연관된 모든 작업을 즉시 반영
    }


    @Transactional(readOnly = true)
    public int getReviewLikesCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review with Id " + reviewId + " not found!"));
        return review.getLikeCount();
    }

    @Transactional(readOnly = true)
    public List<User> getReviewLikedUsers(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review with id " + reviewId + " not found!"));
        return review.getLikedUsers();
    }


    public static ReviewDto mapToDto(Review review, TicketCategoryDto ticketCategoryDto, List<ImageDto> imageDtos, boolean likedByCurrentUser) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setId(review.getId());
        reviewDto.setContent(review.getContent());
        reviewDto.setLikes(review.getLikeCount());
        reviewDto.setTicketcategory(ticketCategoryDto);
        reviewDto.setImages(imageDtos);
        reviewDto.setLikedByCurrentUser(likedByCurrentUser);
        reviewDto.setDate(review.getDate());
        reviewDto.setSeat(review.getSeat());
        reviewDto.setTitle(review.getTitle());
        reviewDto.setLocation(review.getLocation());
        if (review.getCustomImage() != null) {
            reviewDto.setCustomImageUrl(review.getCustomImage().getCustomImageUrl());
        }
        reviewDto.setPrivate(review.isPrivate());
        reviewDto.setOcr(review.isOcr());

        return reviewDto;
    }


    public ReviewDto getReviewDto(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        //비공개 리뷰인 경우 작성자만 조회 가능
        if (review.isPrivate() && !review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("No permission to view this review");
        }
        User author = review.getUser();
        String authorNickname = author.getNickname();
        String authorProfileImageUrl = author.getProfileImage();
        Long authorId = author.getId();
        //TicketCategory -> DTO
        TicketCategoryDto ticketCategoryDto = ticketCategoryService.getTicketCategoryDtoById(review.getTicketCategory().getId());
        //이미지 -> DTO
        List<ImageDto> imageDtos = review.getImages().stream()
                .map(ImageService::mapToDto)
                .collect(Collectors.toList());

        boolean likedByCurrentUser = likeRepository.existsByReviewIdAndUserId(reviewId, userId);
        //review -> DTO
        ReviewDto reviewDto = mapToDto(review, ticketCategoryDto, imageDtos, likedByCurrentUser);
        //DTO에 작성자 닉네임, 프로필이미지 추가 설정
        reviewDto.setAuthorNickname(authorNickname);
        reviewDto.setAuthorProfileImageUrl(authorProfileImageUrl);
        reviewDto.setAuthorId(authorId);

        return reviewDto;

    }

    public List<ReviewDto> getReviewsByCategory(Long categoryId, Long userId) {
        List<Review> reviews = reviewRepository.findByTicketCategoryId(categoryId);
        List<Review> filteredReviews = reviews.stream().filter(review ->
                !review.isPrivate() || review.getUser().getId().equals(userId)).collect(Collectors.toList());
        return filteredReviews.stream().map(review -> getReviewDto(review.getId(), userId)).collect(Collectors.toList());
    }

    public void setFeaturedReview(Long userId, Long reviewId) {
        //대표 리뷰가 이미 있다면 해제
        reviewRepository.findByUserIdAndIsFeaturedTrue(userId).ifPresent(existingFeaturedReview -> {
            existingFeaturedReview.setFeatured(false);
            reviewRepository.save(existingFeaturedReview);
        });

        //새로운 리뷰 대표리뷰 설정
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setFeatured(true);
        reviewRepository.save(review);
    }


    public List<ReviewDto> searchReviews(String keyword, String searchType) {
        Long currentUserId = userService.getCurrentUser().getId();
        List<Review> reviews;

        if ("date".equalsIgnoreCase(searchType)) {
            // 날짜 형식에 맞게 keyword를 LocalDate로 변환
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            LocalDate date = LocalDate.parse(keyword, formatter);

            // 해당 날짜에 맞는 시작일과 종료일 설정
            LocalDate startDate = date;
            LocalDate endDate = date;

            // 특정 날짜에 해당하는 리뷰 검색
            reviews = reviewRepository.searchByField(null, searchType, startDate, endDate);
        } else {
            // 다른 필드에 대한 검색
            reviews = reviewRepository.searchByField(keyword, searchType, null, null);
        }

        return reviews.stream()
                .map(review -> getReviewDto(review.getId(), currentUserId))
                .collect(Collectors.toList());
    }

    //최신순으로 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByLatest() {
        Long currentUserId = userService.getCurrentUser().getId();
        List<Review> reviews = reviewRepository.findAllByIsPrivateFalseOrderByCreatedAtDesc();
        return reviews.stream().map(review -> getReviewDto(review.getId(), currentUserId)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByLikeCount() {
        Long currentUserId = userService.getCurrentUser().getId();
        List<Review> reviews = reviewRepository.findReviewsByLikeCountAndIsPrivateFalse();
        return reviews.stream().map(review -> getReviewDto(review.getId(), currentUserId)).collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsSortedByDate() {
        Long currentUserId = userService.getCurrentUser().getId();
        List<Review> reviews = reviewRepository.findAllByOrderByDateDesc();
        return reviews.stream().map(review -> getReviewDto(review.getId(), currentUserId)).collect(Collectors.toList());
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

    public List<LocalDate> getAllReviewDates(Long userId) {

        return reviewRepository.findReviewDatesByUserId(userId);
    }

    public List<ReviewDto> getReviewsByDate(Long userId, LocalDate date) {
        List<Review> reviews = reviewRepository.findByUserId(userId);

        // 주어진 날짜에 해당하는 리뷰만 필터링
       List<ReviewDto> reviewsByDate = reviews.stream()
               .filter(review -> review.getDate().equals(date))
               .map(review -> getReviewDto(review.getId(), userId))
               .collect(Collectors.toList());

        return reviewsByDate;
    }
    public List<ReviewDto> getLikedReviewsByUserId(Long userId){
        List<Like> likedReviews = likeRepository.findByUserId(userId);
        return likedReviews.stream().map(like -> getReviewDto(like.getReview().getId(), userId)).collect(Collectors.toList());
    }


}
