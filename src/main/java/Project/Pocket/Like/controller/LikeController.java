package Project.Pocket.Like.controller;

import Project.Pocket.Like.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService){
        this.likeService = likeService;
    }

    @PostMapping("/like/{reviewId}")
    public ResponseEntity<Void> likeReview(@PathVariable Long reviewId, @RequestParam Long userId){
        likeService.likeReview(reviewId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/unlike/{reviewId}")
    public ResponseEntity<String> unlikeReview(@PathVariable Long reviewId, @RequestParam Long userId){
        likeService.unlikeReview(reviewId,userId);
        return ResponseEntity.ok("Unlike succeed");
    }
}
