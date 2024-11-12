package com.potato_y.where_are_you.location;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.location.domain.UserLocation;
import com.potato_y.where_are_you.location.domain.UserLocationRepository;
import com.potato_y.where_are_you.location.dto.UpdateUserLocationRequest;
import com.potato_y.where_are_you.location.dto.UserLocationResponse;
import com.potato_y.where_are_you.schedule.GroupScheduleService;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.user.domain.User;
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
  public List<UserLocationResponse> getScheduleMemberLocations(Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();

    GroupSchedule schedule = groupScheduleService.getSchedule(scheduleId);
    validateParticipation(user, schedule);
    validateLocationShareTime(schedule);

    return getUserLocationResponses(schedule);
  }

  private List<UserLocationResponse> getUserLocationResponses(GroupSchedule schedule) {
    List<User> users = groupScheduleService.getParticipationUsers(schedule);

    return users.stream()
        .map(userLocationRepository::findByUser)
        .flatMap(Optional::stream) // Optional에서 값이 있는 경우에만
        .filter(it -> {
          LocalDateTime updateTime = it.getUpdateAt();
          LocalDateTime shareRuleTime = updateTime.plusMinutes(LOCATION_SHARE_DURATION_MINUTES);

          return LocalDateTime.now().isBefore(shareRuleTime);
        })
        .map(UserLocationResponse::new)
        .toList();
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
    LocalDateTime shareStartTime = startTime.minusHours(schedule.getAlarmBeforeHours());

    return LocalDateTime.now().isAfter(shareStartTime);
  }
}
