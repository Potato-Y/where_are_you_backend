package com.potato_y.where_are_you.authentication.domain.oauth;

public interface OAuthLoginParams {

  OAuthProvider oAuthProvider();

  String getCode();
}
