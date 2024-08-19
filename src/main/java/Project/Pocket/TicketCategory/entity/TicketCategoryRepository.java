package Project.Pocket.TicketCategory.entity;

import Project.Pocket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    int countByUserId(Long userId);
    List<TicketCategory> findByUser(User user);
    Optional<TicketCategory> findByIdAndUserId(Long categoryId, Long userId);

}
