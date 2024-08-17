package Project.Pocket.Like.entity;

import Project.Pocket.Review.entity.Review;
import Project.Pocket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public  interface  LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    Optional<Like> findByReviewIdAndUserId(Long reviewId, Long userId);


}
