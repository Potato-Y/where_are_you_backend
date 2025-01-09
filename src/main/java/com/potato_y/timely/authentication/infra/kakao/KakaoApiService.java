package com.potato_y.timely.authentication.infra.kakao;

import com.potato_y.timely.authentication.domain.oauth.OAuthApiService;
import com.potato_y.timely.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.timely.authentication.domain.oauth.OAuthLoginParams;
import com.potato_y.timely.authentication.domain.oauth.OAuthProvider;
import com.potato_y.timely.authentication.infra.kakao.dto.KakaoInfoResponse;
import com.potato_y.timely.authentication.infra.kakao.dto.KakaoTokenResponse;
import com.potato_y.timely.error.exception.InternalServerErrorException;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KakaoApiService implements OAuthApiService {

  private static final String GRANT_TYPE = "authorization_code";

  @Value("${oauth.kakao.url.auth}")
  private String authUrl;

  @Value("${oauth.kakao.url.api}")
  private String apiUrl;

  @Value("${oauth.kakao.client-id}")
  private String clientId;

  @Override
  public OAuthProvider oAuthProvider() {
    return OAuthProvider.KAKAO;
  }

  @Override
  public String requestAccessToken(OAuthLoginParams params) {
    KakaoTokenResponse dto = WebClient.create(authUrl).post()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .path("/oauth/token")
            .queryParam("grant_type", GRANT_TYPE)
            .queryParam("client_id", clientId)
            .queryParam("code", params.getCode())
            .build())
        .header(HttpHeaders.CONTENT_TYPE,
            HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
        .retrieve()
        // TODO : Custom Exception
        .onStatus(HttpStatusCode::is4xxClientError,
            clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
        .onStatus(HttpStatusCode::is5xxServerError,
            clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
        .bodyToMono(KakaoTokenResponse.class)
        .block();

    if (dto == null) {
      throw new InternalServerErrorException("KakaoTokenResponse가 비어있습니다.");
    }

    return dto.getAccessToken();
  }

  @Override
  public OAuthInfoResponse requestOAuthInfo(String accessToken) {
    KakaoInfoResponse userInfo = WebClient.create(apiUrl).get()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .path("/v2/user/me")
            .build(true))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
        .header(HttpHeaders.CONTENT_TYPE,
            HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
        .retrieve()
        // TODO : Custom Exception
        .onStatus(HttpStatusCode::is4xxClientError,
            clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
        .onStatus(HttpStatusCode::is5xxServerError,
            clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
        .bodyToMono(KakaoInfoResponse.class)
        .block();

    return userInfo;
  }
}
