package Project.Pocket.Review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ReviewRequest {
    private Long ticketCategoryId;
    private String content;
    private List<MultipartFile> images;
    private String title;
    @NotNull
    @DateTimeFormat(pattern = "yyyy.MM.dd")
    private LocalDate date;
    private String location;
    private String seat;
    private boolean isPrivate;
    private boolean isOcr;





}
