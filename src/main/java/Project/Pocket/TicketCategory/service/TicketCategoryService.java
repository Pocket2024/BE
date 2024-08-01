package Project.Pocket.TicketCategory.service;

import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.TicketCategory.entity.TicketCategoryRepository;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketCategoryService {
    private final TicketCategoryRepository ticketCategoryRepository;
    private final UserService userService;

    @Autowired
    public TicketCategoryService(TicketCategoryRepository ticketCategoryRepository, UserService userService){
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.userService = userService;
    }

    public TicketCategoryDto createTicketCategory(Long userId, String category){
        User user = userService.getUserById(userId);
        TicketCategory ticketCategory = new TicketCategory();
        ticketCategory.setUser(user);
        ticketCategory.setCategory(category);
        TicketCategory savedCategory = ticketCategoryRepository.save(ticketCategory);
        return savedCategory.toDto();
    }
    public TicketCategoryDto getTicketCategoryDtoById(Long categoryId){
        TicketCategory ticketCategory = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return ticketCategory.toDto();

    }
    public TicketCategory getTicketCategoryById(Long categoryId){
        return ticketCategoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
