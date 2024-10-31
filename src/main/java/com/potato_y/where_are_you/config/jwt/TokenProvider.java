package com.potato_y.where_are_you.config.jwt;

import com.potato_y.where_are_you.user.UserDetailService;
import com.potato_y.where_are_you.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenProvider {

  private static final Duration TOKEN_EXPIRATION_THRESHOLD = Duration.ofDays(7);

  private final JwtProperties jwtProperties;
  private final UserDetailService userDetailService;

  /**
   * JWT 토큰 생성
   *
   * @param user      user 객체
   * @param expiredAt 유효 시간
   * @return Token
   */
  public String generateToken(User user, Duration expiredAt, TokenType tokenType) {
    Date now = new Date();
    return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user, tokenType);
  }

  /**
   * JWT 토큰을 만들어 반환
   *
   * @param expiry 유효 기간
   * @param user   유저 객체
   * @return Token
   */
  private String makeToken(Date expiry, User user, TokenType tokenType) {
    Date now = new Date();

    return Jwts.builder().setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 type: JWT
        // 내용 iss: propertise에서 가져온 값
        .setIssuer(jwtProperties.getIssuer()).setIssuedAt(now) // 내용 isa: 현재 시간
        .setExpiration(expiry) // 내용 exp: expiry 멤버 변수값
        .setSubject(user.getId().toString()) // 내용 sub: User id
        .claim("email", user.getEmail()) // 클래임 id: User email
        .claim("nickname", user.getNickname())
        .claim("token_type", tokenType.getType())
        // 서명: 비밀값과 함께 해시값을 HS256 방식으로 암호화
        .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey()).compact();
  }

  /**
   * 유효한 토큰인지 확인
   *
   * @param token     Token
   * @param tokenType Token 형식
   * @return boolean true: 검증 성공, false: 검증 실패
   */
  public boolean validToken(String token, TokenType tokenType) {
    try {
      Claims claims = getClaims(token);
      String type = (String) claims.getOrDefault("token_type", "");

      if (!type.equals(tokenType.getType())) {
        throw new RuntimeException("잘못된 Access Token");
      }

      return true;
    } catch (Exception e) { // 복호화 과정에서 오류가 발생할 경우 false 반환
      log.warn("validToken. Token 검증 실패. token={}", token);
      return false;
    }
  }

  /**
   * 토큰 기반으로 인증 정보를 가져오는 메서드
   *
   * @param token Token
   * @return User 인증 정보
   */
  public Authentication getAuthentication(String token) {
    Claims claims = getClaims(token);
    Set<SimpleGrantedAuthority> authorities = Collections.singleton(
        new SimpleGrantedAuthority("ROLE_USER"));

    Long userId = Long.parseLong(claims.getSubject()); // 토큰에서 subject를 userId로 사용
    UserDetails userDetails = userDetailService.loadUserById(userId); // ID로 사용자 조회

    return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
  }

  /**
   * 토큰 기반으로 유저 ID를 가져오는 메서드
   *
   * @param token Token
   * @return (Long) user_id
   */
  public Long getUserId(String token) {
    Claims claims = getClaims(token);
    return Long.parseLong(claims.getSubject());
  }

  /**
   * 토큰 만료 예정일인지 검증
   *
   * @param token RefreshToken
   * @return 토큰 만료 예정 여부
   */
  public boolean isRefreshTokenExpiringSoon(String token) {
    Duration remainingValidity = getRemainingValidity(token);
    return remainingValidity.compareTo(TOKEN_EXPIRATION_THRESHOLD) < 0;
  }

  /**
   * 토큰의 남은 유효 기간을 반환
   *
   * @param token Token
   * @return Duration 남은 유효 기간
   */
  private Duration getRemainingValidity(String token) {
    Claims claims = getClaims(token);
    Date expiration = claims.getExpiration();
    Date now = new Date();

    if (expiration.after(now)) {
      long remainingMillis = expiration.getTime() - now.getTime();
      return Duration.ofMillis(remainingMillis);
    } else {
      return Duration.ZERO; // 토큰이 이미 만료된 경우
    }
  }

  /**
   * 토큰에서 body 부분 추출
   *
   * @param token Token
   * @return Claims
   */
  private Claims getClaims(String token) {
    return Jwts.parser() // 클레임 조회
        .setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token).getBody();
  }
}
