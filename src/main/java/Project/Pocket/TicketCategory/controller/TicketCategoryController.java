package Project.Pocket.TicketCategory.controller;

import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.dto.TicketCategoryRequest;
import Project.Pocket.TicketCategory.dto.TicketCategoryResponse;
import Project.Pocket.TicketCategory.service.TicketCategoryService;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class TicketCategoryController {
    private final TicketCategoryService ticketCategoryService;
    private final UserService userService;

    @Autowired
    public TicketCategoryController(TicketCategoryService ticketCategoryService, UserService userService){
        this.ticketCategoryService = ticketCategoryService;
        this.userService = userService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<TicketCategoryDto> createTicketCategory(@PathVariable Long userId, @RequestBody TicketCategoryRequest ticketCategoryRequest){
        TicketCategoryDto ticketCategoryDto = ticketCategoryService.createTicketCategory(userId, ticketCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketCategoryDto);
    }
    @GetMapping("getTicketCategories")
    public ResponseEntity<TicketCategoryResponse> getTicketCategories(){
        User user = userService.getCurrentUser();
        TicketCategoryResponse response = ticketCategoryService.getTicketCategoryByUser(user);
        return ResponseEntity.ok(response);
    }

}
