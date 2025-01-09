package com.potato_y.where_are_you.authentication.token;

import com.potato_y.where_are_you.authentication.token.dto.TokenDto.AccessTokenResponse;
import com.potato_y.where_are_you.authentication.token.dto.TokenDto.AllTokenResponse;
import com.potato_y.where_are_you.authentication.token.dto.TokenDto.RefreshTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/token")
public class TokenApiController {

  private final TokenService tokenService;

  @PostMapping("") // refresh 토큰을 통해 access 토큰 재발급
  public ResponseEntity<AccessTokenResponse> getAccessToken(
      @RequestBody RefreshTokenRequest request) {
    AccessTokenResponse tokenResponse = tokenService.updateAccessToken(request.getRefreshToken());

    return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AllTokenResponse> getTokenSet(@RequestBody RefreshTokenRequest request) {
    AllTokenResponse tokenResponse = tokenService.updateNewTokenSet(request.getRefreshToken());

    return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
  }
}
