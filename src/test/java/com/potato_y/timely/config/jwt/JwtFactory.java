package com.potato_y.where_are_you.config.jwt;

import static java.util.Collections.emptyMap;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtFactory {

  private String subject = "1"; // user id가 사용됨
  private Date issuedAt = new Date();
  private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
  private Map<String, Object> claims = emptyMap();
  private TokenType tokenType = TokenType.ACCESS;

  // 빌더 패턴을 사용해 설정이 필요한 데이터만 선택 설정
  @Builder
  public JwtFactory(String subject, Date issuedAt, Date expiration, Map<String, Object> claims,
      TokenType tokenType) {
    this.subject = subject != null ? subject : this.subject;
    this.issuedAt = issuedAt != null ? issuedAt : this.issuedAt;
    this.expiration = expiration != null ? expiration : this.expiration;
    this.claims = claims != null ? claims : this.claims;
    this.tokenType = tokenType != null ? tokenType : this.tokenType;
  }

  public static JwtFactory withDefaultValues() {
    return JwtFactory.builder().build();
  }

  // jjwt 라이브러리를 사용해 JWT 토큰 생성
  public String createToken(JwtProperties jwtProperties) {
    return Jwts.builder()
        .setSubject(subject)
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
        .setIssuer(jwtProperties.getIssuer())
        .setExpiration(expiration)
        .addClaims(claims)
        .claim("token_type", tokenType.getType())
        .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
        .compact();
  }
}