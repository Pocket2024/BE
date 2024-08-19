package Project.Pocket.CustomImage.entity;

import Project.Pocket.Review.entity.Review;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class CustomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String CustomImageUrl;

    @OneToOne(mappedBy = "customImage",cascade = CascadeType.ALL,orphanRemoval = true)
    private Review review;
}
