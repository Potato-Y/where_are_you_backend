package com.potato_y.where_are_you.firebase;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.firebase.domain.FcmToken;
import com.potato_y.where_are_you.firebase.domain.FcmTokenRepository;
import com.potato_y.where_are_you.firebase.dto.FcmTokenRequest;
import com.potato_y.where_are_you.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmService {

  private final FcmTokenRepository fcmTokenRepository;
  private final CurrentUserProvider currentUserProvider;

  @Transactional
  public void saveOrUpdateFcmToken(FcmTokenRequest dto) {
    User user = currentUserProvider.getCurrentUser();

    fcmTokenRepository.findByUser(user).ifPresentOrElse(
        fcmToken -> updateFcmToken(fcmToken, dto.token()),
        () -> saveFcmToken(user, dto.token())
    );
  }

  private void updateFcmToken(FcmToken fcmToken, String token) {
    fcmToken.updateToken(token);
  }

  private void saveFcmToken(User user, String token) {
    fcmTokenRepository.save(FcmToken.builder()
        .user(user)
        .token(token)
        .build());
  }
}
