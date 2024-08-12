package Project.Pocket.CustomImage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomImageResponse {

    private Long id;
    private String CustomImageUrl;

    public CustomImageResponse(Long id, String CustomImageUrl){
        this.id = id;
        this.CustomImageUrl = CustomImageUrl;
    }
}
