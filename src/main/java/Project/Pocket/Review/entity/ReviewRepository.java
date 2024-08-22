package Project.Pocket.Review.entity;

import Project.Pocket.TicketCategory.entity.TicketCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    int countByUserId(Long userId);
    Optional<Review> findByUserIdAndIsFeaturedTrue(Long userId);
    List<Review> findByTicketCategoryId(Long ticketCategoryId);
    int countByTicketCategory(TicketCategory ticketCategory);
    List<Review> findAllByOrderByCreatedAtDesc();
    @Query("SELECT r FROM Review r LEFT JOIN r.likes l GROUP BY r ORDER BY COUNT(l) DESC")
    List<Review> findAllByOrderByLikeCountDesc();



//    @Query("SELECT r FROM Review r WHERE " +
//            "(r.title LIKE %:keyword% AND :searchType = 'title') OR " +
//            "(r.content LIKE %:keyword% AND :searchType = 'content') OR " +
//            "(r.location LIKE %:keyword% AND :searchType = 'location') OR " +
//            "(r.seat LIKE %:keyword% AND :searchType = 'seat') OR " +
//            "(r.date LIKE %:keyword% AND :searchType = 'date')")


    @Query("SELECT r FROM Review r " +
            "JOIN r.user u " + // Review 엔티티와 User 엔티티를 조인
            "WHERE u.isPrivate = false AND " + // 비공개 계정 제외
            "((r.title LIKE %:keyword% AND :searchType = 'title') OR " +
            "(r.content LIKE %:keyword% AND :searchType = 'content') OR " +
            "(r.location LIKE %:keyword% AND :searchType = 'location') OR " +
            "(r.seat LIKE %:keyword% AND :searchType = 'seat') OR " +
            "(r.date LIKE %:keyword% AND :searchType = 'date'))")
    List<Review> searchByField(@Param("keyword") String keyword, @Param("searchType") String searchType);
    Optional<Review> findByIdAndUserId(Long reviewId, Long userId);
}
