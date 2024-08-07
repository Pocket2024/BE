package Project.Pocket.TicketCategory.controller;

import Project.Pocket.TicketCategory.dto.TicketCategoryDto;
import Project.Pocket.TicketCategory.dto.TicketCategoryRequest;
import Project.Pocket.TicketCategory.entity.TicketCategory;
import Project.Pocket.TicketCategory.service.TicketCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class TicketCategoryController {
    private final TicketCategoryService ticketCategoryService;

    @Autowired
    public TicketCategoryController(TicketCategoryService ticketCategoryService){
        this.ticketCategoryService = ticketCategoryService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<TicketCategoryDto> createTicketCategory(@PathVariable Long userId, @RequestBody TicketCategoryRequest ticketCategoryRequest){
        TicketCategoryDto ticketCategoryDto = ticketCategoryService.createTicketCategory(userId, ticketCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketCategoryDto);
    }

}
