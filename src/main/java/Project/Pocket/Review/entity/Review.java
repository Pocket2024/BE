package Project.Pocket.Review.entity;

import Project.Pocket.CustomImage.entity.CustomImage;
import Project.Pocket.Image.entity.Image;
import Project.Pocket.Like.entity.Like;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;


import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
    @JsonIgnore
    @JoinColumn(name = "ticket_category_id", nullable = false)
    private TicketCategory ticketCategory;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate date;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String seat;


    @Column(nullable = false)
    private String content;

    private boolean isFeatured;

    private boolean isPrivate;

    private boolean isOcr;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "custom_image_id")
    private CustomImage customImage;



    public int getLikeCount(){
        return likes.size();
    }

    public List<User> getLikedUsers() {
        return likes.stream().map(Like::getUser).collect(Collectors.toList());
    }



}
