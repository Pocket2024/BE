package Project.Pocket.Review.controller;


import Project.Pocket.Image.service.ImageService;
import Project.Pocket.Like.service.LikeService;
import Project.Pocket.Review.dto.ReviewDateDto;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.dto.ReviewRequest;

import Project.Pocket.Review.dto.ReviewTranslateDto;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.service.ReviewService;

import Project.Pocket.TicketCategory.service.TicketCategoryService;
import Project.Pocket.chatbot.service.GPTService;
import Project.Pocket.user.dto.UserDto;
import Project.Pocket.user.entity.User;

import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final TicketCategoryService ticketCategoryService;
    private final ImageService imageService;
    private final LikeService likeService;
    private final UserService userService;
    private final GPTService gptService;

    @Autowired
    public ReviewController(ReviewService reviewService, TicketCategoryService ticketCategoryService,ImageService imageService, LikeService likeService, UserService userService, GPTService gptService) {
        this.reviewService = reviewService;
        this.ticketCategoryService = ticketCategoryService;
        this.imageService = imageService;
        this.likeService = likeService;
        this.userService = userService;
        this.gptService = gptService;

    }

    @PostMapping
    public ResponseEntity<String> createReview(@ModelAttribute @Validated ReviewRequest reviewRequest, @RequestParam(value = "customImageFile", required = false) MultipartFile customImageFile) {
        Review review = reviewService.createReview(reviewRequest, customImageFile);
        // 커스텀 이미지가 존재하는 경우 URL 반환, 없으면 null 반환
        String customImageUrl = review.getCustomImage() != null ? review.getCustomImage().getCustomImageUrl() : null;

        // 커스텀 이미지 URL을 반환
        return ResponseEntity.ok(customImageUrl);


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
                .map(User:: toDto) // Assuming User entity has a toDto() method
                .collect(Collectors.toList());
        return ResponseEntity.ok(likedUsers);
    }
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long reviewId, @RequestParam Long userId){
        ReviewDto reviewDto = reviewService.getReviewDto(reviewId, userId);
        return ResponseEntity.ok(reviewDto);
    }

    @PostMapping("/{reviewId}/feature")
    public ResponseEntity<String> setFeaturedReview(@PathVariable Long reviewId) {
        // 로그인한 유저 ID 가져오기
        User user = userService.getCurrentUser();
        Long userId = user.getId();

        reviewService.setFeaturedReview(userId, reviewId);

        return ResponseEntity.ok("Review Featured");
    }
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ReviewDto>>getReviewsByCategory(@PathVariable Long categoryId, @RequestParam Long userId){
        List<ReviewDto> reviews = reviewService.getReviewsByCategory(categoryId, userId);
        return ResponseEntity.ok(reviews);

    }
    @GetMapping("/search")
    public ResponseEntity<List<ReviewDto>> searchReviews(@RequestParam String keyword, @RequestParam String searchType){
        if (!List.of("title", "content", "location", "seat", "date").contains(searchType)) {
            return ResponseEntity.badRequest().body(null);
        }
        List<ReviewDto> reviewDtos = reviewService.searchReviews(keyword, searchType);
        return ResponseEntity.ok(reviewDtos);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId, @RequestParam Long userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok("Review deleted");
    }

    @GetMapping("/latest")
    public ResponseEntity<List<ReviewDto>> getLatestReviews(){
        List<ReviewDto> reviews = reviewService.getReviewsByLatest();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ReviewDto>> getPopularReviews(){
        List<ReviewDto> reviews = reviewService.getReviewsByLikeCount();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<ReviewDto>> getSortedReviews(){
        List<ReviewDto> reviews = reviewService.getReviewsSortedByDate();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/dates")
    public List<ReviewDateDto> getAllReviewDates( @RequestParam Long userId) {
        List<LocalDate> dates = reviewService.getAllReviewDates(userId);
        // 날짜 리스트를 ReviewDateDto로 변환
        return dates.stream()
                .map(date -> {
                    ReviewDateDto dto = new ReviewDateDto();
                    dto.setDate(date);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/bydates")
    public ResponseEntity<List<ReviewDto>> getReviewsByDate(@RequestParam Long userId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ReviewDto> reviewsByDate = reviewService.getReviewsByDate(userId, date);
        return ResponseEntity.ok(reviewsByDate);
    }

    @PostMapping("/translate")
    public ResponseEntity<ReviewTranslateDto> translateReview(@RequestBody ReviewTranslateDto reviewTranslateDto) {
        ReviewTranslateDto translatedReview = gptService.translateReview(reviewTranslateDto);
        return ResponseEntity.ok(translatedReview);
    }
    @GetMapping("/liked")
    public ResponseEntity<List<ReviewDto>> getLikedReviews(@RequestParam Long userId){
        List<ReviewDto> likedReviews = reviewService.getLikedReviewsByUserId(userId);
        return ResponseEntity.ok(likedReviews);
    }



}
