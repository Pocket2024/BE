package Project.Pocket.user.dto;

import Project.Pocket.Review.dto.ReviewDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private Long id;
    private String nickName;
    private String bio;
    private String profileImageUrl;
    private int followersCount;
    private int followingsCount;
    private int ticketCategoryCount;
    private int reviewCount;
    private Long featuredReviewId;



}

