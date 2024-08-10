package Project.Pocket.user.entity;

import Project.Pocket.redis.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNickname(String nickname);
    @Cacheable(cacheNames = CacheNames.USERBYUSERNAME, key = "'login'+#p0", unless = "#result==null" )
    Optional<User> findByEmail(String email);



    // 팔로잉 수 증가

}



