package Project.Pocket.user.controller;

import Project.Pocket.security.dto.ReissueTokenRequest;
import Project.Pocket.security.dto.TokenResponse;
import Project.Pocket.security.jwt.JwtProvider;
import Project.Pocket.security.service.UserDetailsImpl;
import Project.Pocket.user.dto.LoginRequest;
import Project.Pocket.user.dto.SignUpRequest;
import Project.Pocket.user.dto.UserResponse;
import Project.Pocket.user.dto.UserUpdateRequest;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.exception.UserNotFoundException;
import Project.Pocket.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입
     *
     * @param signUpRequest
     * @return 회원가입 성공
     */
    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Validated SignUpRequest signUpRequest) {
        //패스워드 1과 패스워드 2 가 동일 한지 체크
        if (!signUpRequest.getPassword().equals(signUpRequest.getPassword2())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
        }
        return userService.signup(signUpRequest);
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

    @GetMapping("/details")
    public User getUserDetails() {
        User currentUser = userService.getCurrentUser();
        //현재 로그인한 사용자의 정보가 없을 경우
        if (currentUser == null) {
            return null;
        }
        // 데이터베이스에서 사용자 정보를 다시 조회하여 반환
        User userDetails = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(userDetails).getBody();

    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        User currentUser = userService.getCurrentUser();

        // 현재 로그인한 사용자의 권한을 확인하고 수정하려는 사용자 정보의 소유자인지 검증할 수 있습니다.
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            userService.updateUser(userId, request);
            //업데이트된 사용자 정보를 다시 가져와 반환
            User updatedUser = userService.getUserById(userId);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}




