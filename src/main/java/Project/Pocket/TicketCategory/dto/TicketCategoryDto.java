package Project.Pocket.TicketCategory.dto;

import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.user.dto.UserResponse;

import java.util.List;

public class TicketCategoryDto {
    private Long id;
//    private UserResponse user;
    private String category;
    private List<ReviewDto> reviews;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public UserResponse getUser() {
//        return user;
//    }
//
//    public void setUser(UserResponse user) {
//        this.user = user;
//    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

//    public List<ReviewDto> getReviews() {
//        return reviews;
//    }
//
//    public void setReviews(List<ReviewDto> reviews) {
//        this.reviews = reviews;
//    }
}
