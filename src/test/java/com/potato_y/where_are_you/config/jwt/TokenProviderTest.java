package com.potato_y.where_are_you.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TokenProviderTest {

  @Autowired
  private TokenProvider tokenProvider;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JwtProperties jwtProperties;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }


  @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다. - access token")
  @Test
  void generateToken_accessToken() {
    // given 토큰에 유저 정보를 추가하기 위한 테스트 유저를 만든다.
    User testUser = userRepository.save(User.builder()
        .serviceId("1")
        .email("user@email.com")
        .nickname("test user")
        .build());

    // when 토큰 제공자의 generateToken() 메서드를 호출해 토큰을 만든다.
    String token = tokenProvider.generateToken(testUser, Duration.ofDays(1), TokenType.ACCESS);

    // then jjwt 라이브러리를 사용해 토큰을 복호화한다. 토큰을 만들 때 클레임으로 넣어둔 id 값이 given 절에서 만든 유저 id와
    // 동일한지 확인한다.
    Claims claims = Jwts.parser()
        .setSigningKey(jwtProperties.getSecretKey())
        .parseClaimsJws(token)
        .getBody();

    Long userId = Long.valueOf(claims.getSubject());
    String tokenType = (String) claims.get("token_type");

    assertThat(userId).isEqualTo(testUser.getId());
    assertThat(tokenType).isEqualTo(TokenType.ACCESS.getType());
  }

  @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다. - refresh token")
  @Test
  void generateToken_refreshToken() {
    // given 토큰에 유저 정보를 추가하기 위한 테스트 유저를 만든다.
    User testUser = userRepository.save(User.builder()
        .serviceId("1")
        .email("user@email.com")
        .nickname("test user")
        .build());

    // when 토큰 제공자의 generateToken() 메서드를 호출해 토큰을 만든다.
    String token = tokenProvider.generateToken(testUser, Duration.ofDays(14), TokenType.REFRESH);

    // then jjwt 라이브러리를 사용해 토큰을 복호화한다. 토큰을 만들 때 클레임으로 넣어둔 id 값이 given 절에서 만든 유저 id와
    // 동일한지 확인한다.
    Claims claims = Jwts.parser()
        .setSigningKey(jwtProperties.getSecretKey())
        .parseClaimsJws(token)
        .getBody();

    Long userId = Long.valueOf(claims.getSubject());
    String tokenType = (String) claims.get("token_type");

    assertThat(userId).isEqualTo(testUser.getId());
    assertThat(tokenType).isEqualTo(TokenType.REFRESH.getType());
  }

  @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
  @Test
  void validToken_invalidToken() {
    // given jjwt 라이브러리를 사용해 토큰을 생성한다. 이때 만료된 시간은 1970년 1월 1일부터 현재 시간을 밀리초 단위로 치환한
    // 값이다.
    // (new Date().getTime())에 1000을 빼, 이미 만료된 토큰으로 생성한다.
    String token =
        JwtFactory.builder()
            .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
            .build()
            .createToken(jwtProperties);

    // when 토큰 제공자의 validToken() 메서드를 호출해 토큰인지 검증한 뒤 결괏값을 치환받는다.
    boolean result = tokenProvider.validToken(token, TokenType.ACCESS);

    // then 반환값이 false(유효한 토큰이 아님)인 것을 확인한다.
    assertThat(result).isFalse();
  }

  @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다.")
  @Test
  void validToken_validToken() {
    // given jjwt 라이브러리를 사용해 토큰을 생성한다. 만료 시간은 현재 시간으로부터 14일 뒤로, 만료되지 않은 토큰으로 생성한다.
    String token = JwtFactory.builder()
        .expiration(new Date(new Date().getTime() + Duration.ofDays(1).toMillis()))
        .build()
        .createToken(jwtProperties);

    // when 토큰 제공자의 validToken() 메서드를 호출해 유효한 토큰인지 검증한 뒤 결괏값을 반환받는다.
    boolean result = tokenProvider.validToken(token, TokenType.ACCESS);

    // then 반환값이 true(유효한 토큰임)인 것을 확인한다.
    assertThat(result).isTrue();
  }


  @DisplayName("validToken(): 검증에 맞는 타입이 아니면 실패한다.")
  @Test
  void validToken_notMatchTokenType() {
    // given jjwt 라이브러리를 사용해 토큰을 생성한다. 만료 시간은 현재 시간으로부터 14일 뒤로, 만료되지 않은 토큰으로 생성한다.
    String validToken = JwtFactory.builder()
        .expiration(new Date(new Date().getTime() + Duration.ofDays(1).toMillis()))
        .build()
        .createToken(jwtProperties);
    String failToken = JwtFactory.builder()
        .expiration(new Date(new Date().getTime() + Duration.ofDays(1).toMillis()))
        .tokenType(TokenType.REFRESH)
        .build()
        .createToken(jwtProperties);

    // when 토큰 제공자의 validToken() 메서드를 호출해 유효한 토큰인지 검증한 뒤 결괏값을 반환받는다.
    boolean validResult = tokenProvider.validToken(validToken, TokenType.ACCESS);
    boolean failResult1 = tokenProvider.validToken(validToken, TokenType.REFRESH);
    boolean failResult2 = tokenProvider.validToken(failToken, TokenType.ACCESS);

    // then 반환값이 true(유효한 토큰임)인 것을 확인한다.
    assertThat(validResult).isTrue();
    assertThat(failResult1).isFalse();
    assertThat(failResult2).isFalse();
  }

  @DisplayName("getAuthentication(): 검증 테스트")
  @Test
  void getAuthentication() {
    // given jjwt 라이브러리를 사용해 토큰을 생성한다. 이때 토큰의 제목인 subject는 'user@emil.com'라는 값을
    // 사용한다.
    String userId = "1";
    String token = JwtFactory.builder().subject(userId).build().createToken(jwtProperties);
    userRepository.save(User.builder()
        .serviceId("1")
        .email("test@mail.com")
        .oAuthProvider(OAuthProvider.KAKAO)
        .nickname("test_user")
        .build());

    // when 토큰 제공자의 getAuthentication() 메서드를 호출해 인증 객체를 반환받는다.
    Authentication authentication = tokenProvider.getAuthentication(token);

    // then 반환받은 인증 객체의 유저 이름을 가져와 given 절에서 설정한 subject 값인 'user@email.com'과 같은지
    // 확인한다.
    assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userId);
  }

  @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
  @Test
  void getUserId() {
    // given jjwt 라이브러리를 통해 토큰을 생성한다. 이떄 클레임을 추가한다. 키는 "id", 값은 1이라는 유저 Id이다.
    Long userId = 1L;
    String token =
        JwtFactory.builder().claims(Map.of("id", userId)).build().createToken(jwtProperties);

    // when 토큰 제공자의 getUserId() 메서드를 호출해 유저 id를 반환받는다.
    Long userIdByToken = tokenProvider.getUserId(token);

    // then 반환받은 유저 id가 given 절에서 설정한 유저 ID 값인 1과 같은지 확인한다.
    assertThat(userIdByToken).isEqualTo(userId);
  }

  @DisplayName("isRefreshTokenExpiringSoon(): refresh token 만료 예정 여부 테스트")
  @Test
  void successIsRefreshTokenExpiringSoon() {
    // given 만료 예정인 토큰 생성
    String expiringToken = JwtFactory.builder()
        .expiration(new Date(new Date().getTime() + Duration.ofDays(7).toMillis()))
        .tokenType(TokenType.REFRESH)
        .build()
        .createToken(jwtProperties);

    String validToken = JwtFactory.builder()
        .expiration(new Date(new Date().getTime() + Duration.ofDays(14).toMillis()))
        .tokenType(TokenType.REFRESH)
        .build()
        .createToken(jwtProperties);

    // when
    boolean isExpiringSoon = tokenProvider.isRefreshTokenExpiringSoon(expiringToken);
    boolean isNotExpiringSoon = tokenProvider.isRefreshTokenExpiringSoon(validToken);

    // then
    assertThat(isExpiringSoon).isTrue();
    assertThat(isNotExpiringSoon).isFalse();
  }

  @DisplayName("isRefreshTokenExpiringSoon(): 이미 만료된 토큰 테스트")
  @Test
  void failIsRefreshTokenExpiringSoon_expiredToken() {
    String expiredToken = JwtFactory.builder()
        .expiration(new Date(new Date().getTime() - Duration.ofDays(1).toMillis()))
        .build()
        .createToken(jwtProperties);

    assertThatThrownBy(() -> tokenProvider.isRefreshTokenExpiringSoon(expiredToken))
        .isInstanceOf(RuntimeException.class);
  }

  @DisplayName("isRefreshTokenExpiringSoon(): 유효하지 않은 토큰 테스트")
  @Test
  void failIsRefreshTokenExpiringSoon_invalidToken() {
    String token = "non_token";

    assertThatThrownBy(() -> tokenProvider.isRefreshTokenExpiringSoon(token))
        .isInstanceOf(RuntimeException.class);
  }
}
