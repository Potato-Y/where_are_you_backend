package com.potato_y.where_are_you.authentication;

import com.potato_y.where_are_you.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthLoginParams;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.authentication.domain.oauth.RequestOAuthInfoService;
import com.potato_y.where_are_you.authentication.token.TokenService;
import com.potato_y.where_are_you.authentication.token.dto.TokenDto.AllTokenResponse;
import com.potato_y.where_are_you.user.UserService;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

  private final UserService userService;
  private final TokenService tokenService;
  private final UserRepository userRepository;
  private final RequestOAuthInfoService requestOAuthInfoService;

  public AllTokenResponse login(OAuthLoginParams params) {
    OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
    Long userId = findOrCreateUser(oAuthInfoResponse);

    return tokenService.createNewTokenSet(userId);
  }

  public AllTokenResponse loginKakaoMobile(String accessToken) {
    OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService
        .request(OAuthProvider.KAKAO, accessToken);
    Long userId = findOrCreateUser(oAuthInfoResponse);

    return tokenService.createNewTokenSet(userId);
  }

  private Long findOrCreateUser(OAuthInfoResponse oAuthInfoResponse) {
    return userRepository.findByServiceId(oAuthInfoResponse.getId())
        .map(User::getId)
        .orElseGet(() -> userService.createUser(oAuthInfoResponse).getId());
  }
}
