package com.potato_y.where_are_you.authentication.domain.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RequestOAuthInfoService {

  private final Map<OAuthProvider, OAuthApiService> clients;

  public RequestOAuthInfoService(List<OAuthApiService> clients) {
    this.clients = clients.stream().collect(
        Collectors.toUnmodifiableMap(OAuthApiService::oAuthProvider, Function.identity())
    );
  }

  public OAuthInfoResponse request(OAuthLoginParams params) {
    OAuthApiService oAuthApiService = clients.get(params.oAuthProvider());
    String accessToken = oAuthApiService.requestAccessToken(params);

    return oAuthApiService.requestOAuthInfo(accessToken);
  }
}
