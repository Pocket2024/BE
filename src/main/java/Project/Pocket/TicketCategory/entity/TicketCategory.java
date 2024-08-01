package Project.Pocket.TicketCategory.entity;


import Project.Pocket.Review.entity.Review;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.user.dto.UserResponse;
import Project.Pocket.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category;


   @OneToMany(mappedBy = "ticketCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   private List<Review> reviews = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public TicketCategoryDto toDto(){
        TicketCategoryDto dto = new TicketCategoryDto();
        dto.setId(this.id);
//        dto.setUser(UserResponse.of(this.user));
        dto.setCategory(this.category.trim());
//        if (this.reviews != null) {
//            dto.setReviews(this.reviews.stream().map(Review::toDto).collect(Collectors.toList()));
//        } else {
//            dto.setReviews(new ArrayList<>());
//        }
        return dto;
    }
}
