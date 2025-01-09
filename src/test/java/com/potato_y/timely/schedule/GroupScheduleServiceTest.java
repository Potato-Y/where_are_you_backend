package com.potato_y.where_are_you.schedule;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.firebase.FirebaseService;
import com.potato_y.where_are_you.group.GroupService;
import com.potato_y.where_are_you.group.GroupValidator;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.schedule.domain.AlarmSchedule;
import com.potato_y.where_are_you.schedule.domain.AlarmScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.Participation;
import com.potato_y.where_are_you.schedule.domain.ParticipationRepository;
import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.schedule.dto.GetGroupScheduleListResponse;
import com.potato_y.where_are_you.schedule.dto.GroupScheduleResponse;
import com.potato_y.where_are_you.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
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
class GroupScheduleServiceTest {

  @InjectMocks
  private GroupScheduleService scheduleService;

  @Mock
  private CurrentUserProvider currentUserProvider;

  @Mock
  private GroupService groupService;

  @Mock
  private GroupScheduleRepository scheduleRepository;

  @Mock
  private FirebaseService firebaseService;

  @Mock
  private AlarmScheduleRepository alarmScheduleRepository;

  @Mock
  private ParticipationRepository participationRepository;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private ScheduleValidator scheduleValidator;

  private User testUser;

  private Group testGroup;

  @BeforeEach
  void setUp() {
    testUser = createUser("test@mail.com", "test user", "1");
    testGroup = createGroup("group", testUser);
    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
  }

  @Test
  @DisplayName("createSchedule(): 스케줄을 저장할 수 있다. - 알람 비활성화")
  void successCreateSchedule_notAlarm() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .isAlarmEnabled(false)
        .build();

    given(groupService.findByGroup(anyLong())).willReturn(testGroup);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(scheduleRepository.save(any(GroupSchedule.class))).willAnswer(
        invocation -> invocation.getArgument(0));
    given(groupService.getGroupMembers(any(Group.class))).willReturn(List.of());

    var request = new CreateGroupScheduleRequest(schedule.getTitle(), schedule.getStartTime(),
        schedule.getEndTime(), schedule.isAlarmEnabled(), schedule.getAlarmBeforeHours(),
        schedule.getLocation(), schedule.getLocationLatitude(), schedule.getLocationLongitude());

    GroupScheduleResponse response = scheduleService.createSchedule(1L, request);

    assertThat(response.title()).isEqualTo(schedule.getTitle());
    assertThat(response.startTime()).isEqualTo(schedule.getStartTime());
    assertThat(response.endTime()).isEqualTo(schedule.getEndTime());
    assertThat(response.isAlarmEnabled()).isFalse();
    assertThat(response.alarmBeforeHours()).isEqualTo(schedule.getAlarmBeforeHours());
    assertThat(response.location()).isEqualTo(schedule.getLocation());
    assertThat(response.locationLatitude()).isEqualTo(schedule.getLocationLatitude());
    assertThat(response.locationLongitude()).isEqualTo(schedule.getLocationLongitude());
  }

  @Test
  @DisplayName("createSchedule(): 스케줄을 저장할 수 있다. - 알람 활성화")
  void successCreateSchedule_enabledAlarm() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();

    given(groupService.findByGroup(anyLong())).willReturn(testGroup);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(scheduleRepository.save(any(GroupSchedule.class))).willAnswer(
        invocation -> invocation.getArgument(0));
    given(alarmScheduleRepository.save(any(AlarmSchedule.class))).willReturn(AlarmSchedule.builder()
        .build());
    given(groupService.getGroupMembers(any(Group.class))).willReturn(List.of());

    var request = new CreateGroupScheduleRequest(schedule.getTitle(), schedule.getStartTime(),
        schedule.getEndTime(), schedule.isAlarmEnabled(), schedule.getAlarmBeforeHours(),
        schedule.getLocation(), schedule.getLocationLatitude(), schedule.getLocationLongitude());

    GroupScheduleResponse response = scheduleService.createSchedule(1L, request);

    assertThat(response.title()).isEqualTo(schedule.getTitle());
    assertThat(response.startTime()).isEqualTo(schedule.getStartTime());
    assertThat(response.endTime()).isEqualTo(schedule.getEndTime());
    assertThat(response.isAlarmEnabled()).isTrue();
    assertThat(response.alarmBeforeHours()).isEqualTo(schedule.getAlarmBeforeHours());
    assertThat(response.location()).isEqualTo(schedule.getLocation());
    assertThat(response.locationLatitude()).isEqualTo(schedule.getLocationLatitude());
    assertThat(response.locationLongitude()).isEqualTo(schedule.getLocationLongitude());
  }

  @Test
  @DisplayName("createSchedule(): 그룹원이 아니라면 저장할 수 없다.")
  void failCreateSchedule_notGroupMember() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();

    given(groupService.findByGroup(anyLong())).willReturn(testGroup);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(false);

    var request = new CreateGroupScheduleRequest(schedule.getTitle(), schedule.getStartTime(),
        schedule.getEndTime(), schedule.isAlarmEnabled(), schedule.getAlarmBeforeHours(),
        schedule.getLocation(), schedule.getLocationLatitude(), schedule.getLocationLongitude());

    assertThatThrownBy(() -> scheduleService.createSchedule(1L, request))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("getGroupSchedules(): 스케줄 정보를 조회할 수 있다")
  void successGetGroupSchedules() {
    GroupSchedule schedule1 = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();
    GroupSchedule schedule2 = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 2")
        .build();

    Participation participation = Participation.builder().user(testUser).schedule(schedule1)
        .isParticipating(true).build();

    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(groupService.findByGroup(anyLong())).willReturn(testGroup);
    given(scheduleRepository.findByGroup(any(Group.class))).willReturn(
        List.of(schedule1, schedule2));
    given(participationRepository.findByUserAndSchedule(any(User.class),
        any(GroupSchedule.class))).willReturn(
        Optional.of(participation));

    List<GetGroupScheduleListResponse> responses = scheduleService.getGroupSchedules(1L);

    assertThat(responses.get(0).groupSchedule().title()).isEqualTo(schedule1.getTitle());
    assertThat(responses.get(0).isParticipating()).isEqualTo(participation.isParticipating());
    assertThat(responses.get(1).groupSchedule().title()).isEqualTo(schedule2.getTitle());
    assertThat(responses.get(1).isParticipating()).isEqualTo(participation.isParticipating());
  }

  @Test
  @DisplayName("getGroupSchedules(): 그룹원이 아니라면 예외가 발생한다")
  void failGetGroupSchedules_notMember() {
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(false);

    assertThatThrownBy(() -> scheduleService.getGroupSchedules(1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("registerParticipation(): 스케줄에 참여할 수 있다 - 참여 데이터가 없는 경우")
  void successRegisterParticipation_create() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();
    Participation participation = Participation.builder()
        .user(testUser)
        .schedule(schedule)
        .isParticipating(true)
        .build();

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    doNothing().when(groupValidator).groupId(testGroup, schedule.getId());
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    given(participationRepository.findByUserAndSchedule(any(User.class), any(GroupSchedule.class)))
        .willReturn(Optional.empty());
    given(participationRepository.save(any(Participation.class))).willReturn(participation);

    scheduleService.registerParticipation(null, 1L);
  }

  @Test
  @DisplayName("registerParticipation(): 스케줄에 참여할 수 있다 - 참여 데이터가 있는 경우")
  void successRegisterParticipation_update() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();
    Participation participation = Mockito.spy(Participation.builder()
        .user(testUser)
        .schedule(schedule)
        .isParticipating(false)
        .build());

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    doNothing().when(groupValidator).groupId(testGroup, schedule.getId());
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    given(participationRepository.findByUserAndSchedule(any(User.class), any(GroupSchedule.class)))
        .willReturn(Optional.of(participation));

    scheduleService.registerParticipation(null, 1L);

    verify(participation, times(1)).updateIsParticipating(true);
  }

  @Test
  @DisplayName("registerParticipation(): 사용자가 그룹이 아니면 예외가 발생한다")
  void failRegisterParticipation() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    doNothing().when(groupValidator).groupId(testGroup, schedule.getId());
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(false);

    assertThatThrownBy(() -> scheduleService.registerParticipation(null, 1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("cancelParticipation(): 스케줄 참여를 취소한다")
  void successCancelParticipation() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();
    Participation participation = Mockito.spy(Participation.builder()
        .user(testUser)
        .schedule(schedule)
        .isParticipating(true)
        .build());

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    doNothing().when(groupValidator).groupId(testGroup, schedule.getId());
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    given(participationRepository.findByUserAndSchedule(any(User.class), any(GroupSchedule.class)))
        .willReturn(Optional.of(participation));

    scheduleService.cancelParticipation(null, 1L);

    verify(participation, times(1)).updateIsParticipating(false);
  }

  @Test
  @DisplayName("cancelParticipation(): 그룹원이 아니면 스케줄 참여를 취소하면 예외가 발생한다")
  void failCancelParticipation_notMember() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    doNothing().when(groupValidator).groupId(testGroup, schedule.getId());
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(false);

    assertThatThrownBy(() -> scheduleService.cancelParticipation(null, 1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("cancelParticipation(): 데이터가 없으면 스케줄 참여를 취소할 수 없다")
  void failCancelParticipation_noData() {
    GroupSchedule schedule = GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build();
    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    doNothing().when(groupValidator).groupId(testGroup, schedule.getId());
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    given(participationRepository.findByUserAndSchedule(any(User.class), any(GroupSchedule.class)))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> scheduleService.cancelParticipation(null, 1L))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("deleteGroupSchedule(): 그룹 스케줄을 삭제할 수 있다")
  void successDeleteGroupSchedule() {
    GroupSchedule schedule = Mockito.spy(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build());

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    doNothing().when(scheduleValidator).scheduleOwner(eq(schedule), eq(testUser));

    scheduleService.deleteGroupSchedule(null, 1L);

    verify(scheduleRepository, times(1)).delete(schedule);
  }

  @Test
  @DisplayName("deleteGroupSchedule(): 그룹 멤버가 아니라면 스케줄을 삭제할 수 없다")
  void failDeleteGroupSchedule_notGroupMember() {
    GroupSchedule schedule = Mockito.spy(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build());

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(false);

    assertThatThrownBy(() -> scheduleService.deleteGroupSchedule(null, 1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("modifyGroupSchedule: 그룹 스케줄을 변경할 수 있다 - 이미 알람이 등록된 경우, 알람 시간 업데이트")
  void successModifyGroupSchedule_updateAlarm() {
    GroupSchedule schedule = Mockito.spy(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build());
    AlarmSchedule alarmSchedule = Mockito.spy(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(LocalDateTime.now())
        .build());
    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().plusMinutes(10),
        LocalDateTime.now().plusHours(1),
        true,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    doNothing().when(scheduleValidator).scheduleOwner(eq(schedule), eq(testUser));
    given(alarmScheduleRepository.findBySchedule(any(GroupSchedule.class)))
        .willReturn(Optional.of(alarmSchedule));

    GroupScheduleResponse response = scheduleService.modifyGroupSchedule(null, 1L, request);

    assertThat(response.title()).isEqualTo(request.title());
    assertThat(response.startTime()).isEqualTo(request.startTime());
    assertThat(response.endTime()).isEqualTo(request.endTime());
    assertThat(response.isAlarmEnabled()).isEqualTo(request.isAlarmEnabled());
    assertThat(response.alarmBeforeHours()).isEqualTo(request.alarmBeforeHours());
    assertThat(response.location()).isEqualTo(request.location());
    assertThat(response.locationLatitude()).isEqualTo(request.locationLatitude());
    assertThat(response.locationLongitude()).isEqualTo(request.locationLongitude());

    verify(schedule, times(1)).updateTitle(request.title());
    verify(schedule, times(1)).updateStartTime(request.startTime());
    verify(schedule, times(1)).updateEndTime(request.endTime());
    verify(schedule, times(1)).updateIsAlarmEnabled(request.isAlarmEnabled());
    verify(schedule, times(1)).updateAlarmBeforeHours(request.alarmBeforeHours());
    verify(schedule, times(1)).updateLocation(request.location());
    verify(schedule, times(1)).updateLocationLatitude(request.locationLatitude());
    verify(schedule, times(1)).updateLocationLongitude(request.locationLongitude());

    verify(alarmSchedule, times(1))
        .updateDateTime(request.startTime().minusHours(request.alarmBeforeHours()));
  }

  @Test
  @DisplayName("modifyGroupSchedule: 그룹 스케줄을 변경할 수 있다 - 알람 비활성화")
  void successModifyGroupSchedule_deleteAlarm() {
    GroupSchedule schedule = Mockito.spy(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build());
    AlarmSchedule alarmSchedule = Mockito.spy(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(LocalDateTime.now())
        .build());
    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().plusMinutes(10),
        LocalDateTime.now().plusHours(1),
        false,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    doNothing().when(scheduleValidator).scheduleOwner(eq(schedule), eq(testUser));
    given(alarmScheduleRepository.findBySchedule(any(GroupSchedule.class)))
        .willReturn(Optional.of(alarmSchedule));

    scheduleService.modifyGroupSchedule(null, 1L, request);

    verify(alarmScheduleRepository, times(1)).delete(alarmSchedule);
  }

  @Test
  @DisplayName("modifyGroupSchedule: 그룹 스케줄을 변경할 수 있다 - 알람 활성화(생성)")
  void successModifyGroupSchedule_createAlarm() {
    GroupSchedule schedule = Mockito.spy(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("테스트 스케줄 1")
        .build());
    AlarmSchedule alarmSchedule = Mockito.spy(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(LocalDateTime.now())
        .build());
    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().plusMinutes(10),
        LocalDateTime.now().plusHours(1),
        true,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );

    given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
    given(groupService.checkGroupMember(eq(testGroup.getId()), eq(testUser))).willReturn(true);
    doNothing().when(scheduleValidator).scheduleOwner(eq(schedule), eq(testUser));
    given(alarmScheduleRepository.findBySchedule(any(GroupSchedule.class)))
        .willReturn(Optional.empty());
    given(alarmScheduleRepository.save(any(AlarmSchedule.class))).willReturn(alarmSchedule);

    scheduleService.modifyGroupSchedule(null, 1L, request);

    verify(alarmScheduleRepository, times(1)).save(any(AlarmSchedule.class));
  }
}