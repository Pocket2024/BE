package Project.Pocket.user.controller;

import Project.Pocket.security.dto.ReissueTokenRequest;
import Project.Pocket.security.dto.TokenResponse;
import Project.Pocket.security.jwt.JwtProvider;
import Project.Pocket.security.service.UserDetailsImpl;
import Project.Pocket.user.dto.*;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.exception.UserNotFoundException;
import Project.Pocket.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;
    private final JwtProvider jwtProvider;


    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Validated SignUpRequest signUpRequest, HttpServletRequest request) {
        // 패스워드 1과 패스워드 2가 동일한지 체크
        if (!signUpRequest.getPassword().equals(signUpRequest.getPassword2())) {
            // 비밀번호가 일치하지 않는 경우 400 Bad Request 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
        }

        // 회원가입 처리
        try {
            // signup 메서드에 HttpServletRequest를 전달
            return userService.signup(signUpRequest, request);
        } catch (Exception e) {
            // 회원가입 중 에러가 발생한 경우 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원가입 중 문제가 발생했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 로그인
     * 로그인 시 atk, rtk 이 생성되어 response header 에 담아 보낸다.
     * rtk 는 레디스에 저장한다. 추후 atk 만료시 rtk를 이용해 atk 재발급 하기 위함
     */

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        UserResponse user = userService.login(loginRequest);
        TokenResponse token = jwtProvider.createTokenByLogin(user.getEmail(),
                user.getRole());//atk, rtk 생성
        response.addHeader(jwtProvider.AUTHORIZATION_HEADER, token.getAccessToken());// 헤더에 엑세스 토큰만 싣기
        return token;
    }

    /**
     * 로그아웃
     * 현 accessToken 은 다시 사용하지 못하도록 레디스에 저장해두고,
     * 로그아웃시 레디스에 저장된 refreshToken 삭제
     *
     * @param userDetails
     * @param request
     * @return
     */
    @DeleteMapping("/logout")
    public ResponseEntity logout(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        String accessToken = jwtProvider.resolveToken(request);
        return userService.logout(accessToken, userDetails.getUsername());//username = email

    }

    /**
     *  해당 유저의 정보 확인
     * @param userDetails
     * @return
     */
//    @GetMapping("/user-info")
//    public UserResponse getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
//        return userService.getUserInfo(userDetails.getUsername());//username = email
//    }

    /**
     * AccessToken  재발급
     * 매 API 호출 시 시큐리티필터를 통해 인증인가를 받게  된다. 이때 만료된 토큰인지 검증하고 만료시 만료된토큰임을 에러메세지로 보낸다.
     * 그럼 클라이언트에서 에러메세지를 확인 후 이 api(atk 재발급 ) 을 요청 하게 된다.
     *
     * @param userDetails
     * @param tokenRequest : refreshToken
     * @return AccessToken + RefreshToken
     */
    @PostMapping("/reissue-token")
    public TokenResponse reissueToken(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @RequestBody ReissueTokenRequest tokenRequest) {
        //유저 객체 정보를 이용하여 토큰 발행
        UserResponse user = UserResponse.of(userDetails.getUser());
        return jwtProvider.reissueAtk(user.getEmail(), user.getRole(), tokenRequest.getRefreshToken());
    }

    @GetMapping("/details/{userId}")
    public ResponseEntity<UserDto> getUserDetails(@PathVariable Long userId) {
        User currentUser = userService.getCurrentUser();
        //현재 로그인한 사용자의 정보가 없을 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        //사용자 정보 조회 후 UserDTO 반환
        UserDto userDetails = userService.getUserDetails(userId);
        return ResponseEntity.ok(userDetails);

    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @ModelAttribute UserUpdateRequest request, HttpServletRequest httpRequest) {
        User currentUser = userService.getCurrentUser();

        // 현재 로그인한 사용자의 권한을 확인하고 수정하려는 사용자 정보의 소유자인지 검증할 수 있습니다.
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            UserDto updatedUserDto  = userService.updateUser(userId,request, httpRequest);
            return ResponseEntity.ok(updatedUserDto);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);


        }
    }
}




