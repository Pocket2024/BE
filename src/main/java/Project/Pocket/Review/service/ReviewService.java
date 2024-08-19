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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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




    @Autowired
    public ReviewService(@Lazy TicketCategoryService ticketCategoryService, ReviewRepository reviewRepository, ImageRepository imageRepository, @Lazy UserService userService, LikeRepository likeRepository, CustomImageRepository customImageRepository,TicketCategoryRepository ticketCategoryRepository, UserRepository userRepository){
        this.ticketCategoryService = ticketCategoryService;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.likeRepository = likeRepository;
        this.customImageRepository = customImageRepository;
        this.imageDirectory = Paths.get("src", "main", "resources", "static", "images").toAbsolutePath();
        this.userRepository = userRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
    }

//    public String saveImage(MultipartFile imageFile) throws IOException{
//        if (imageFile == null || imageFile.isEmpty()) {
//            return ""; //
//        }
//        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
//        Path imagePath = Paths.get("src/main/resources/static/images" + fileName);
//        Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
//        String baseUrl = "http://localhost:8080";
//        return baseUrl + "/images/" + fileName;
//    }
public String saveImage(MultipartFile imageFile) throws IOException {
    if (imageFile == null || imageFile.isEmpty()) {
        return ""; // 파일이 없을 경우 빈 문자열 반환
    }

    // 파일 이름 생성
    String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

    // 전체 파일 경로 설정
    Path imagePath = imageDirectory.resolve(fileName);

    // 파일 저장
    Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

    // 반환할 URL 생성
    String baseUrl = "http://localhost:8080";
    return baseUrl + "/images/" + fileName;
}



    public void deleteImageFile(String imageUrl) throws IOException {
        // URL에서 파일 이름 추출
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        // 전체 파일 경로 설정
        Path imagePath = imageDirectory.resolve(fileName);


        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
            System.out.println("File deleted successfully.");
        } else {
            System.err.println("Image not found at path: " + imagePath.toString());
            throw new IllegalArgumentException("Image not found: " + fileName);
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
    public int getReviewLikesCount(Long reviewId){
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review with Id " + reviewId + " not found!"));
        return review.getLikeCount();
    }

    @Transactional(readOnly = true)
    public List<User> getReviewLikedUsers(Long reviewId){
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review with id " + reviewId + " not found!"));
        return review.getLikedUsers();
    }


   public static ReviewDto mapToDto(Review review, TicketCategoryDto ticketCategoryDto, List<ImageDto> imageDtos, boolean likedByCurrentUser){
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
        if(review.getCustomImage() != null){
            reviewDto.setCustomImageUrl(review.getCustomImage().getCustomImageUrl());
        }

        return reviewDto;
   }


   public ReviewDto getReviewDto(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        User author =review.getUser();
        String authorNickname = author.getNickname();
        String authorProfileImageUrl = author.getProfileImage();
        //TicketCategory -> DTO
        TicketCategoryDto ticketCategoryDto = ticketCategoryService.getTicketCategoryDtoById(review.getTicketCategory().getId());
       //이미지 -> DTO
        List<ImageDto> imageDtos = review.getImages().stream()
               .map(ImageService::mapToDto)
               .collect(Collectors.toList());

        boolean likedByCurrentUser = likeRepository.existsByReviewIdAndUserId(reviewId, userId);
        //review -> DTO
        ReviewDto reviewDto =  mapToDto(review, ticketCategoryDto, imageDtos, likedByCurrentUser);
        //DTO에 작성자 닉네임, 프로필이미지 추가 설정
        reviewDto.setAuthorNickname(authorNickname);
        reviewDto.setAuthorProfileImageUrl(authorProfileImageUrl);

        return reviewDto;

   }

    public List<ReviewDto> getReviewsByCategory(Long categoryId, Long userId){
        List<Review> reviews = reviewRepository.findByTicketCategoryId(categoryId);
        return reviews.stream().map(review -> getReviewDto(review.getId(), userId)).collect(Collectors.toList());
    }

    public void setFeaturedReview(Long userId, Long reviewId){
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

    public List<ReviewDto> searchReviews(String keyword, String searchType){
        Long currentUserId = userService.getCurrentUser().getId();
        List<Review> reviews = reviewRepository.searchByField(keyword,searchType);
        System.out.println("Search Results: " + reviews);
        return reviews.stream().map(review -> getReviewDto(review.getId(), currentUserId)).collect(Collectors.toList());
    }









}
