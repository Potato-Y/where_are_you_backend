package com.potato_y.where_are_you.user;

import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserLate;
import com.potato_y.where_are_you.user.domain.UserLateRepository;
import com.potato_y.where_are_you.user.domain.UserRepository;
import com.potato_y.where_are_you.user.dto.UserLateRequest;
import java.util.Optional;
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

  @Mock
  private UserLateRepository userLateRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

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

  @Test
  @DisplayName("updateUserLate(): 사용자 지각 정보를 추가할 수 있다 - 업데이트, 지각인 경우")
  void successUpdateUserLate_update_lateTrue() {
    User testUser = createUser("test@mail.com", "test user", "1");
    UserLate userLate = UserLate.builder().user(testUser).build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(userLateRepository.findByUser(any(User.class))).willReturn(Optional.of(userLate));

    UserLate result = userService.updateUserLate(new UserLateRequest(true));

    assertThat(result.getUser().getEmail()).isEqualTo(testUser.getEmail());
    assertThat(result.getParticipationCount()).isEqualTo(1L);
    assertThat(result.getLateCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("updateUserLate(): 사용자 지각 정보를 추가할 수 있다 - 업데이트, 지각이 아닌 경우")
  void successUpdateUserLate_update_lateFalse() {
    User testUser = createUser("test@mail.com", "test user", "1");
    UserLate userLate = UserLate.builder().user(testUser).build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(userLateRepository.findByUser(any(User.class))).willReturn(Optional.of(userLate));

    UserLate result = userService.updateUserLate(new UserLateRequest(false));

    assertThat(result.getUser().getEmail()).isEqualTo(testUser.getEmail());
    assertThat(result.getParticipationCount()).isEqualTo(1L);
    assertThat(result.getLateCount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("updateUserLate(): 사용자 지각 정보를 추가할 수 있다 - 생성")
  void successUpdateUserLate_new() {
    User testUser = createUser("test@mail.com", "test user", "1");
    UserLate userLate = UserLate.builder().user(testUser).build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(userLateRepository.findByUser(any(User.class))).willReturn(Optional.empty());
    given(userLateRepository.save(any(UserLate.class))).willReturn(userLate);

    UserLate result = userService.updateUserLate(new UserLateRequest(true));

    assertThat(result.getUser().getEmail()).isEqualTo(testUser.getEmail());
    assertThat(result.getParticipationCount()).isEqualTo(1L);
    assertThat(result.getLateCount()).isEqualTo(1L);
  }
}