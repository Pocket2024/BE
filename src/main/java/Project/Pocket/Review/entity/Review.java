package Project.Pocket.Review.entity;

import Project.Pocket.CustomImage.entity.CustomImage;
import Project.Pocket.Image.entity.Image;
import Project.Pocket.Like.entity.Like;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.user.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_category_id", nullable = false)
    private TicketCategory ticketCategory;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String seat;


    @Column(nullable = false)
    private String content;

    private boolean isFeatured;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Image> images;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Like> likes = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "custom_image_id")
    private CustomImage customImage;



    public int getLikeCount(){
        return likes.size();
    }

    public List<User> getLikedUsers() {
        return likes.stream().map(Like::getUser).collect(Collectors.toList());
    }



}
