package com.potato_y.timely.authentication.infra.kakao.dto;

import com.potato_y.timely.authentication.domain.oauth.OAuthLoginParams;
import com.potato_y.timely.authentication.domain.oauth.OAuthProvider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoLoginParams implements OAuthLoginParams {

  private String code;

  @Override
  public OAuthProvider oAuthProvider() {
    return OAuthProvider.KAKAO;
  }
}
