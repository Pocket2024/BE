package Project.Pocket.TicketCategory.entity;


import Project.Pocket.Review.entity.Review;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.user.dto.UserResponse;
import Project.Pocket.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String Color;


   @OneToMany(mappedBy = "ticketCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   private List<Review> reviews = new ArrayList<>();

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    ublic void setUser(User user) {
//        this.user = user;
//    }
//
//    public String getCategory() {
//        return category;
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
//    }
//
//    public List<Review> getReviews() {
//        return reviews;
//    }
//
//    public void setReviews(List<Review> reviews) {
//        this.reviews = reviews;
//    }public User getUser() {
//        return user;
    }





