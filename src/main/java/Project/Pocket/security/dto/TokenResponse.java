package Project.Pocket.security.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
    //추가
    private Long userId;

    public TokenResponse(String accessToken, String refreshToken, Long userId){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }
}
