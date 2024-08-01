package Project.Pocket.Review.dto;

import Project.Pocket.Image.entity.ImageDto;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;

import java.util.List;

public class ReviewDto {
    private Long id;
    private String content;
    TicketCategoryDto ticketcategory;
    private List<ImageDto> images;

    private int likes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TicketCategoryDto getTicketcategory() {
        return ticketcategory;
    }

    public void setTicketCategory(TicketCategoryDto ticketcategory) {
        this.ticketcategory = ticketcategory;
    }

    public List<ImageDto> getImages() {
        return images;
    }

    public void setImages(List<ImageDto> images) {
        this.images = images;
    }
    public int getLikes(int likes){
        return likes;
    }
    public void setLikes(int likes){
        this.likes = likes;
    }
}
