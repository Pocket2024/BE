package Project.Pocket.Review.service;

import Project.Pocket.CustomImage.entity.CustomImage;
import Project.Pocket.CustomImage.entity.CustomImageRepository;
import Project.Pocket.Image.entity.Image;
import Project.Pocket.Image.entity.ImageDto;
import Project.Pocket.Image.entity.ImageRepository;
import Project.Pocket.Image.service.ImageService;
import Project.Pocket.Like.entity.LikeRepository;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.dto.ReviewRequest;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.entity.ReviewRepository;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.TicketCategory.service.TicketCategoryService;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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


    @Autowired
    public ReviewService(@Lazy TicketCategoryService ticketCategoryService, ReviewRepository reviewRepository, ImageRepository imageRepository, @Lazy UserService userService, LikeRepository likeRepository, CustomImageRepository customImageRepository){
        this.ticketCategoryService = ticketCategoryService;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.likeRepository = likeRepository;
        this.customImageRepository = customImageRepository;
    }

    public String saveImage(MultipartFile imageFile) throws IOException{
        if (imageFile == null || imageFile.isEmpty()) {
            return ""; //
        }
        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path imagePath = Paths.get("src/main/resources/static/images",fileName);
        Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        String baseUrl = "http://localhost:8080";
        return baseUrl + "/images/" + fileName;
    }

    @Transactional
    public Review createReview(ReviewRequest reviewRequest, Long customImageId) {
        // 사용자 ID와 카테고리 ID가 올바르게 전달되는지 확인
        TicketCategory ticketCategory = ticketCategoryService.getTicketCategoryById(reviewRequest.getTicketCategoryId());
        if (ticketCategory == null) {
            throw new IllegalArgumentException("Ticket category with id " + reviewRequest.getTicketCategoryId() + " not found!");
        }

        User user = userService.getCurrentUser();
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }

        Review review = new Review();
        review.setTicketCategory(ticketCategory);
        review.setContent(reviewRequest.getContent());
        review.setLocation(reviewRequest.getLocation());
        review.setDate(reviewRequest.getDate());
        review.setTitle(reviewRequest.getTitle());
        review.setSeat(reviewRequest.getSeat());
        review.setUser(user);
        //리뷰 저장
        review = reviewRepository.save(review);
        // 이미지 파일들 저장 + 그 url을 리스트로 반환
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : reviewRequest.getImages()) {
            try {
                String imageUrl = saveImage(file);
                Image image = new Image();
                image.setReview(review);
                image.setUrl(imageUrl);
                images.add(image);
            } catch (IOException e) {
                // 이미지 저장 실패 시 로깅하거나 적절히 처리
                System.err.println("Failed to save image: " + e.getMessage());
            }
        }
        // 이미지와 리뷰를 연관 짓기
        review.setImages(images);
        reviewRepository.save(review);

        if (customImageId != null) {
            CustomImage customImage = customImageRepository.findById(customImageId)
                    .orElseThrow(() -> new IllegalArgumentException("Custom image not found with id: " + customImageId));
            review.setCustomImage(customImage);
        }

        return review;


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
        reviewDto.setCustomImageId(review.getCustomImage().getId());
        return reviewDto;
   }


   public ReviewDto getReviewDto(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        //TicketCategory -> DTO
        TicketCategoryDto ticketCategoryDto = ticketCategoryService.getTicketCategoryDtoById(review.getTicketCategory().getId());
       //이미지 -> DTO
        List<ImageDto> imageDtos = review.getImages().stream()
               .map(ImageService::mapToDto)
               .collect(Collectors.toList());

        boolean likedByCurrentUser = likeRepository.existsByReviewIdAndUserId(reviewId, userId);
        //review -> DTO 전환
        return mapToDto(review, ticketCategoryDto, imageDtos, likedByCurrentUser);
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









}
