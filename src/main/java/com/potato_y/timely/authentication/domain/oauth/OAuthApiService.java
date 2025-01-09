package com.potato_y.timely.authentication.domain.oauth;

public interface OAuthApiService {

  OAuthProvider oAuthProvider();

  String requestAccessToken(OAuthLoginParams params);

  OAuthInfoResponse requestOAuthInfo(String accessToken);
}
