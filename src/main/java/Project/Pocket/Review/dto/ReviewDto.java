package Project.Pocket.Review.dto;

import Project.Pocket.Image.entity.ImageDto;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReviewDto {
    private Long id;
    private String content;
    private String title;
    private String seat;
    private String date;
    private String location;
    TicketCategoryDto ticketcategory;
    private List<ImageDto> images;
    private boolean likedByCurrentUser;
    private int likes;


}
