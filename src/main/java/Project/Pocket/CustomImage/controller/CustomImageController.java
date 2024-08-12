package Project.Pocket.CustomImage.controller;

import Project.Pocket.CustomImage.dto.CustomImageResponse;
import Project.Pocket.CustomImage.entity.CustomImage;
import Project.Pocket.CustomImage.entity.CustomImageRepository;
import Project.Pocket.Review.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/customImage")
public class CustomImageController {


    private final CustomImageRepository customImageRepository;
    private final ReviewService reviewService;

    public CustomImageController(CustomImageRepository customImageRepository, ReviewService reviewService) {
        this.customImageRepository = customImageRepository;
        this.reviewService = reviewService;
    }

        @PostMapping("/upload")
        public ResponseEntity<CustomImageResponse> uploadCustomImage(@RequestParam("image")MultipartFile imageFile){
            try{
                String imageUrl = reviewService.saveImage(imageFile);
                CustomImage customImage = new CustomImage();
                customImage.setCustomImageUrl(imageUrl);
                // 커스텀 이미지 저장
                customImage = customImageRepository.save(customImage);

                //응답 객체 생성
                CustomImageResponse customImageResponse = new CustomImageResponse(customImage.getId(), imageUrl);

                return ResponseEntity.ok(customImageResponse);

            }catch (IOException e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

}
