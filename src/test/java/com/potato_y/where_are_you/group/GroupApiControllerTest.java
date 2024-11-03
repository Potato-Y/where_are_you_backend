package com.potato_y.where_are_you.group;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.group.GroupTestUtils.createUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupMemberRepository;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupApiControllerTest {

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

  private User testUser;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    groupMemberRepository.deleteAll();
    groupRepository.deleteAll();
    userRepository.deleteAll();

    testUser = userRepository.save(createUser("test@mail.com", "test user", "1"));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("createGroup(): 그룹을 생성할 수 있다.")
  void successCreateGroup() throws Exception {
    // given
    final String url = "/v1/groups";
    final String groupName = "test group";

    CreateGroupRequest request = new CreateGroupRequest(groupName);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.groupName").value(groupName))
        .andExpect(jsonPath("$.userResponse.userId").value(testUser.getId()))
        .andExpect(jsonPath("$.userResponse.nickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$.memberNumber").value(1));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("createGroup(): 그룹 이름이 없으면 500 응답을 반환한다.")
  void failCreateGroup_emptyGroupName() throws Exception {
    // given
    final String url = "/v1/groups";

    CreateGroupRequest request = new CreateGroupRequest(null);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

    // then
    result.andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("createGroup(): 그룹 이름이 공백이면 500 응답을 반환한다.")
  void failCreateGroup_blankGroupName() throws Exception {
    // given
    final String url = "/v1/groups";

    CreateGroupRequest request = new CreateGroupRequest(" ");
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

    // then
    result.andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("getGroup(): 그룹 정보를 조회할 수 있다.")
  void successGetGroup() throws Exception {
    // given
    final String url = "/v1/groups";
    final String groupName = "test group";
    final Group group = groupRepository.save(createGroup(groupName, testUser));

    // when
    ResultActions result = mockMvc.perform(get(url + "/" + group.getId()));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.groupName").value(groupName))
        .andExpect(jsonPath("$.userResponse.userId").value(testUser.getId()))
        .andExpect(jsonPath("$.userResponse.nickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$.memberNumber").value(1));
  }

  @Test
  @DisplayName("getGroup(): 인증되지 않은 사용자는 그룹 정보를 조회할 수 없다.")
  void failWithoutAuthentication() throws Exception {
    // given
    final String groupName = "test group";
    groupRepository.save(createGroup(groupName, testUser));

    // when, then
    mockMvc.perform(get("/v1/groups/1"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("createGroupInviteCode(): 그룹 초대 코드를 생성할 수 있다.")
  void successCreateGroupInviteCode() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}/invite-code";
    Group group = groupRepository.save(createGroup("test group", testUser));

    // when
    ResultActions result = mockMvc.perform(
        post(url.replace("{groupId}", group.getId().toString())));

    // then
    result
        .andExpect(jsonPath("$.groupId").value(group.getId()))
        .andExpect(jsonPath("$.inviteCode").isNotEmpty());
  }

  @Test
  @DisplayName("createGroupInviteCode(): 인증되지 않은 사용자는 그룹 초대 코드를 생성할 수 없다.")
  void failCreateGroupInviteCode() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}/invite-code";
    Group group = groupRepository.save(createGroup("test group", testUser));

    // when
    ResultActions result = mockMvc.perform(
        post(url.replace("{groupId}", group.getId().toString())));

    // when, then
    mockMvc.perform(get("/v1/groups/1"))
        .andExpect(status().isForbidden());
  }
}
