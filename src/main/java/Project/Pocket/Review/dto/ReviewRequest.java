package Project.Pocket.Review.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReviewRequest {
    private Long ticketCategoryId;
    private String content;
    private List<String> imageUrls;
    private String title;
    private String date;
    private String location;
    private String seat;






}
