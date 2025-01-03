package com.potato_y.where_are_you.location;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.location.domain.UserLocation;
import com.potato_y.where_are_you.location.domain.UserLocationRepository;
import com.potato_y.where_are_you.location.dto.StateMessage.StateMessageRequest;
import com.potato_y.where_are_you.location.dto.UpdateUserLocationRequest;
import com.potato_y.where_are_you.schedule.GroupScheduleService;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.user.domain.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationShareServiceTest {

  @InjectMocks
  private LocationShareService locationShareService;

  @Mock
  private GroupScheduleService groupScheduleService;

  @Mock
  private UserLocationRepository userLocationRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  private User testUser;

  private Group testGroup;

  @BeforeEach
  void setUp() {
    testUser = createUser("test@mail.com", "test user", "1");
    testGroup = createGroup("group", testUser);
  }

  @Test
  @DisplayName("updateUserLocation(): 사용자의 위치를 저장하고 업데이트 한다.")
  void successUpdateUserLocation_newLocation() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(30))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(123.2, 456.1);

    final var userLocation = Mockito.spy(UserLocation.builder()
        .user(testUser)
        .locationLatitude(request.locationLatitude())
        .locationLongitude(request.locationLongitude())
        .build());

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);
    given(userLocationRepository.findByUser(testUser)).willReturn(Optional.empty());
    given(userLocationRepository.save(any(UserLocation.class))).willReturn(userLocation);

    locationShareService.updateUserLocation(scheduleId, request);

    then(userLocationRepository).should(times(1)).save(any(UserLocation.class));
    verify(userLocation, times(1)).updateLocation(request.locationLatitude(),
        request.locationLongitude());
  }

  @Test
  @DisplayName("updateUserLocation(): 사용자의 위치를 업데이트 한다.")
  void successUpdateUserLocation_updateLocation() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();
    final UserLocation userLocation = Mockito.spy(UserLocation.builder()
        .user(testUser)
        .locationLatitude(1.0)
        .locationLongitude(2.0)
        .build());

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(123.2, 456.1);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);
    given(userLocationRepository.findByUser(testUser)).willReturn(Optional.of(userLocation));

    locationShareService.updateUserLocation(scheduleId, request);

    then(userLocationRepository).should(never()).save(any(UserLocation.class));
    verify(userLocation, times(1)).updateLocation(request.locationLatitude(),
        request.locationLongitude());
  }

  @Test
  @DisplayName("updateUserLocation(): 공유 시간이 아니라면 예외가 발생한다. - 31분 전")
  void failUpdateUserLocation_notShareTime_overBefore() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(31))
        .endTime(LocalDateTime.now().plusDays(26))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(123.2, 456.1);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);

    assertThatThrownBy(
        () -> locationShareService.updateUserLocation(scheduleId, request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("updateUserLocation(): 공유 시간이 아니라면 예외가 발생한다. - 일정 시작 시각")
  void failUpdateUserLocation_notShareTime_startTime() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now())
        .endTime(LocalDateTime.now().plusDays(26))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(123.2, 456.1);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);

    assertThatThrownBy(
        () -> locationShareService.updateUserLocation(scheduleId, request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("updateUserLocation(): 참여자가 아니라면 예외가 발생한다.")
  void failUpdateUserLocation_notParticipation() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(123.2, 456.1);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(false);

    assertThatThrownBy(
        () -> locationShareService.updateUserLocation(scheduleId, request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("getScheduleMemberLocations(): 공유 시간이 아니라면 예외가 발생한다.")
  void failGetScheduleMemberLocations_notShareTime() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusDays(25))
        .endTime(LocalDateTime.now().plusDays(26))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);

    assertThatThrownBy(
        () -> locationShareService.getScheduleMemberLocations(scheduleId))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("getScheduleMemberLocations(): 참여자가 아니라면 예외가 발생한다.")
  void failGetScheduleMemberLocations() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(false);

    assertThatThrownBy(
        () -> locationShareService.getScheduleMemberLocations(scheduleId))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("updateStateMessage(): 사용자의 빈 위치 정보를 저장하고 상태 메시지를 업데이트 한다")
  void successUpdateStateMessage_newLocation() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    StateMessageRequest request = new StateMessageRequest("상태 메시지");

    final var userLocation = Mockito.spy(UserLocation.builder()
        .user(testUser)
        .build());

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);
    given(userLocationRepository.findByUser(testUser)).willReturn(Optional.empty());
    given(userLocationRepository.save(any(UserLocation.class))).willReturn(userLocation);

    locationShareService.updateStateMessage(scheduleId, request);

    then(userLocationRepository).should(times(1)).save(any(UserLocation.class));
    verify(userLocation, times(1)).updateStateMessage(request.message());
  }

  @Test
  @DisplayName("updateStateMessage(): 사용자의 빈 위치 정보를 저장하고 상태 메시지를 업데이트 한다")
  void successUpdateStateMessage_updateLocation() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    StateMessageRequest request = new StateMessageRequest("상태 메시지");

    final var userLocation = Mockito.spy(UserLocation.builder()
        .user(testUser)
        .build());

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);
    given(userLocationRepository.findByUser(testUser)).willReturn(Optional.of(userLocation));

    locationShareService.updateStateMessage(scheduleId, request);

    verify(userLocation, times(1)).updateStateMessage(request.message());
  }

  @Test
  @DisplayName("updateStateMessage(): 공유 시간이 아니라면 예외가 발생한다.")
  void failUpdateStateMessage_notShareTime() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusDays(25))
        .endTime(LocalDateTime.now().plusDays(26))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    StateMessageRequest request = new StateMessageRequest("변경 메시지");

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(true);

    assertThatThrownBy(
        () -> locationShareService.updateStateMessage(scheduleId, request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("updateStateMessage(): 참여자가 아니라면 예외가 발생한다.")
  void failUpdateStateMessage_notParticipation() {
    final var scheduleId = 1L;
    GroupSchedule schedule = GroupSchedule.builder()
        .title("스케줄")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .user(testUser)
        .group(testGroup)
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .build();

    StateMessageRequest request = new StateMessageRequest("변경 메시지");

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupScheduleService.getSchedule(scheduleId)).willReturn(schedule);
    given(groupScheduleService.checkParticipation(testUser, schedule)).willReturn(false);

    assertThatThrownBy(
        () -> locationShareService.updateStateMessage(scheduleId, request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("resetUserLocation(): 참여자가 아니라면 예외가 발생한다.")
  void successResetUserLocation() {
    final var userLocation = Mockito.spy(UserLocation.builder()
        .user(testUser)
        .locationLatitude(1.1)
        .locationLongitude(1.2)
        .build());

    given(userLocationRepository.findByUser(testUser)).willReturn(Optional.of(userLocation));

    locationShareService.resetUserLocation(testUser);

    verify(userLocation, times(1)).updateLocation(null, null);
    verify(userLocation, times(1)).updateStateMessage(null);

    assertThat(userLocation.getLocationLatitude()).isNull();
    assertThat(userLocation.getLocationLongitude()).isNull();
    assertThat(userLocation.getStateMessage()).isNull();
  }
}
