package Project.Pocket.user.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;



    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    public class LoginRequest {

        private final String email;
        private final String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }


    }

