package Project.Pocket.Like.entity;

import Project.Pocket.Review.entity.Review;
import Project.Pocket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public  interface  LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

}
