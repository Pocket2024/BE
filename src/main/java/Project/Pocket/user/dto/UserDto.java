package Project.Pocket.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private Long id;
    private String nickname;
    private String bio;
    private String profileImageUrl;
}
