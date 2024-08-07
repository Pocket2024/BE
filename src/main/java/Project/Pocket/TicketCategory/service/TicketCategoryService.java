package Project.Pocket.TicketCategory.service;

import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.service.ReviewService;
import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.dto.TicketCategoryRequest;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.TicketCategory.entity.TicketCategoryRepository;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketCategoryService {
    private final TicketCategoryRepository ticketCategoryRepository;
    private final UserService userService;

    @Autowired
    public TicketCategoryService(TicketCategoryRepository ticketCategoryRepository, UserService userService){
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.userService = userService;
    }

    public TicketCategoryDto createTicketCategory(Long userId, TicketCategoryRequest ticketCategoryRequest){
        User user = userService.getUserById(userId);
        String category = ticketCategoryRequest.getCategory();
        String color = ticketCategoryRequest.getColor();
        //생성
        TicketCategory ticketCategory = new TicketCategory();
        ticketCategory.setUser(user);
        ticketCategory.setCategory(category);
        ticketCategory.setColor(color);
        //저장
        TicketCategory savedCategory = ticketCategoryRepository.save(ticketCategory);
        //TicketCategory --> DTO
        return mapToDto(savedCategory);


    }
    public TicketCategoryDto getTicketCategoryDtoById(Long categoryId){
        TicketCategory ticketCategory = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToDto(ticketCategory);

    }
    public TicketCategory getTicketCategoryById(Long categoryId){
        return ticketCategoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public static TicketCategoryDto mapToDto(TicketCategory ticketCategory){
        TicketCategoryDto dto = new TicketCategoryDto();
        dto.setId(ticketCategory.getId());
        dto.setCategory(ticketCategory.getCategory());
        dto.setColor(ticketCategory.getColor());

        return  dto;

    }


}
