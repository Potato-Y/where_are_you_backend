package com.potato_y.timely.authentication.domain.oauth;

public interface OAuthLoginParams {

  OAuthProvider oAuthProvider();

  String getCode();
}
