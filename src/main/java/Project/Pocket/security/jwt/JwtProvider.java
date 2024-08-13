package Project.Pocket.security.jwt;

import Project.Pocket.redis.RedisDao;
import Project.Pocket.security.dto.TokenResponse;
import Project.Pocket.security.exception.CustomException;
import Project.Pocket.security.exception.ExceptionStatus;
import Project.Pocket.user.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class JwtProvider {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    private static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    private static final String BEARER_PREFIX = "Bearer ";

    private static final long ACCESS_TOKEN_TIME = 1000 * 60 * 60L; // 1시간 1000ms(=1s) *60=(1min)*60=(1hour)
    private static final long REFRESH_TOKEN_TIME = 1000 * 60 * 60 * 24 * 7L;// 7일

    @Value("${jwt.secret.key}")
    private String secretKey;

    //HMAC-SHA 키를 생성
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;


    private final UserDetailsService userDetailsService;
    private final RedisDao redisDao;

    // 이 코드는 HMAC-SHA 키를 생성하는 데 사용되는 Base64 인코딩된 문자열을 디코딩하여 키를 초기화하는 용도로 사용
    @PostConstruct//의존성 주입이 이루어진 후 초기화를 수행하는 어노테이션
    public void init() {
        byte[] bytes = Base64.getDecoder()
                .decode(secretKey);// Base64로 인코딩된 값을 시크릿키 변수에 저장한 값을 디코딩하여 바이트 배열로 변환
        //* Base64 (64진법) : 바이너리(2진) 데이터를 문자 코드에 영향을 받지 않는 공통 ASCII문자로 표현하기 위해 만들어진 인코딩
        key = Keys.hmacShaKeyFor(
                bytes);//디코팅된 바이트 배열을 기반으로 HMAC-SHA 알고르즘을 사용해서 Key객체로 반환 , 이를 key 변수에 대입
    }

    /**
     * Header 에서 토큰 가져오기
     *
     * @param request
     * @return
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 토큰 생성 메서드 username 대신 email
     *
     * @param role
     * @param tokenExpireTime
     * @return Token
     */

    private String createToken(String email, UserRoleEnum role, Long tokenExpireTime) {
        Date date = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .claim(AUTHORIZATION_KEY, role)// JWT에 사용자 역할 정보를 클레임(claim)으로 추가합니다.
                .setSubject(email)//JWT의 주제(subject)를 사용자 이름으로 설정합니다.
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime() + tokenExpireTime))
                .signWith(key, SignatureAlgorithm.HS256) //: JWT에 서명을 추가합니다. key는 서명에 사용되는 비밀 키이며,
                .compact();
    }

    /**
     * 유저 로그인 후 토큰 발행 username 대신 email
     *
     * @param role
     * @return 에세스토큰과 리프레쉬토큰을 담은 DTO 반환
     */
    public TokenResponse createTokenByLogin(String email, UserRoleEnum role,Long userId) {
        String accessToken = createToken(email, role, ACCESS_TOKEN_TIME);
        String refreshToken = createToken(email, role, REFRESH_TOKEN_TIME);
        redisDao.setRefreshToken(email, refreshToken, REFRESH_TOKEN_TIME);
        return new TokenResponse(accessToken, refreshToken, userId);
    }

    //AccessToken 재발행 + refreshToken 함께 발행

    public TokenResponse reissueAtk(String email, UserRoleEnum role, String reToken,Long userId) {
        // 레디스 저장된 리프레쉬토큰값을 가져와서 입력된 reToken 같은지 유무 확인
        if (!redisDao.getRefreshToken(email).equals(reToken)) {
            throw new CustomException(
                    ExceptionStatus.AUTHENTICATION);
        }
        String accessToken = createToken(email, role, ACCESS_TOKEN_TIME);
        String refreshToken = createToken(email, role, REFRESH_TOKEN_TIME);
        redisDao.setRefreshToken(email, refreshToken, REFRESH_TOKEN_TIME);
        return new TokenResponse(accessToken, refreshToken,userId);
    }

    /**
     *  토큰으로 유저정보 가져오기
     * @param token
     * @return
     */
    public Claims getUserInfoFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }


    /**  Header에서 가져온 토큰 검증하는 메소드
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            //parser : parsing을 하는 도구. parsing : token에 내재된 자료 구조를 빌드하고 문법을 검사한다.
            //Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)는
            // 주어진 토큰을 파싱하기 위해 JWT 파서를 설정하고, 서명 키를 설정한 뒤, 토큰을 파싱하여 JWT 서명 검사를 수행합니다.
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 유효하면 true
        } catch (SecurityException | MalformedJwtException |
                 UnsupportedJwtException e) {// 전: 권한 없다면 발생 , 후: JWT가 올바르게 구성되지 않았다면 발생
            log.info("Invalid JWT token, 만료된 jwt 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    /**
     * 남은 에세스토큰의 만료시간 조회
     * @param accessToken
     * @return
     */
    public Long getExpiration(String accessToken){
        //에세스 토큰 만료시간
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody()
                .getExpiration();
        //현재시간
        long now = new Date().getTime();
        return (expiration.getTime()-now);
    }

    /**
     * 일반 유저 인증 객체 생성
     * @param email
     * @return
     */
    public Authentication createUserAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
