package Project.Pocket.user.dto;

import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRoleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(force = true)
public class UserResponse implements Serializable {

    private final String email;
    private final String nickname;

    private final UserRoleEnum role;

    private final String profileImage;
    private final Long userId;

    /**
     *  유저 생성자를 private로 외부에서 생성 할수 없도록 함
     * @param user
     */
    private UserResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.profileImage = user.getProfileImage();
        this.userId = user.getId();
    }

    /**
     *  유저 생성자를 private로 외부에서 생성 할수 없도록 함으로 써
     *  of 메서드를 통해
     *  유저 객체를 DTO에 담아 반환해줍니다.
     * @param user
     * @return
     */
    public static UserResponse of(User user){

        return new UserResponse(user);
    }


}
