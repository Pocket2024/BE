package Project.Pocket.Like.service;

import Project.Pocket.Like.entity.Like;
import Project.Pocket.Like.entity.LikeRepository;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.entity.ReviewRepository;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @Autowired
    public LikeService(LikeRepository likeRepository, ReviewRepository reviewRepository, @Lazy UserService userService){
        this.likeRepository = likeRepository;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }

    @Transactional
    public void likeReview(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException(("Review with id " + reviewId + " not found! ")));

        User user = userService.getUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("User with id "  + userId + " not found!");
        }
        if(likeRepository.existsByReviewIdAndUserId(reviewId, userId)){
            throw new IllegalArgumentException("User with id " + userId + " already liked this review!");
        }

        Like like = new Like();
        like.setReview(review);
        like.setUser(user);
        likeRepository.save(like);

    }

    public  boolean isReviewLikedByUser(Long reviewId, Long userId){
        return likeRepository.existsByReviewIdAndUserId(reviewId, userId);
    }
}
