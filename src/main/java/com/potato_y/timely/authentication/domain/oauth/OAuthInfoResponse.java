package com.potato_y.timely.authentication.domain.oauth;

public interface OAuthInfoResponse {

  String getId();

  String getEmail();

  String getNickname();

  OAuthProvider getOAuthProvider();
}
