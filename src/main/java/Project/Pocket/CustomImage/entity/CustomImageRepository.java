package Project.Pocket.CustomImage.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomImageRepository extends JpaRepository<CustomImage, Long> {
    Optional<CustomImage> findById(Long customImageId);
}
