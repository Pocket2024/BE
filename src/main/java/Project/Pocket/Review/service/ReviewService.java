package Project.Pocket.Review.service;

import Project.Pocket.Image.entity.Image;
import Project.Pocket.Image.entity.ImageRepository;
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

    @Autowired
    public ReviewService(TicketCategoryService ticketCategoryService, ReviewRepository reviewRepository, ImageRepository imageRepository, UserService userService){
        this.ticketCategoryService = ticketCategoryService;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.userService = userService;
    }

    @Transactional
    public Review createReview(Long categoryId, String content, List<String> imageUrls, Long userId){
        // 사용자 ID와 카테고리 ID가 올바르게 전달되는지 확인
        TicketCategory ticketCategory = ticketCategoryService.getTicketCategoryById(categoryId);
        if (ticketCategory == null) {
            throw new IllegalArgumentException("Ticket category with id " + categoryId + " not found!");
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with id " + userId + " not found!");
        }



        Review review = new Review();
        review.setTicketCategory(ticketCategory);
        review.setContent(content);
        review.setUser(user);
        List<Image> images = imageUrls.stream().map(url -> {
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






}
