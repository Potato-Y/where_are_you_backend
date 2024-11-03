package com.potato_y.where_are_you.group;

import static com.potato_y.where_are_you.common.utils.CodeMaker.createCode;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupInviteCode;
import static com.potato_y.where_are_you.group.GroupTestUtils.createGroupMember;
import static com.potato_y.where_are_you.group.GroupTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
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
    assertThat(response.userResponse().getNickname()).isEqualTo(testUser.getNickname());
    assertThat(response.memberNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("getGroupResponse(): 그룹 정보를 조회할 수 있다. - 1명 그룹")
  void successGetGroupResponse_oneMember() {
    // given
    Group group = createGroup("test name", testUser);
    List<GroupMember> members = Collections.emptyList();

    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    GroupResponse response = groupService.getGroupResponse(1L);

    // then
    assertThat(response.groupName()).isEqualTo(group.getGroupName());
    assertThat(response.userResponse().getNickname()).isEqualTo(testUser.getNickname());
    assertThat(response.memberNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("getGroupResponse(): 그룹 정보를 조회할 수 있다. - 다수 인원의 그룹")
  void successGetGroupResponse_manyMember() {
    // given
    Group group = createGroup("test name", testUser);

    User memberUser = createUser("member@mail.com", "member 1", "2");

    GroupMember member = createGroupMember(group, memberUser);
    List<GroupMember> members = List.of(member, member, member);

    given(groupRepository.findById(any(Long.class))).willReturn(Optional.of(group));
    given(groupMemberRepository.findByGroup(group)).willReturn(members);

    // when
    GroupResponse response = groupService.getGroupResponse(1L);

    // then
    assertThat(response.groupName()).isEqualTo(group.getGroupName());
    assertThat(response.userResponse().getNickname()).isEqualTo(testUser.getNickname());
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

    given(createCode(any(Integer.class))).willReturn(code);
    given(groupInviteCodeRepository.findByCode(any(String.class))).willReturn(Optional.empty());

    // when
    String result = groupService.getUniqueInviteCode();

    // then
    assertThat(result).isEqualTo(code);
  }
}
