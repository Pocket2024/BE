package Project.Pocket.Like.service;

import Project.Pocket.Like.entity.Like;
import Project.Pocket.Like.entity.LikeRepository;
import Project.Pocket.Review.entity.Review;
import Project.Pocket.Review.entity.ReviewRepository;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@Service
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    @PersistenceContext
    private EntityManager entityManager;


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
    @Transactional
    public void unlikeReview(Long reviewId, Long userId) {


        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {

                    return new IllegalArgumentException("No review found");
                });


        // 사용자 조회
        User user = userService.getUserById(userId);
        if (user == null) {
            System.out.println("No user found for userId: " + userId);
            throw new IllegalArgumentException("No user found");
        }
        System.out.println("Found user: " + user);

        // 좋아요 조회
        Like like = likeRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> {
                    System.out.println("No like found for reviewId: " + reviewId + " and userId: " + userId);
                    return new IllegalArgumentException("No like exists");
                });
        System.out.println("Found like: " + like);

        // 좋아요 삭제
        likeRepository.delete(like);
        entityManager.flush();
        System.out.println("Like deleted successfully");
    }



}
