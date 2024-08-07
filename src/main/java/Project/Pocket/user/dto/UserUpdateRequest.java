package Project.Pocket.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserUpdateRequest {

    private String email;
    private String nickname;
    private String phoneNumber;
    private String bio;
    private MultipartFile profileImage;


}

