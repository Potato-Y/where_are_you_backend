package com.potato_y.where_are_you.firebase;

import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.firebase.domain.FcmToken;
import com.potato_y.where_are_you.firebase.domain.FcmTokenRepository;
import com.potato_y.where_are_you.firebase.dto.FcmTokenRequest;
import com.potato_y.where_are_you.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirebaseServiceTest {

  @InjectMocks
  private FirebaseService firebaseService;

  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = createUser("test@mail.com", "test user", "1");
  }

  @Test
  @DisplayName("saveOrUpdateFcmToken(): 토큰을 저장할 수 있다.")
  void successSaveOrUpdateFcmToken_update() {
    String fcmToken = "new_fcm_token";
    FcmToken token = new FcmToken(testUser, "old_token");

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(fcmTokenRepository.findByUser(testUser)).willReturn(Optional.of(token));

    firebaseService.saveOrUpdateFcmToken(new FcmTokenRequest(fcmToken));

    verify(fcmTokenRepository).findByUser(testUser);
  }

  @Test
  @DisplayName("saveOrUpdateFcmToken(): 토큰을 저장할 수 있다.")
  void successSaveOrUpdateFcmToken_save() {
    String fcmToken = "newFcmToken";

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(fcmTokenRepository.findByUser(testUser)).willReturn(Optional.empty());

    firebaseService.saveOrUpdateFcmToken(new FcmTokenRequest(fcmToken));

    verify(fcmTokenRepository).findByUser(testUser);
    verify(fcmTokenRepository).save(any(FcmToken.class));
  }
}