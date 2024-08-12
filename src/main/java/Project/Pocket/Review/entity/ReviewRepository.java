package Project.Pocket.Review.entity;

import Project.Pocket.TicketCategory.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    int countByUserId(Long userId);
    Optional<Review> findByUserIdAndIsFeaturedTrue(Long userId);
    List<Review> findByTicketCategoryId(Long ticketCategoryId);
    int countByTicketCategory(TicketCategory ticketCategory);
}
