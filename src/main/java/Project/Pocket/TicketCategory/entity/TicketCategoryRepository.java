package Project.Pocket.TicketCategory.entity;

import Project.Pocket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    int countByUserId(Long userId);
    List<TicketCategory> findByUser(User user);

}
