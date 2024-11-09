package com.potato_y.where_are_you.group;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupMember;
import static com.potato_y.where_are_you.group.GroupTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupInviteCode;
import com.potato_y.where_are_you.group.domain.GroupInviteCodeRepository;
import com.potato_y.where_are_you.group.domain.GroupMember;
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

  @Autowired
  private GroupInviteCodeRepository groupInviteCodeRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    groupInviteCodeRepository.deleteAll();
    groupMemberRepository.deleteAll();
    groupRepository.deleteAll();

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
    final String url = "/v1/groups/{groupId}";
    final String groupName = "test group";
    final Group group = groupRepository.save(createGroup(groupName, testUser));

    // when
    ResultActions result = mockMvc.perform(get(url, group.getId()));

    // then
    result
        .andExpect(status().isOk())
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
    ResultActions result = mockMvc.perform(post(url, group.getId()));

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
    result
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateGroup(): 그룹 정보를 변경할 수 있다.")
  void successUpdateGroup() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";
    final String groupName = "new group";

    Group group = groupRepository.save(
        Group.builder().groupName("group").hostUser(testUser).build());

    CreateGroupRequest request = new CreateGroupRequest(groupName);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, group.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.groupName").value(groupName))
        .andExpect(jsonPath("$.userResponse.userId").value(testUser.getId()))
        .andExpect(jsonPath("$.userResponse.nickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$.memberNumber").value(1));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateGroup(): 없는 그룹은 정보를 변경할 수 없다.")
  void failUpdateGroup_notFoundGroup() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";
    final String groupName = "new group";

    CreateGroupRequest request = new CreateGroupRequest(groupName);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, 1L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("updateGroup(): 권한이 없는 경우 실패한다.")
  void failUpdateGroup_notForbidden() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";

    Group group = groupRepository.save(
        Group.builder().groupName("group").hostUser(testUser).build());

    CreateGroupRequest request = new CreateGroupRequest("new group");
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
        put(url, 1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

    // then
    result
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("signupGroup(): 그룹에 가입할 수 있다.")
  void successSignupGroup() throws Exception {
    // given
    final String url = "/v1/groups/signup/{inviteCode}";
    final String groupName = "group";
    final String code = "code";
    final User hostUser = userRepository.save(createUser("host@mail.com", "host", "1"));

    Group group = groupRepository.save(
        Group.builder().groupName(groupName).hostUser(hostUser).build());
    groupInviteCodeRepository.save(
        GroupInviteCode.builder().group(group).code(code).createUser(hostUser).build());

    // when
    ResultActions result = mockMvc.perform(post(url, code));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.groupName").value(groupName))
        .andExpect(jsonPath("$.userResponse.userId").value(hostUser.getId()))
        .andExpect(jsonPath("$.userResponse.nickname").value(hostUser.getNickname()))
        .andExpect(jsonPath("$.memberNumber").value(2));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("signupGroup(): 호스트 사용자는 그룹에 가입할 수 없다.")
  void failSignupGroup_hostUser() throws Exception {
    // given
    final String url = "/v1/groups/signup/{inviteCode}";
    final String groupName = "group";
    final String code = "code";

    Group group = groupRepository.save(
        Group.builder().groupName(groupName).hostUser(testUser).build());
    groupInviteCodeRepository.save(
        GroupInviteCode.builder().group(group).code(code).createUser(testUser).build());

    // when
    ResultActions result = mockMvc.perform(post(url, code));

    // then
    result
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("signupGroup(): 호스트 사용자는 그룹에 가입할 수 없다.")
  void failSignupGroup_notFoundCode() throws Exception {
    // given
    final String url = "/v1/groups/signup/{inviteCode}";
    final String groupName = "group";
    final User hostUser = userRepository.save(createUser("host@mail.com", "host", "1"));

    Group group = groupRepository.save(
        Group.builder().groupName(groupName).hostUser(hostUser).build());
    groupInviteCodeRepository.save(
        GroupInviteCode.builder().group(group).code("code").createUser(hostUser).build());

    // when
    ResultActions result = mockMvc.perform(post(url, "codes"));

    // then
    result
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("signupGroup(): 이미 그룹에 가입한 사용자는 가입할 수 없다.")
  void failSignupGroup_inMember() throws Exception {
    // given
    final String url = "/v1/groups/signup/{inviteCode}";
    final String groupName = "group";
    final String code = "code";
    final User hostUser = userRepository.save(createUser("host@mail.com", "host", "1"));

    Group group = groupRepository.save(
        Group.builder().groupName(groupName).hostUser(hostUser).build());
    groupInviteCodeRepository.save(
        GroupInviteCode.builder().group(group).code(code).createUser(hostUser).build());
    groupMemberRepository.save(createGroupMember(group, testUser));

    // when
    ResultActions result = mockMvc.perform(post(url, code));

    // then
    result
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("signupGroup(): 인증이 없는 사용자는 가입할 수 없다.")
  void failSignupGroup_notForbidden() throws Exception {
    // given
    final String url = "/v1/groups/signup/{inviteCode}";
    final String groupName = "group";
    final String code = "code";
    final User hostUser = userRepository.save(createUser("host@mail.com", "host", "1"));

    Group group = groupRepository.save(
        Group.builder().groupName(groupName).hostUser(hostUser).build());
    groupInviteCodeRepository.save(
        GroupInviteCode.builder().group(group).code(code).createUser(hostUser).build());

    // when
    ResultActions result = mockMvc.perform(post(url, code));

    // then
    result
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("getGroupList(): 사용자의 그룹 목록을 조회할 수 있다.")
  void successGetGroupList() throws Exception {
    // given
    final String url = "/v1/groups";

    Group hostGroup = groupRepository.save(createGroup("host group", testUser));

    // 멤버로 있는 그룹 생성
    User otherUser = userRepository.save(createUser("other@mail.com", "other user", "2"));
    Group memberGroup = groupRepository.save(createGroup("member group", otherUser));
    groupMemberRepository.save(createGroupMember(memberGroup, testUser));

    // when
    ResultActions result = mockMvc.perform(get(url));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(hostGroup.getId()))
        .andExpect(jsonPath("$[0].groupName").value(hostGroup.getGroupName()))
        .andExpect(jsonPath("$[0].userResponse.userId").value(testUser.getId()))
        .andExpect(jsonPath("$[0].userResponse.nickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$[0].memberNumber").value(1))
        .andExpect(jsonPath("$[1].id").value(memberGroup.getId()))
        .andExpect(jsonPath("$[1].groupName").value(memberGroup.getGroupName()))
        .andExpect(jsonPath("$[1].userResponse.userId").value(otherUser.getId()))
        .andExpect(jsonPath("$[1].userResponse.nickname").value(otherUser.getNickname()))
        .andExpect(jsonPath("$[1].memberNumber").value(2));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("getGroupList(): 그룹이 없는 경우 빈 배열을 반환한다.")
  void successGetGroupList_empty() throws Exception {
    // given
    final String url = "/v1/groups";

    // when
    ResultActions result = mockMvc.perform(get(url));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("getGroupList(): 인증되지 않은 사용자는 그룹 목록을 조회할 수 없다.")
  void failGetGroupList_withoutAuthentication() throws Exception {
    // given
    final String url = "/v1/groups";

    // when
    ResultActions result = mockMvc.perform(get(url));

    // then
    result.andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("deleteOrLeaveGroup(): 호스트 사용자는 그룹을 삭제할 수 있다.")
  void successDeleteOrLeaveGroup_hostUser() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";
    Group group = groupRepository.save(createGroup("test group", testUser));

    // when
    ResultActions result = mockMvc.perform(delete(url, group.getId()));

    // then
    result.andExpect(status().isOk());
    assertThat(groupRepository.findById(group.getId())).isEmpty();
  }

  @Test
  @WithMockUser("1")
  @DisplayName("deleteOrLeaveGroup(): 멤버 사용자는 그룹에서 탈퇴할 수 있다.")
  void successDeleteOrLeaveGroup_memberUser() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";
    User hostUser = userRepository.save(createUser("host@mail.com", "host user", "2"));
    Group group = groupRepository.save(createGroup("test group", hostUser));
    GroupMember member = groupMemberRepository.save(createGroupMember(group, testUser));

    // when
    ResultActions result = mockMvc.perform(delete(url, group.getId()));

    // then
    result.andExpect(status().isOk());
    assertThat(groupMemberRepository.findById(member.getId())).isEmpty();
    assertThat(groupRepository.findById(group.getId())).isPresent();
  }

  @Test
  @WithMockUser("1")
  @DisplayName("deleteOrLeaveGroup(): 존재하지 않는 그룹은 삭제/탈퇴할 수 없다.")
  void failDeleteOrLeaveGroup_notFoundGroup() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";

    // when
    ResultActions result = mockMvc.perform(delete(url, 1L));

    // then
    result.andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("deleteOrLeaveGroup(): 인증되지 않은 사용자는 그룹을 삭제/탈퇴할 수 없다.")
  void failDeleteOrLeaveGroup_unauthorized() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";
    Group group = groupRepository.save(createGroup("test group", testUser));

    // when
    ResultActions result = mockMvc.perform(delete(url, group.getId()));

    // then
    result.andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("deleteOrLeaveGroup(): 그룹의 멤버가 아닌 사용자는 탈퇴할 수 없다.")
  void failDeleteOrLeaveGroup_notMember() throws Exception {
    // given
    final String url = "/v1/groups/{groupId}";
    User hostUser = userRepository.save(createUser("host@mail.com", "host user", "2"));
    Group group = groupRepository.save(createGroup("test group", hostUser));

    // when
    ResultActions result = mockMvc.perform(
        delete(url, group.getId())
    );

    // then
    result.andExpect(status().isBadRequest());
  }
}