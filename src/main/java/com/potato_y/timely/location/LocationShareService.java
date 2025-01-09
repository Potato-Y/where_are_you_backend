package com.potato_y.timely.location;

import com.potato_y.timely.authentication.CurrentUserProvider;
import com.potato_y.timely.common.constants.Number;
import com.potato_y.timely.error.exception.BadRequestException;
import com.potato_y.timely.location.domain.UserLocation;
import com.potato_y.timely.location.domain.UserLocationRepository;
import com.potato_y.timely.location.dto.ShareLocationResponse;
import com.potato_y.timely.location.dto.StateMessage.StateMessageRequest;
import com.potato_y.timely.location.dto.UpdateUserLocationRequest;
import com.potato_y.timely.schedule.GroupScheduleService;
import com.potato_y.timely.schedule.domain.GroupSchedule;
import com.potato_y.timely.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationShareService {

  private static final int LOCATION_SHARE_DURATION_MINUTES = 5;
  private static final int SHARE_TIME_THRESHOLD_MINUTES = 30;

  private final GroupScheduleService groupScheduleService;
  private final UserLocationRepository userLocationRepository;
  private final CurrentUserProvider currentUserProvider;

  @Transactional
  public void updateUserLocation(Long scheduleId, UpdateUserLocationRequest dto) {
    User user = currentUserProvider.getCurrentUser();

    GroupSchedule schedule = groupScheduleService.getSchedule(scheduleId);
    validateParticipation(user, schedule);
    validateLocationShareTime(schedule);

    UserLocation userLocation = userLocationRepository.findByUser(user)
        .orElseGet(() -> createUserLocation(user));

    userLocation.updateLocation(dto.locationLatitude(), dto.locationLongitude());
  }

  @Transactional(readOnly = true)
  public ShareLocationResponse getScheduleMemberLocations(Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();

    GroupSchedule schedule = groupScheduleService.getSchedule(scheduleId);
    validateParticipation(user, schedule);
    validateLocationShareTime(schedule);

    return new ShareLocationResponse(schedule, getUserLocation(schedule));
  }

  @Transactional
  public String updateStateMessage(Long scheduleId, StateMessageRequest request) {
    User user = currentUserProvider.getCurrentUser();

    GroupSchedule schedule = groupScheduleService.getSchedule(scheduleId);
    validateParticipation(user, schedule);
    validateLocationShareTime(schedule);

    UserLocation userLocation = userLocationRepository.findByUser(user)
        .orElseGet(() -> createUserLocation(user));

    userLocation.updateStateMessage(request.message());
    return userLocation.getStateMessage();
  }

  @Transactional
  public void resetUserLocation(User user) {
    userLocationRepository.findByUser(user).ifPresent(it -> it
        .updateLocation(null, null)
        .updateStateMessage(null));
  }

  private List<UserLocation> getUserLocation(GroupSchedule schedule) {
    List<User> users = groupScheduleService.getParticipationUsers(schedule);

    return users.stream()
        .map(userLocationRepository::findByUser)
        .flatMap(Optional::stream) // Optional에서 값이 있는 경우에만
        .filter(it -> {
          LocalDateTime updateTime = it.getUpdateAt();
          LocalDateTime shareRuleTime = updateTime.plusMinutes(LOCATION_SHARE_DURATION_MINUTES);

          return LocalDateTime.now().isBefore(shareRuleTime);
        }).toList();
  }

  private UserLocation createUserLocation(User user) {
    return userLocationRepository.save(UserLocation.builder().user(user).build());
  }

  private void validateParticipation(User user, GroupSchedule schedule) {
    if (!groupScheduleService.checkParticipation(user, schedule)) {
      throw new BadRequestException("위치 공유 대상자가 아닙니다");
    }
  }

  private void validateLocationShareTime(GroupSchedule schedule) {
    if (!checkLocationShareTime(schedule)) {
      throw new BadRequestException("위치 공유 시간이 아닙니다");
    }
  }

  private boolean checkLocationShareTime(GroupSchedule schedule) {
    LocalDateTime startTime = schedule.getStartTime();
    LocalDateTime shareStartTime = startTime
        .minusMinutes(SHARE_TIME_THRESHOLD_MINUTES)
        .minusNanos(Number.ONE.getValue());

    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(shareStartTime) && now.isBefore(startTime);
  }
}
