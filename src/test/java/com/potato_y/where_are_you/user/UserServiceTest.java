package com.potato_y.where_are_you.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.potato_y.where_are_you.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("createUser(): OAuth 사용자를 추가할 수 있다.")
  void successCreateUser_OAuth() {
    // given
    String email = "test@mail.com";
    String nickname = "test user";
    String serviceId = "2";
    OAuthProvider oAuthProvider = OAuthProvider.KAKAO;

    User user = User.builder()
        .email(email)
        .nickname(nickname)
        .serviceId(serviceId)
        .password("pwd")
        .oAuthProvider(OAuthProvider.KAKAO)
        .build();

    OAuthInfoResponse oAuthInfoResponse = new OAuthInfoResponse() {
      @Override
      public String getId() {
        return serviceId;
      }

      @Override
      public String getEmail() {
        return email;
      }

      @Override
      public String getNickname() {
        return nickname;
      }

      @Override
      public OAuthProvider getOAuthProvider() {
        return oAuthProvider;
      }
    };

    given(userRepository.save(any(User.class))).willReturn(user);

    User response = userService.createUser(oAuthInfoResponse);

    assertThat(response.getServiceId()).isEqualTo(serviceId);
    assertThat(response.getEmail()).isEqualTo(email);
    assertThat(response.getNickname()).isEqualTo(nickname);
    assertThat(response.getPassword()).isNotEmpty();
  }
}