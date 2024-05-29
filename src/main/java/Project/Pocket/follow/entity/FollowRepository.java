package Project.Pocket.follow.entity;

import Project.Pocket.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowingId(Long followingId);
    boolean existsByFollowerAndFollowing(User follower, User following);
    int countByFollowingId(Long userId);
    int countByFollowerId(Long userId);

}
