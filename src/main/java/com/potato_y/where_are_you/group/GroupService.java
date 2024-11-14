package com.potato_y.where_are_you.group;

import static com.potato_y.where_are_you.common.utils.CodeMaker.createCode;
import static com.potato_y.where_are_you.group.GroupValidator.validateGroupHostUser;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupInviteCode;
import com.potato_y.where_are_you.group.domain.GroupInviteCodeRepository;
import com.potato_y.where_are_you.group.domain.GroupMember;
import com.potato_y.where_are_you.group.domain.GroupMemberRepository;
import com.potato_y.where_are_you.group.domain.GroupMemberType;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
import com.potato_y.where_are_you.group.dto.GroupInviteCodeResponse;
import com.potato_y.where_are_you.group.dto.GroupResponse;
import com.potato_y.where_are_you.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private static final int GROUP_INVITE_CODE_LENGTH = 10;

  private final GroupRepository groupRepository;
  private final CurrentUserProvider currentUserProvider;
  private final GroupMemberRepository groupMemberRepository;
  private final GroupInviteCodeRepository groupInviteCodeRepository;

  @Transactional
  public GroupResponse createGroup(CreateGroupRequest dto) {
    User hostUser = currentUserProvider.getCurrentUser();

    Group group = Group.builder()
        .groupName(dto.groupName())
        .hostUser(hostUser)
        .build();

    groupRepository.save(group);
    groupMemberRepository.save(GroupMember.builder()
        .group(group)
        .user(hostUser)
        .memberType(GroupMemberType.HOST)
        .build());

    return new GroupResponse(group, 1);
  }

  @Transactional(readOnly = true)
  public GroupResponse getGroupResponse(Long groupId) {
    Group group = groupRepository.findById(groupId).orElseThrow(NotFoundException::new);
    int memberCount = getGroupMembers(group).size();

    return new GroupResponse(group, memberCount); // host user 수를 추가
  }

  @Transactional(readOnly = true)
  public List<GroupMember> getGroupMembers(Group group) {
    return groupMemberRepository.findByGroup(group);
  }

  @Transactional
  public GroupInviteCodeResponse createInviteCode(Long groupId) {
    User user = currentUserProvider.getCurrentUser();
    Group group = groupRepository.findById(groupId).orElseThrow(NotFoundException::new);

    validateGroupHostUser(group, user);

    String code = getUniqueInviteCode();

    GroupInviteCode groupInviteCode = groupInviteCodeRepository.save(
        GroupInviteCode.builder()
            .group(group)
            .createUser(user)
            .code(code)
            .build());

    return new GroupInviteCodeResponse(groupId, groupInviteCode.getCode());
  }

  @Transactional(readOnly = true)
  protected String getUniqueInviteCode() {
    String code = createCode(GROUP_INVITE_CODE_LENGTH);

    while (groupInviteCodeRepository.findByCode(code).isPresent()) {
      code = createCode(GROUP_INVITE_CODE_LENGTH);
    }

    return code;
  }

  @Transactional
  public GroupResponse updateGroup(Long groupId, CreateGroupRequest request) {
    User user = currentUserProvider.getCurrentUser();
    Group group = groupRepository.findById(groupId).orElseThrow(NotFoundException::new);

    validateGroupHostUser(group, user);

    group.updateGroupName(request.groupName());

    return new GroupResponse(group, getGroupMembers(group).size());
  }

  @Transactional
  public GroupResponse signupGroup(String inviteCode) {
    User user = currentUserProvider.getCurrentUser();

    GroupInviteCode codeInfo = groupInviteCodeRepository.findByCode(inviteCode)
        .orElseThrow(NotFoundException::new);
    Group group = codeInfo.getGroup();

    groupMemberRepository.findByGroupAndUser(group, user)
        .ifPresent(v -> {
          throw new BadRequestException("이미 가입된 사용자입니다.");
        });

    if (group.getHostUser().equals(user)) {
      throw new BadRequestException("호스트는 가입할 수 없습니다.");
    }

    groupMemberRepository.save(GroupMember.builder()
        .group(group)
        .user(user)
        .memberType(GroupMemberType.MEMBER)
        .build());

    return new GroupResponse(group, getGroupMembers(group).size());
  }

  @Transactional(readOnly = true)
  public List<GroupResponse> getGroupList() {
    User user = currentUserProvider.getCurrentUser();

    List<GroupResponse> responses = new ArrayList<>();

    List<GroupMember> inGroups = groupMemberRepository.findByUser(user);
    inGroups.forEach(it -> responses.add(
        new GroupResponse(it.getGroup(), getGroupMembers(it.getGroup()).size())));

    return responses;
  }

  @Transactional
  public void deleteOrLeaveGroup(Long groupId) {
    User user = currentUserProvider.getCurrentUser();

    Group group = groupRepository.findById(groupId).orElseThrow(NotFoundException::new);
    if (group.getHostUser().equals(user)) {
      groupRepository.delete(group);
      return;
    }

    GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, user)
        .orElseThrow(() -> new BadRequestException("사용자가 그룹의 멤버가 아닙니다."));
    groupMemberRepository.delete(groupMember);
  }

  @Transactional(readOnly = true)
  public Boolean checkGroupMember(Long groupId, User user) {
    Group group = findByGroup(groupId);

    return groupMemberRepository.findByGroupAndUser(group, user).isPresent();
  }

  @Transactional(readOnly = true)
  public Group findByGroup(Long groupId) {
    return groupRepository.findById(groupId)
        .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));
  }
}
