package Project.Pocket.Review.entity;

import Project.Pocket.Image.entity.Image;
import Project.Pocket.Like.entity.Like;
import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Getter
@Setter
@Entity
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
    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Image> images;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Like> likes;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public TicketCategory getTicketCategory() {
//        return ticketCategory;
//    }
//
//    public void setTicketCategory(TicketCategory ticketCategory) {
//        this.ticketCategory = ticketCategory;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public List<Image> getImages() {
//        return images;
//    }
//
//    public void setImages(List<Image> images) {
//        this.images = images;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }

    public int getLikeCount(){
        return likes.size();
    }

    public List<User> getLikedUsers() {
        return likes.stream().map(Like::getUser).collect(Collectors.toList());
    }

    public ReviewDto toDto() {
        ReviewDto dto = new ReviewDto();
        dto.setId(this.id);
        dto.setContent(this.content);
        dto.setTicketCategory(this.ticketCategory.toDto());
        dto.setImages(this.images.stream().map(Image::toDto).collect(Collectors.toList()));
        dto.setLikes(this.getLikeCount());

        return dto;
    }

}
