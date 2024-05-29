package Project.Pocket.user.dto;


import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRoleEnum;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SignUpRequest {



        private final String email;

        private final String nickName;


        private final String password;


        private final String password2;

        private final String bio;
        private final String phoneNumber;







        @Builder
        public SignUpRequest(String email, String nickName, String password, String password2,
                             String bio, String phoneNumber, String address) {
            this.email = email;
            this.nickName = nickName;
            this.password = password;
            this.password2 = password2;
            this.bio = bio;
            this.phoneNumber = phoneNumber;


        }

        /**
         * DTO -> Entity
         * @param role
         * @param encodedPassword
         * @return
         */
        public User toEntity(UserRoleEnum role, String encodedPassword) {
            return User.builder()
                    .nickname(nickName)
                    .email(email)
                    .bio(bio)
                    .password(encodedPassword)
                    .phoneNumber(phoneNumber)
                    .role(role)
                    .build();
        }
    }

