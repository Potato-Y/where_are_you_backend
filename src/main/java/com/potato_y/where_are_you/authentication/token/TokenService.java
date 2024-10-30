package com.potato_y.where_are_you.authentication.token;

import com.potato_y.where_are_you.authentication.token.dto.TokenDto.AccessTokenResponse;
import com.potato_y.where_are_you.authentication.token.dto.TokenDto.AllTokenResponse;
import com.potato_y.where_are_you.config.jwt.TokenProvider;
import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.user.UserService;
import com.potato_y.where_are_you.user.domain.User;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

  private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(30);
  private static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

  private final TokenProvider tokenProvider;
  private final UserService userService;

  public AllTokenResponse createNewTokenSet(Long userId) {
    User user = userService.findById(userId);

    // refresh token 생성
    String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
    // access token 생성
    String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

    return new AllTokenResponse(accessToken, refreshToken);
  }

  public AccessTokenResponse updateAccessToken(String refreshToken) {
    // 토큰 유효성 검사에 실패하면 예외 발생
    if (!tokenProvider.validToken(refreshToken)) {
      throw new IllegalArgumentException("Unexpected token");
    }

    Long userId = tokenProvider.getUserId(refreshToken);
    User user = userService.findById(userId);

    String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

    return new AccessTokenResponse(accessToken);
  }

  public AllTokenResponse updateNewTokenSet(String refreshToken) {
    tokenProvider.validToken(refreshToken);

    if (tokenProvider.isRefreshTokenExpiringSoon(refreshToken)) {
      Long userId = tokenProvider.getUserId(refreshToken);

      return createNewTokenSet(userId);
    } else {
      throw new BadRequestException("토큰 만료일 예정이 아닙니다.");
    }
  }
}
