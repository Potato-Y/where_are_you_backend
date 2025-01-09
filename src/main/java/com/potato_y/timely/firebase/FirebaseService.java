package com.potato_y.where_are_you.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.firebase.domain.FcmChannelId;
import com.potato_y.where_are_you.firebase.domain.FcmToken;
import com.potato_y.where_are_you.firebase.domain.FcmTokenRepository;
import com.potato_y.where_are_you.firebase.dto.FcmTokenRequest;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.user.domain.User;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

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

  @Transactional
  public void pushFcmNotificationForSchedule(List<User> users, GroupSchedule schedule,
      FcmChannelId channelId) {
    users.forEach(it -> {
      fcmTokenRepository.findByUser(it).ifPresent(token -> {
        try {
          FirebaseMessaging.getInstance().send(
              Message.builder()
                  .setToken(token.getToken())
                  .putData("groupId", schedule.getGroup().getId().toString())
                  .putData("groupName", schedule.getGroup().getGroupName())
                  .putData("scheduleId", schedule.getId().toString())
                  .putData("scheduleTitle", schedule.getTitle())
                  .putData("scheduleStartTime", schedule.getStartTime().toString())
                  .putData("channelId", channelId.getValue())
                  .build());
        } catch (FirebaseMessagingException e) {
          log.warn(e.getMessage());
        }
      });
    });
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
