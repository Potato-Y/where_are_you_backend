package com.potato_y.where_are_you.location;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupHost;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupMember;
import static com.potato_y.where_are_you.schedule.GroupScheduleUtils.createParticipation;
import static com.potato_y.where_are_you.schedule.GroupScheduleUtils.createScheduleCase1;
import static com.potato_y.where_are_you.schedule.GroupScheduleUtils.createScheduleCase2;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupMemberRepository;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.location.domain.UserLocation;
import com.potato_y.where_are_you.location.domain.UserLocationRepository;
import com.potato_y.where_are_you.location.dto.UpdateUserLocationRequest;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.ParticipationRepository;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class LocationShareApiControllerTest {

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
  private ParticipationRepository participationRepository;

  @Autowired
  private UserLocationRepository userLocationRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    userLocationRepository.deleteAll();
    participationRepository.deleteAll();
    scheduleRepository.deleteAll();
    groupMemberRepository.deleteAll();
    groupRepository.deleteAll();
    userRepository.deleteAll();

    testUser = userRepository.save(createUser("test@mail.com", "test user", "1"));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLocation(): 사용자 위치를 업데이트 할 수 있다.")
  void successUpdateUserLocation() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase1(group, testUser));
    participationRepository.save(createParticipation(schedule, testUser, true));

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(1, 3);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, schedule.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result.andExpect(status().isOk());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLocation(): 참여자가 아니라면 업데이트 할 수 없다 1")
  void failUpdateUserLocation_notParticipation() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase1(group, testUser));
    participationRepository.save(createParticipation(schedule, testUser, false));

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(1, 3);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, schedule.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result.andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLocation(): 참여자가 아니라면 업데이트 할 수 없다 2")
  void failUpdateUserLocation_notParticipationEntity() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase1(group, testUser));

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(1, 3);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, schedule.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result.andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLocation(): 공유 허용 시간이 아니면 공유할 수 없다")
  void failUpdateUserLocation_notShareTime() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase2(group, testUser));
    participationRepository.save(createParticipation(schedule, testUser, true));

    UpdateUserLocationRequest request = new UpdateUserLocationRequest(1, 3);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, schedule.getId()).contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result.andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("getScheduleMemberLocations(): 그룹원의 위치를 가져올 수 있다.")
  void successGetScheduleMemberLocations() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    User memberUser = userRepository.save(createUser("member@mail.com", "member", "123"));

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    groupMemberRepository.save(createGroupMember(group, memberUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase1(group, testUser));
    participationRepository.save(createParticipation(schedule, testUser, true));
    participationRepository.save(createParticipation(schedule, memberUser, true));

    var testUserLocation = userLocationRepository.save(
        UserLocation.builder()
            .user(testUser)
            .locationLatitude(123.567)
            .locationLongitude(456.123)
            .build());

    var memberUserLocation = userLocationRepository.save(
        UserLocation.builder()
            .user(memberUser)
            .locationLatitude(1123.34)
            .locationLongitude(342.12)
            .build());

    // when
    ResultActions result = mockMvc.perform(get(url, schedule.getId()));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].user.userId").value(testUser.getId()))
        .andExpect(jsonPath("$[0].user.nickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$[0].user.email").value(testUser.getEmail()))
        .andExpect(jsonPath("$[0].location.locationLatitude").value(
            testUserLocation.getLocationLatitude()))
        .andExpect(jsonPath("$[0].location.locationLongitude").value(
            testUserLocation.getLocationLongitude()))

        .andExpect(jsonPath("$[1].user.userId").value(memberUser.getId()))
        .andExpect(jsonPath("$[1].user.nickname").value(memberUser.getNickname()))
        .andExpect(jsonPath("$[1].user.email").value(memberUser.getEmail()))
        .andExpect(jsonPath("$[1].location.locationLatitude").value(
            memberUserLocation.getLocationLatitude()))
        .andExpect(jsonPath("$[1].location.locationLongitude").value(
            memberUserLocation.getLocationLongitude()));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("getScheduleMemberLocations(): 참여자가 아니라면 조회할 수 없다.")
  void successGetScheduleMemberLocations_notParticipation() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    User memberUser = userRepository.save(createUser("member@mail.com", "member", "123"));

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    groupMemberRepository.save(createGroupMember(group, memberUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase1(group, testUser));
    participationRepository.save(createParticipation(schedule, memberUser, true));

    // when
    ResultActions result = mockMvc.perform(get(url, schedule.getId()));

    // then
    result.andExpect(status().isBadRequest());
  }


  @Test
  @WithMockUser("1")
  @DisplayName("getScheduleMemberLocations(): 공유 시간이 아니라면 조회할 수 없다.")
  void successGetScheduleMemberLocations_notShareTime() throws Exception {
    // given
    final String url = "/v1/locations/{scheduleId}";

    User memberUser = userRepository.save(createUser("member@mail.com", "member", "123"));

    Group group = groupRepository.save(createGroup("test group", testUser));
    groupMemberRepository.save(createGroupHost(group, testUser));
    groupMemberRepository.save(createGroupMember(group, memberUser));
    GroupSchedule schedule = scheduleRepository.save(createScheduleCase2(group, testUser));
    participationRepository.save(createParticipation(schedule, memberUser, true));

    // when
    ResultActions result = mockMvc.perform(get(url, schedule.getId()));

    // then
    result.andExpect(status().isBadRequest());
  }
}
