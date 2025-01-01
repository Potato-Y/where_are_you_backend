package com.potato_y.where_are_you.schedule;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupHost;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupMember;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static com.potato_y.where_are_you.utils.SecurityContextUtils.setAuthentication;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupMemberRepository;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.schedule.domain.AlarmSchedule;
import com.potato_y.where_are_you.schedule.domain.AlarmScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.Participation;
import com.potato_y.where_are_you.schedule.domain.ParticipationRepository;
import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupScheduleApiControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private GroupMemberRepository groupMemberRepository;

  @Autowired
  private GroupScheduleRepository scheduleRepository;

  @Autowired
  private AlarmScheduleRepository alarmScheduleRepository;

  @Autowired
  private ParticipationRepository participationRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    alarmScheduleRepository.deleteAll();
    participationRepository.deleteAll();
    scheduleRepository.deleteAll();
    groupMemberRepository.deleteAll();
    groupRepository.deleteAll();
    userRepository.deleteAll();

    userRepository.findAll();  // flush 목적
    testUser = userRepository.save(createUser("test@mail.com", "test user", "1"));
  }

  @Test
  @Transactional
  @DisplayName("createGroupSchedule(): 그룹 스케줄을 추가할 수 있다 - 호스트")
  void successCreateGroupSchedule_host() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "스케줄 이름",
        LocalDateTime.now().plusHours(1).withSecond(0).withNano(0),
        LocalDateTime.now().plusHours(2).withSecond(0).withNano(0),
        true,
        1,
        "위치 이름",
        1.1,
        2.2
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url, testGroup.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.scheduleId").isNumber())
        .andExpect(jsonPath("$.groupId").isNumber())
        .andExpect(jsonPath("$.createUserId").isNumber())
        .andExpect(jsonPath("$.title").value(request.title()))
        .andExpect(jsonPath("$.startTime").value(startsWith(request.startTime().toString())))
        .andExpect(jsonPath("$.endTime").value(startsWith(request.endTime().toString())))
        .andExpect(jsonPath("$.isAlarmEnabled").value(request.isAlarmEnabled()))
        .andExpect(jsonPath("$.alarmBeforeHours").value(request.alarmBeforeHours()))
        .andExpect(jsonPath("$.location").value(request.location()))
        .andExpect(jsonPath("$.locationLatitude").value(request.locationLatitude()))
        .andExpect(jsonPath("$.locationLongitude").value(request.locationLongitude()));

    List<AlarmSchedule> alarmSchedule = alarmScheduleRepository.findByDateTime(
        request.startTime().minusHours(1));
    assertThat(alarmSchedule.getFirst().getSchedule().getGroup()).isEqualTo(testGroup);
    assertThat(alarmSchedule.getFirst().getSchedule().getTitle()).isEqualTo(request.title());
  }

  @Test
  @Transactional
  @DisplayName("createGroupSchedule(): 그룹 스케줄을 추가할 수 있다 - 호스트")
  void successCreateGroupSchedule_member() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "스케줄 이름",
        LocalDateTime.now().plusHours(1).withSecond(0).withNano(0),
        LocalDateTime.now().plusHours(2).withSecond(0).withNano(0),
        true,
        1,
        "위치 이름",
        1.1,
        2.2
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url, testGroup.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.scheduleId").isNumber())
        .andExpect(jsonPath("$.groupId").isNumber())
        .andExpect(jsonPath("$.createUserId").isNumber())
        .andExpect(jsonPath("$.title").value(request.title()))
        .andExpect(jsonPath("$.startTime").value(startsWith(request.startTime().toString())))
        .andExpect(jsonPath("$.endTime").value(startsWith(request.endTime().toString())))
        .andExpect(jsonPath("$.isAlarmEnabled").value(request.isAlarmEnabled()))
        .andExpect(jsonPath("$.alarmBeforeHours").value(request.alarmBeforeHours()))
        .andExpect(jsonPath("$.location").value(request.location()))
        .andExpect(jsonPath("$.locationLatitude").value(request.locationLatitude()))
        .andExpect(jsonPath("$.locationLongitude").value(request.locationLongitude()));

    List<AlarmSchedule> alarmSchedule = alarmScheduleRepository.findByDateTime(
        request.startTime().minusHours(1));
    assertThat(alarmSchedule.getFirst().getSchedule().getGroup()).isEqualTo(testGroup);
    assertThat(alarmSchedule.getFirst().getSchedule().getTitle()).isEqualTo(request.title());
  }

  @Test
  @Transactional
  @DisplayName("createGroupSchedule(): 그룹 스케줄을 추가할 수 있다 - 알람 없이")
  void successCreateGroupSchedule_notAlarm() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "스케줄 이름",
        LocalDateTime.now().plusHours(1).withSecond(0).withNano(0),
        LocalDateTime.now().plusHours(2).withSecond(0).withNano(0),
        false,
        1,
        "위치 이름",
        1.1,
        2.2
    );

    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url, testGroup.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.scheduleId").isNumber())
        .andExpect(jsonPath("$.groupId").isNumber())
        .andExpect(jsonPath("$.createUserId").isNumber())
        .andExpect(jsonPath("$.title").value(request.title()))
        .andExpect(jsonPath("$.startTime").value(startsWith(request.startTime().toString())))
        .andExpect(jsonPath("$.endTime").value(startsWith(request.endTime().toString())))
        .andExpect(jsonPath("$.isAlarmEnabled").value(request.isAlarmEnabled()))
        .andExpect(jsonPath("$.alarmBeforeHours").value(request.alarmBeforeHours()))
        .andExpect(jsonPath("$.location").value(request.location()))
        .andExpect(jsonPath("$.locationLatitude").value(request.locationLatitude()))
        .andExpect(jsonPath("$.locationLongitude").value(request.locationLongitude()));

    List<AlarmSchedule> alarmSchedule = alarmScheduleRepository.findByDateTime(
        request.startTime().minusHours(1));
    assertThat(alarmSchedule.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("createGroupSchedule(): 그룹원이 아니라면 권한 예외가 발생한다")
  void failCreateGroupSchedule_notGroupMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "스케줄 이름",
        LocalDateTime.now().plusHours(1).withSecond(0).withNano(0),
        LocalDateTime.now().plusHours(2).withSecond(0).withNano(0),
        false,
        1,
        "위치 이름",
        1.1,
        2.2
    );

    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url, testGroup.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getGroupSchedules(): 그룹의 스케줄 목록을 가져올 수 있다")
  void successGetGroupSchedules() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));

    var schedule1 = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());
    var schedule2 = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 2")
        .build());
    participationRepository.save(Participation.builder()
        .user(testUser)
        .schedule(schedule1)
        .isParticipating(true)
        .build());

    ResultActions result = mockMvc.perform(get(url, testGroup.getId()));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].groupSchedule.scheduleId").value(schedule1.getId()))
        .andExpect(jsonPath("$[0].groupSchedule.groupId").value(testGroup.getId()))
        .andExpect(jsonPath("$[0].groupSchedule.createUserId").value(testUser.getId()))
        .andExpect(jsonPath("$[0].groupSchedule.title").value(schedule1.getTitle()))
        .andExpect(
            jsonPath("$[0].groupSchedule.startTime").value(
                startsWith(schedule1.getStartTime().toString())))
        .andExpect(
            jsonPath("$[0].groupSchedule.endTime").value(
                startsWith(schedule1.getEndTime().toString())))
        .andExpect(jsonPath("$[0].groupSchedule.isAlarmEnabled").value(schedule1.isAlarmEnabled()))
        .andExpect(
            jsonPath("$[0].groupSchedule.alarmBeforeHours").value(schedule1.getAlarmBeforeHours()))
        .andExpect(jsonPath("$[0].groupSchedule.location").value(schedule1.getLocation()))
        .andExpect(
            jsonPath("$[0].groupSchedule.locationLatitude").value(schedule1.getLocationLatitude()))
        .andExpect(
            jsonPath("$[0].groupSchedule.locationLongitude").value(
                schedule1.getLocationLongitude()))
        .andExpect(jsonPath("$[0].isParticipating").value(true))
        .andExpect(jsonPath("$[1].groupSchedule.scheduleId").value(schedule2.getId()))
        .andExpect(jsonPath("$[1].groupSchedule.groupId").value(testGroup.getId()))
        .andExpect(jsonPath("$[1].groupSchedule.createUserId").value(testUser.getId()))
        .andExpect(jsonPath("$[1].groupSchedule.title").value(schedule2.getTitle()))
        .andExpect(
            jsonPath("$[1].groupSchedule.startTime").value(
                startsWith(schedule2.getStartTime().toString())))
        .andExpect(
            jsonPath("$[1].groupSchedule.endTime").value(
                startsWith(schedule2.getEndTime().toString())))
        .andExpect(jsonPath("$[1].groupSchedule.isAlarmEnabled").value(schedule2.isAlarmEnabled()))
        .andExpect(
            jsonPath("$[1].groupSchedule.alarmBeforeHours").value(schedule2.getAlarmBeforeHours()))
        .andExpect(jsonPath("$[1].groupSchedule.location").value(schedule2.getLocation()))
        .andExpect(
            jsonPath("$[1].groupSchedule.locationLatitude").value(schedule2.getLocationLatitude()))
        .andExpect(
            jsonPath("$[1].groupSchedule.locationLongitude").value(
                schedule2.getLocationLongitude()))
        .andExpect(jsonPath("$[1].isParticipating").value(false));
  }

  @Test
  @DisplayName("getGroupSchedules(): 그룹원이 아니라면 조회할 수 없다")
  void failGetGroupSchedules_notMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));

    ResultActions result = mockMvc.perform(get(url, testGroup.getId()));

    result
        .andExpect(status().isForbidden());
  }

  @Test
  @Transactional
  @DisplayName("registerParticipation(): 처음으로 스케줄 참여를 신청할 수 있다 - 호스트")
  void successRegisterParticipation_noData_groupHost() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());

    ResultActions result = mockMvc.perform(post(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isOk());

    Optional<Participation> participation = participationRepository.findByUserAndSchedule(testUser,
        schedule);
    assertThat(participation).isNotEmpty();
    assertThat(participation.get().getUser()).isEqualTo(testUser);
    assertThat(participation.get().getSchedule()).isEqualTo(schedule);
  }

  @Test
  @Transactional
  @DisplayName("registerParticipation(): 처음으로 스케줄 참여를 신청할 수 있다 - 그룹원")
  void successRegisterParticipation_noData_groupMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());

    ResultActions result = mockMvc.perform(post(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isOk());

    Optional<Participation> participation = participationRepository.findByUserAndSchedule(testUser,
        schedule);
    assertThat(participation).isNotEmpty();
    assertThat(participation.get().getUser()).isEqualTo(testUser);
    assertThat(participation.get().getSchedule()).isEqualTo(schedule);
    assertThat(participation.get().isParticipating()).isTrue();
  }

  @Test
  @Transactional
  @DisplayName("registerParticipation(): 취소한 일정을 다시 신청할 수 있다")
  void successRegisterParticipation_updateData() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());
    participationRepository.save(Participation.builder()
        .schedule(schedule)
        .user(testUser)
        .isParticipating(false)
        .build());

    ResultActions result = mockMvc.perform(post(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isOk());

    Optional<Participation> participation = participationRepository.findByUserAndSchedule(testUser,
        schedule);
    assertThat(participation).isNotEmpty();
    assertThat(participation.get().getUser()).isEqualTo(testUser);
    assertThat(participation.get().getSchedule()).isEqualTo(schedule);
    assertThat(participation.get().isParticipating()).isTrue();
  }

  @Test
  @DisplayName("registerParticipation(): 그룹원이 아니라면 참여할 수 없다")
  void failRegisterParticipation_notMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());

    ResultActions result = mockMvc.perform(post(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isForbidden());
  }

  @Test
  @Transactional
  @DisplayName("cancelParticipation(): 참여를 취소할 수 있다")
  void successCancelParticipation() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());
    participationRepository.save(Participation.builder()
        .schedule(schedule)
        .user(testUser)
        .isParticipating(false)
        .build());

    ResultActions result = mockMvc.perform(patch(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isOk());

    Optional<Participation> participation = participationRepository.findByUserAndSchedule(testUser,
        schedule);
    assertThat(participation).isNotEmpty();
    assertThat(participation.get().getUser()).isEqualTo(testUser);
    assertThat(participation.get().getSchedule()).isEqualTo(schedule);
    assertThat(participation.get().isParticipating()).isFalse();
  }

  @Test
  @DisplayName("cancelParticipation(): 그룹원이 아니라면 참여를 취소할 수 없다")
  void failCancelParticipation_notMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());

    ResultActions result = mockMvc.perform(patch(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getParticipationList(): 특정 스케줄의 참여자 목록을 가져올 수 있다")
  void successGetParticipationList() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());
    participationRepository.save(Participation.builder()
        .schedule(schedule)
        .user(hostUser)
        .isParticipating(true)
        .build());
    participationRepository.save(Participation.builder()
        .schedule(schedule)
        .user(testUser)
        .isParticipating(true)
        .build());

    ResultActions result = mockMvc.perform(get(url, testGroup.getId(), schedule.getId()));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userId").value(hostUser.getId()))
        .andExpect(jsonPath("$[1].userId").value(testUser.getId()));
  }

  @Test
  @DisplayName("getParticipationList(): 그룹원이 아니라면 스케줄의 참여자 목록을 가져올 수 없다")
  void failGetParticipationList_notMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}/participation";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));

    var schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .title("스케줄 1")
        .build());
    participationRepository.save(Participation.builder()
        .schedule(schedule)
        .user(hostUser)
        .isParticipating(true)
        .build());

    ResultActions result = mockMvc.perform(get(url, testGroup.getId(), schedule.getId()));

    result.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("deleteGroupSchedule(): 스케줄을 삭제할 수 있다")
  void successDeleteGroupSchedule() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .build());
    Participation participation = participationRepository.save(Participation.builder()
        .isParticipating(true)
        .schedule(schedule)
        .user(testUser)
        .build());
    AlarmSchedule alarmSchedule = alarmScheduleRepository.save(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(LocalDateTime.now())
        .build());

    ResultActions result = mockMvc.perform(
        delete(url, testGroup.getId(), schedule.getId()).contentType(
            MediaType.APPLICATION_JSON_VALUE));

    result.andExpect(status().isOk());
    assertThat(scheduleRepository.findById(schedule.getId())).isEmpty();
    assertThat(participationRepository.findById(participation.getId())).isEmpty();
    assertThat(alarmScheduleRepository.findById(alarmSchedule.getId())).isEmpty();
  }

  @Test
  @DisplayName("deleteGroupSchedule(): 그룹 유저가 아니라면 스케줄을 삭제할 수 없다")
  void failDeleteGroupSchedule_notGroupMember() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(hostUser)
        .build());

    ResultActions result = mockMvc.perform(
        delete(url, testGroup.getId(), schedule.getId()).contentType(
            MediaType.APPLICATION_JSON_VALUE));

    result.andExpect(status().isForbidden());

    assertThat(scheduleRepository.findById(schedule.getId())).isNotEmpty();
  }

  @Test
  @DisplayName("deleteGroupSchedule(): 스케줄을 생성한 유저가 아니라면 스케줄을 삭제할 수 없다")
  void failDeleteGroupSchedule_notScheduleOwner() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", hostUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(hostUser)
        .build());

    ResultActions result = mockMvc.perform(
        delete(url, testGroup.getId(), schedule.getId()).contentType(
            MediaType.APPLICATION_JSON_VALUE));

    result.andExpect(status().isForbidden());

    assertThat(scheduleRepository.findById(schedule.getId())).isNotEmpty();
  }

  @Test
  @Transactional
  @DisplayName("modifyGroupSchedule(): 그룹 스케줄을 변경할 수 있다 - 이미 알람이 등록된 경우, 알람 시간 업데이트")
  void successModifyGroupSchedule_updateAlarm() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .build());
    alarmScheduleRepository.save(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(LocalDateTime.now())
        .build());

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(10),
        LocalDateTime.now().withSecond(0).withNano(0).plusHours(1),
        true,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        put(url, testGroup.getId(), schedule.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scheduleId").isNumber())
        .andExpect(jsonPath("$.groupId").isNumber())
        .andExpect(jsonPath("$.createUserId").isNumber())
        .andExpect(jsonPath("$.title").value(request.title()))
        .andExpect(jsonPath("$.startTime").value(startsWith(request.startTime().toString())))
        .andExpect(jsonPath("$.endTime").value(startsWith(request.endTime().toString())))
        .andExpect(jsonPath("$.isAlarmEnabled").value(request.isAlarmEnabled()))
        .andExpect(jsonPath("$.alarmBeforeHours").value(request.alarmBeforeHours()))
        .andExpect(jsonPath("$.location").value(request.location()))
        .andExpect(jsonPath("$.locationLatitude").value(request.locationLatitude()))
        .andExpect(jsonPath("$.locationLongitude").value(request.locationLongitude()));

    AlarmSchedule alarmSchedule = alarmScheduleRepository.findBySchedule(schedule).get();
    assertThat(alarmSchedule.getSchedule().getGroup()).isEqualTo(testGroup);
    assertThat(alarmSchedule.getSchedule().getTitle()).isEqualTo(request.title());
    assertThat(alarmSchedule.getDateTime())
        .isEqualTo(request.startTime().minusHours(request.alarmBeforeHours()));
  }

  @Test
  @Transactional
  @DisplayName("modifyGroupSchedule(): 그룹 스케줄을 변경할 수 있다 - 알람 비활성화")
  void successModifyGroupSchedule_deleteAlarm() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .build());
    alarmScheduleRepository.save(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(LocalDateTime.now())
        .build());

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(10),
        LocalDateTime.now().withSecond(0).withNano(0).plusHours(1),
        false,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        put(url, testGroup.getId(), schedule.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scheduleId").isNumber())
        .andExpect(jsonPath("$.groupId").isNumber())
        .andExpect(jsonPath("$.createUserId").isNumber())
        .andExpect(jsonPath("$.title").value(request.title()))
        .andExpect(jsonPath("$.startTime").value(startsWith(request.startTime().toString())))
        .andExpect(jsonPath("$.endTime").value(startsWith(request.endTime().toString())))
        .andExpect(jsonPath("$.isAlarmEnabled").value(request.isAlarmEnabled()))
        .andExpect(jsonPath("$.alarmBeforeHours").value(request.alarmBeforeHours()))
        .andExpect(jsonPath("$.location").value(request.location()))
        .andExpect(jsonPath("$.locationLatitude").value(request.locationLatitude()))
        .andExpect(jsonPath("$.locationLongitude").value(request.locationLongitude()));

    assertThat(alarmScheduleRepository.findBySchedule(schedule)).isEmpty();
  }

  @Test
  @Transactional
  @DisplayName("modifyGroupSchedule(): 그룹 스케줄을 변경할 수 있다 - 알람 활성화(생성)")
  void successModifyGroupSchedule_createAlarm() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(testUser)
        .build());

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(10),
        LocalDateTime.now().withSecond(0).withNano(0).plusHours(1),
        true,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        put(url, testGroup.getId(), schedule.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scheduleId").isNumber())
        .andExpect(jsonPath("$.groupId").isNumber())
        .andExpect(jsonPath("$.createUserId").isNumber())
        .andExpect(jsonPath("$.title").value(request.title()))
        .andExpect(jsonPath("$.startTime").value(startsWith(request.startTime().toString())))
        .andExpect(jsonPath("$.endTime").value(startsWith(request.endTime().toString())))
        .andExpect(jsonPath("$.isAlarmEnabled").value(request.isAlarmEnabled()))
        .andExpect(jsonPath("$.alarmBeforeHours").value(request.alarmBeforeHours()))
        .andExpect(jsonPath("$.location").value(request.location()))
        .andExpect(jsonPath("$.locationLatitude").value(request.locationLatitude()))
        .andExpect(jsonPath("$.locationLongitude").value(request.locationLongitude()));

    AlarmSchedule alarmSchedule = alarmScheduleRepository.findBySchedule(schedule).get();
    assertThat(alarmSchedule.getSchedule().getGroup()).isEqualTo(testGroup);
    assertThat(alarmSchedule.getSchedule().getTitle()).isEqualTo(request.title());
    assertThat(alarmSchedule.getDateTime())
        .isEqualTo(request.startTime().minusHours(request.alarmBeforeHours()));
  }

  @Test
  @DisplayName("modifyGroupSchedule(): 그룹 스케줄을 변경할 수 없다 - 없는 스케줄")
  void failModifyGroupSchedule_notSchedule() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, testUser));

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(10),
        LocalDateTime.now().withSecond(0).withNano(0).plusHours(1),
        true,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        put(url, testGroup.getId(), 1L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result.andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("modifyGroupSchedule(): 그룹 스케줄을 변경할 수 없다 - 스케줄 생성자가 아님")
  void failModifyGroupSchedule_notScheduleOwner() throws Exception {
    setAuthentication(testUser);
    final var url = "/v1/groups/{groupId}/schedule/{scheduleId}";

    User hostUser = userRepository.save(createUser("host@mail.com", "host", "213"));
    Group testGroup = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(testGroup, hostUser));
    groupMemberRepository.save(createGroupMember(testGroup, testUser));
    GroupSchedule schedule = scheduleRepository.save(GroupScheduleFactory.builder()
        .group(testGroup)
        .user(hostUser)
        .build());

    CreateGroupScheduleRequest request = new CreateGroupScheduleRequest(
        "바뀐 이름",
        LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(10),
        LocalDateTime.now().withSecond(0).withNano(0).plusHours(1),
        true,
        2,
        "바뀐 위치",
        12.3,
        34.1
    );
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        put(url, testGroup.getId(), schedule.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    result.andExpect(status().isForbidden());
  }
}
