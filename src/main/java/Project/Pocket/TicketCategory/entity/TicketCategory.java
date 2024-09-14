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
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String Color;


   @OneToMany(mappedBy = "ticketCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   @JsonIgnore
   private List<Review> reviews = new ArrayList<>();


    }





