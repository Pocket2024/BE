package Project.Pocket.Review.entity;

import Project.Pocket.TicketCategory.entity.TicketCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUserId(Long userId);
    int countByUserId(Long userId);
    Optional<Review> findByUserIdAndIsFeaturedTrue(Long userId);
    List<Review> findByTicketCategoryId(Long ticketCategoryId);
    int countByTicketCategory(TicketCategory ticketCategory);
    List<Review> findAllByIsPrivateFalseOrderByCreatedAtDesc();
    @Query("SELECT r.date FROM Review  r WHERE r.user.id = :userId")
    List<LocalDate> findReviewDatesByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r LEFT JOIN r.likes l WHERE r.isPrivate = false GROUP BY r ORDER BY COUNT(l) DESC")
    List<Review> findReviewsByLikeCountAndIsPrivateFalse();

    List<Review> findAllByOrderByDateDesc();






    @Query("SELECT r FROM Review r " +
            "WHERE r.isPrivate = false AND " +
            "((:searchType = 'title' AND r.title LIKE %:keyword%) OR " +
            "(:searchType = 'content' AND r.content LIKE %:keyword%) OR " +
            "(:searchType = 'location' AND r.location LIKE %:keyword%) OR " +
            "(:searchType = 'seat' AND r.seat LIKE %:keyword%) OR " +
            "(:searchType = 'date' AND r.date BETWEEN :startDate AND :endDate))")
    List<Review> searchByField(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Review> findByIdAndUserId(Long reviewId, Long userId);
}
