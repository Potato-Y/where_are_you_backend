package com.potato_y.where_are_you.group;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupHost;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupInviteCode;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupMember;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupInviteCode;
import com.potato_y.where_are_you.group.domain.GroupInviteCodeRepository;
import com.potato_y.where_are_you.group.domain.GroupMember;
import com.potato_y.where_are_you.group.domain.GroupMemberRepository;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
import com.potato_y.where_are_you.group.dto.GroupInviteCodeResponse;
import com.potato_y.where_are_you.group.dto.GroupResponse;
import com.potato_y.where_are_you.user.domain.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  @InjectMocks
  private GroupService groupService;

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private GroupMemberRepository groupMemberRepository;

  @Mock
  private GroupInviteCodeRepository groupInviteCodeRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = createUser("test@mail.com", "test user", "1");
  }

  @Test
  @DisplayName("createGroup(): 그룹을 생성할 수 있다.")
  public void successCreateGroup() {
    // given
    String groupName = "test group";
    CreateGroupRequest request = new CreateGroupRequest(groupName);
    Group group = createGroup(groupName, testUser);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupRepository.save(any(Group.class))).willReturn(group);

    // when
    GroupResponse response = groupService.createGroup(request);

    // then
    assertThat(response.groupName()).isEqualTo(groupName);
    assertThat(response.hostUser().getNickname()).isEqualTo(testUser.getNickname());
    assertThat(response.memberNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("getGroupResponse(): 그룹 정보를 조회할 수 있다. - 1명 그룹")
  void successGetGroupResponse_oneMember() {
    // given
    Group group = createGroup("test name", testUser);
    GroupMember groupHostMember = createGroupHost(group, testUser);
    List<GroupMember> members = List.of(groupHostMember);

    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    GroupResponse response = groupService.getGroupResponse(1L);

    // then
    assertThat(response.groupName()).isEqualTo(group.getGroupName());
    assertThat(response.hostUser().getNickname()).isEqualTo(testUser.getNickname());
    assertThat(response.memberNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("getGroupResponse(): 그룹 정보를 조회할 수 있다. - 다수 인원의 그룹")
  void successGetGroupResponse_manyMember() {
    // given
    Group group = createGroup("test name", testUser);

    User memberUser = createUser("member@mail.com", "member 1", "2");

    GroupMember host = createGroupHost(group, testUser);
    GroupMember member = createGroupMember(group, memberUser);
    List<GroupMember> members = List.of(host, member, member, member);

    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    GroupResponse response = groupService.getGroupResponse(1L);

    // then
    assertThat(response.groupName()).isEqualTo(group.getGroupName());
    assertThat(response.hostUser().getNickname()).isEqualTo(testUser.getNickname());
    assertThat(response.memberNumber()).isEqualTo(4);
  }

  @Test
  @DisplayName("getGroupResponse(): 존재하지 않는 그룹을 조회하면 예외가 발생한다.")
  void failGetGroupResponse_notFountGroup() {
    // given
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // when, then
    assertThatThrownBy(() -> groupService.getGroupResponse(1L)).isInstanceOf(
        NotFoundException.class);
  }

  @Test
  @DisplayName("getGroupMembers(): 그룹의 멤버 목록을 조회할 수 있다. - 빈 그룹 멤버")
  void successGetGroupMembers_empty() {
    // given
    Group group = Group.builder()
        .groupName("test group")
        .hostUser(testUser)
        .build();
    List<GroupMember> members = Collections.emptyList();

    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    List<GroupMember> result = groupService.getGroupMembers(group);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getGroupMembers(): 그룹의 멤버 목록을 조회할 수 있다. - 많은 그룹 멤버")
  void successGetGroupMembers_many() {
    // given
    Group group = createGroup("test group", testUser);
    User user1 = createUser("member1@mail.com", "member 1", "2");
    GroupMember member1 = createGroupMember(group, user1);
    User user2 = createUser("member2@mail.com", "member 2", "3");
    GroupMember member2 = createGroupMember(group, user2);

    List<GroupMember> members = List.of(member1, member2);

    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    List<GroupMember> result = groupService.getGroupMembers(group);

    // then
    assertThat(result.get(0).getUser().getNickname()).isEqualTo(user1.getNickname());
    assertThat(result.get(1).getUser().getNickname()).isEqualTo(user2.getNickname());
  }

  @Test
  @DisplayName("createInviteCode(): 초대 코드를 생성할 수 있다.")
  void successCreateInviteCode() {
    // given
    Group group = createGroup("test_group", testUser);
    String code = "1234567890";

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupInviteCodeRepository.findByCode(any(String.class))).willReturn(Optional.empty());
    given(groupInviteCodeRepository.save(any(GroupInviteCode.class))).willReturn(
        createGroupInviteCode(group, testUser, code));

    // when
    GroupInviteCodeResponse response = groupService.createInviteCode(1L);

    // then
    assertThat(response.inviteCode()).isEqualTo(code);
  }

  @Test
  @DisplayName("createInviteCode(): 호스트 사용자가 아니라면 초대 코드를 생성할 수 없다.")
  void failCreateInviteCode_otherUser() {
    // given
    User otherUser = createUser("other@mail.com", "other user", "32");
    Group group = createGroup("test_group", testUser);

    given(currentUserProvider.getCurrentUser()).willReturn(otherUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));

    // when, then
    assertThatThrownBy(() -> groupService.createInviteCode(1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("getUniqueInviteCode(): 고유한 코드를 생성할 수 있다.")
  void successGetUniqueInviteCode() {
    // given
    String code = "1234567890";

    given(groupInviteCodeRepository.findByCode(any(String.class))).willReturn(Optional.empty());

    // when
    String result = groupService.getUniqueInviteCode();

    // then
    assertThat(result).hasSize(10);
  }

  @Test
  @DisplayName("updateGroup(): 그룹 정보를 변경할 수 있다.")
  void successUpdateGroup() {
    // given
    String groupName = "new name";
    Group group = createGroup("test_group", testUser);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupMemberRepository.findByGroup(group)).willReturn(Collections.emptyList());

    CreateGroupRequest request = new CreateGroupRequest(groupName);

    // when
    GroupResponse response = groupService.updateGroup(1L, request);

    // then
    assertThat(response.groupName()).isEqualTo(groupName);
  }

  @Test
  @DisplayName("updateGroup(): 호스트 사용자가 아니라면 그룹 정보를 수정할 수 없다.")
  void failUpdateGroup_notHostUser() {
    // given
    User otherUser = createUser("other@mail.com", "other user", "32");
    Group group = createGroup("test_group", testUser);

    CreateGroupRequest request = new CreateGroupRequest("groupName");

    given(currentUserProvider.getCurrentUser()).willReturn(otherUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));

    // when, then
    assertThatThrownBy(() -> groupService.updateGroup(1L, request))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("signupGroup(): 그룹 가입에 성공한다.")
  void successSignupGroup() {
    final String groupName = "group";
    final String code = "code";
    User user = createUser("other@mail.com", "other", "2");
    Group group = createGroup(groupName, testUser);
    List<GroupMember> members = List.of(createGroupHost(group, testUser),
        createGroupMember(group, user));

    given(currentUserProvider.getCurrentUser()).willReturn(user);
    given(groupInviteCodeRepository.findByCode(anyString())).willReturn(
        Optional.ofNullable(createGroupInviteCode(group, testUser, code)));
    given(groupMemberRepository.findByGroupAndUser(any(Group.class), any(User.class))).willReturn(
        Optional.empty());
    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    GroupResponse response = groupService.signupGroup(code);

    // then
    assertThat(response.groupName()).isEqualTo(groupName);
    assertThat(response.hostUser().getNickname()).isEqualTo(testUser.getNickname());
    assertThat(response.memberNumber()).isEqualTo(2);
  }

  @Test
  @DisplayName("signupGroup(): 호스트 유저는 그룹 가입에 실패한다.")
  void failSignupGroup_hostUser() {
    final String groupName = "group";
    final String code = "code";
    Group group = createGroup(groupName, testUser);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupInviteCodeRepository.findByCode(anyString())).willReturn(
        Optional.ofNullable(createGroupInviteCode(group, testUser, code)));

    // when, then
    assertThatThrownBy(() -> groupService.signupGroup(code))
        .isInstanceOf(BadRequestException.class);
  }


  @Test
  @DisplayName("signupGroup(): 이미 가입한 사용자는 그룹 가입에 실패한다.")
  void failSignupGroup_inMember() {
    final String groupName = "group";
    final String code = "code";
    User user = createUser("other@mail.com", "other", "2");
    Group group = createGroup(groupName, testUser);
    GroupMember member = createGroupMember(group, user);

    given(currentUserProvider.getCurrentUser()).willReturn(user);
    given(groupInviteCodeRepository.findByCode(anyString())).willReturn(
        Optional.ofNullable(createGroupInviteCode(group, testUser, code)));
    given(groupMemberRepository.findByGroupAndUser(any(Group.class), any(User.class))).willReturn(
        Optional.ofNullable(member));

    // when, then
    assertThatThrownBy(() -> groupService.signupGroup(code))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("getGroupList(): 가입한 그룹이 없으면 빈 리스트를 반환한다.")
  void successGetGroupList_empty() {
    // given
    User testUser = createUser("test@mail.com", "test user", "1");

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupMemberRepository.findByUser(any(User.class))).willReturn(Collections.emptyList());

    // when
    List<GroupResponse> responses = groupService.getGroupList();

    // then
    assertThat(responses).isEmpty();
  }

  @Test
  @DisplayName("getGroupList(): 사용자가 가입한 그룹 목록을 조회할 수 있다.")
  void successGetGroupList() {
    // given
    User user = createUser("test@mail.com", "test user", "1");
    User otherUser = createUser("other@mail.com", "other user", "2");

    // 호스트로 있는 그룹
    Group hostGroup = createGroup("host group", user);
    groupRepository.save(hostGroup);
    GroupMember hostGroupHost = createGroupHost(hostGroup, user);
    groupMemberRepository.save(hostGroupHost);

    // 멤버로 있는 그룹
    Group memberGroup = createGroup("member group", otherUser);
    groupRepository.save(memberGroup);
    GroupMember memberGroupHost = createGroupHost(memberGroup, user);
    GroupMember memberGroupMember = createGroupMember(memberGroup, user);
    groupMemberRepository.save(memberGroupHost);
    groupMemberRepository.save(memberGroupMember);

    given(currentUserProvider.getCurrentUser()).willReturn(user);
    given(groupMemberRepository.findByUser(user)).willReturn(
        List.of(hostGroupHost, memberGroupMember));
    given(groupMemberRepository.findByGroup(hostGroup)).willReturn(List.of(hostGroupHost));
    given(groupMemberRepository.findByGroup(memberGroup)).willReturn(
        List.of(memberGroupHost, memberGroupMember));

    // when
    List<GroupResponse> responses = groupService.getGroupList();

    // then
    assertThat(responses).hasSize(2);

    // 호스트 그룹 검증
    assertThat(responses.get(0).groupName()).isEqualTo(hostGroup.getGroupName());
    assertThat(responses.get(0).hostUser().getNickname()).isEqualTo(user.getNickname());
    assertThat(responses.get(0).memberNumber()).isEqualTo(1); // 호스트만 있는 경우

    // 멤버 그룹 검증
    assertThat(responses.get(1).groupName()).isEqualTo(memberGroup.getGroupName());
    assertThat(responses.get(1).hostUser().getNickname()).isEqualTo(otherUser.getNickname());
    assertThat(responses.get(1).memberNumber()).isEqualTo(2); // 호스트 + 멤버 1명
  }

  @Test
  @DisplayName("deleteOrLeaveGroup(): 호스트 사용자는 그룹을 삭제할 수 있다.")
  void successDeleteOrLeaveGroup_hostUser() {
    // given
    User hostUser = createUser("host@mail.com", "host user", "1");
    Group group = createGroup("test group", hostUser);

    given(currentUserProvider.getCurrentUser()).willReturn(hostUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));

    // when
    groupService.deleteOrLeaveGroup(1L);

    // then
    verify(groupRepository, times(1)).delete(group);
  }

  @Test
  @DisplayName("deleteOrLeaveGroup(): 멤버 사용자는 그룹에서 탈퇴할 수 있다.")
  void successDeleteOrLeaveGroup_memberUser() {
    // given
    User hostUser = createUser("host@mail.com", "host user", "1");
    Group group = createGroup("test group", hostUser);
    GroupMember groupMember = createGroupMember(group, testUser);

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupMemberRepository.findByGroupAndUser(group, testUser))
        .willReturn(Optional.of(groupMember));

    // when
    groupService.deleteOrLeaveGroup(1L);

    // then
    verify(groupMemberRepository, times(1)).delete(groupMember);
  }

  @Test
  @DisplayName("deleteOrLeaveGroup(): 존재하지 않는 그룹은 삭제/탈퇴할 수 없다.")
  void failDeleteOrLeaveGroup_notFoundGroup() {
    // given
    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // when, then
    assertThatThrownBy(() -> groupService.deleteOrLeaveGroup(1L))
        .isInstanceOf(NotFoundException.class);
  }

  // TODO: 그룹원 확인에 대한 테스트 코드 추가
  // TODO: findByGroup에 대한 테스트 추가
}
