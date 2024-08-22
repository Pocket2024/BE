package Project.Pocket.user.dto;


import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRoleEnum;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SignUpRequest {

        private final String email;
        private final String nickName;
        private final String password;
        private final String password2;
        private final String bio;
        private final String phoneNumber;
        private MultipartFile profileImage;
        private boolean isPrivate;


        //DTO -> Entity
        public User toEntity(UserRoleEnum role, String encodedPassword) {
            return User.builder()
                    .nickname(nickName)
                    .email(email)
                    .bio(bio)
                    .password(encodedPassword)
                    .phoneNumber(phoneNumber)
                    .role(role)
                    .isPrivate(isPrivate)
                    .build();
        }
    }

