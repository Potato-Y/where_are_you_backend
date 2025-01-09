package com.potato_y.timely.user;

import com.potato_y.timely.authentication.domain.oauth.OAuthProvider;
import com.potato_y.timely.user.domain.User;

public class UserTestUtils {

  public static User createUser(String email, String nickname, String serviceId) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .providerAccountId(serviceId)
        .password("test_password")
        .oauthProvider(OAuthProvider.KAKAO)
        .build();
  }
}
