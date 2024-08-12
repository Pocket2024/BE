package Project.Pocket.TicketCategory.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TicketCategoryResponse {

    private List<TicketCategoryDto> categories;
    private int totalCategories;

}
