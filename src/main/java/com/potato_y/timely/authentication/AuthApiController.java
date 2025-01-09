package com.potato_y.timely.authentication;

import com.potato_y.timely.authentication.infra.kakao.dto.KakaoAccessTokenRequest;
import com.potato_y.timely.authentication.infra.kakao.dto.KakaoLoginParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthApiController {

  private final OAuthLoginService oAuthLoginService;

  @PostMapping("/mobile/kakao")
  public ResponseEntity<?> loginKakaoWithMobile(@RequestBody KakaoAccessTokenRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(oAuthLoginService.loginKakaoMobile(request.getAccessToken()));
  }

  @GetMapping("/callback/kakao")
  public ResponseEntity<?> loginKakaoWithWeb(KakaoLoginParams params) {
    return ResponseEntity.status(HttpStatus.OK).body(oAuthLoginService.login(params));
  }
}
