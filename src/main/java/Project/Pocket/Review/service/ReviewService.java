package Project.Pocket.Review.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final TicketCategoryService ticketCategoryService;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;

    @Autowired
    public ReviewService(TicketCategoryService ticketCategoryService, ReviewRepository reviewRepository, ImageRepository imageRepository, UserService userService, LikeRepository likeRepository){
        this.ticketCategoryService = ticketCategoryService;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public Review createReview(ReviewRequest reviewRequest){
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
        List<Image> images = reviewRequest.getImageUrls().stream().map(url -> {
            Image image = new Image();
            image.setReview(review);
            image.setUrl(url);
            return image;
        }).collect(Collectors.toList());
        review.setImages(images);
        return reviewRepository.save(review);
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









}
