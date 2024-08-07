package Project.Pocket.Review.controller;

import Project.Pocket.Image.entity.ImageDto;
import Project.Pocket.Image.service.ImageService;
import Project.Pocket.Like.service.LikeService;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.dto.ReviewRequest;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.service.ReviewService;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.service.TicketCategoryService;
import Project.Pocket.user.dto.UserDto;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final TicketCategoryService ticketCategoryService;
    private final ImageService imageService;
    private final LikeService likeService;

    @Autowired
    public ReviewController(ReviewService reviewService, TicketCategoryService ticketCategoryService,ImageService imageService, LikeService likeService) {
        this.reviewService = reviewService;
        this.ticketCategoryService = ticketCategoryService;
        this.imageService = imageService;
        this.likeService = likeService;

    }

    @PostMapping
    public ResponseEntity<String> createReview(@RequestBody ReviewRequest reviewRequest) {

        Review review = reviewService.createReview(reviewRequest);
        return ResponseEntity.ok("Review created successfully");
    }

    @GetMapping("/{reviewId}/likes")
    public ResponseEntity<Integer> getReviewLikesCount(@PathVariable Long reviewId){
            int likesCount = reviewService.getReviewLikesCount(reviewId);
            return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/{reviewId}/likedUsers")
    public ResponseEntity<List<UserDto>> getReviewLikedUsers(@PathVariable Long reviewId) {
        List<UserDto> likedUsers = reviewService.getReviewLikedUsers(reviewId)
                .stream()
                .map(User::toDto) // Assuming User entity has a toDto() method
                .collect(Collectors.toList());
        return ResponseEntity.ok(likedUsers);
    }
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long reviewId, @RequestParam Long userId){
        ReviewDto reviewDto = reviewService.getReviewDto(reviewId, userId);
        return ResponseEntity.ok(reviewDto);
    }

}
