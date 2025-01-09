package com.potato_y.where_are_you.authentication.domain.oauth;

public interface OAuthApiService {

  OAuthProvider oAuthProvider();

  String requestAccessToken(OAuthLoginParams params);

  OAuthInfoResponse requestOAuthInfo(String accessToken);
}
