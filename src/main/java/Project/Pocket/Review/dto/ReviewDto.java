package Project.Pocket.Review.dto;

import Project.Pocket.Image.entity.ImageDto;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ReviewDto {
    private Long id;
    private String content;
    private String title;
    private String seat;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate date;
    private String location;
    private boolean isPrivate;
    private boolean isOcr;
    TicketCategoryDto ticketcategory;
    private List<ImageDto> images;
    private boolean likedByCurrentUser;
    private int likes;
    private String customImageUrl;
    private String authorNickname;
    private String authorProfileImageUrl;


}
