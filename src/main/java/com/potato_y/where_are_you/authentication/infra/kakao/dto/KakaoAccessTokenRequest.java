package com.potato_y.where_are_you.authentication.infra.kakao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KakaoAccessTokenRequest {

  @NotBlank
  private String accessToken;
}
