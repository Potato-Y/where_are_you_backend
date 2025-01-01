package com.potato_y.where_are_you.user;

import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.user.domain.User;

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
