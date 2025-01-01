package com.potato_y.where_are_you.authentication.token.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class TokenDto {

  @Getter
  @Setter
  public static class RefreshTokenRequest {

    @NotBlank
    private String refreshToken;
  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class AllTokenResponse {

    private String accessToken;
    private String refreshToken;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class AccessTokenResponse {

    private String accessToken;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class RefreshTokenResponse {

    private String refreshToken;
  }
}
