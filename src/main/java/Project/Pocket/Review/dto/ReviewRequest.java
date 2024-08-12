package Project.Pocket.Review.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReviewRequest {
    private Long ticketCategoryId;
    private String content;
    private List<MultipartFile> images;
    private String title;
    private String date;
    private String location;
    private String seat;





}
