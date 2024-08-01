package Project.Pocket.Review.controller;

import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.dto.ReviewRequest;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.service.ReviewService;
import Project.Pocket.user.dto.UserDto;
import Project.Pocket.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;

    }

    @PostMapping("/{categoryId}")
    public ResponseEntity<ReviewDto> createReview(@PathVariable Long categoryId, @RequestParam Long userId, @RequestBody ReviewRequest reviewRequest){
        Review review = reviewService.createReview(categoryId, reviewRequest.getContent(),reviewRequest.getImageUrls(),userId);
        ReviewDto reviewDto = review.toDto();
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
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


}
