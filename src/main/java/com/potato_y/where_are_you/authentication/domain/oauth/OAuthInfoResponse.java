package com.potato_y.where_are_you.authentication.domain.oauth;

public interface OAuthInfoResponse {

  String getId();

  String getEmail();

  String getNickname();

  OAuthProvider getOAuthProvider();
}
