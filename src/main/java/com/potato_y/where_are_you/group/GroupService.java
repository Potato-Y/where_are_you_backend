package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupMember;
import com.potato_y.where_are_you.group.domain.GroupMemberRepository;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
import com.potato_y.where_are_you.group.dto.GroupResponse;
import com.potato_y.where_are_you.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final CurrentUserProvider currentUserProvider;
  private final GroupMemberRepository groupMemberRepository;

  @Transactional
  public GroupResponse createGroup(CreateGroupRequest dto) {
    User hostUser = currentUserProvider.getCurrentUser();

    Group group = Group.builder()
        .groupName(dto.groupName())
        .hostUser(hostUser)
        .build();

    groupRepository.save(group);
    return new GroupResponse(group, 1);
  }

  @Transactional(readOnly = true)
  public GroupResponse getGroupResponse(Long groupId) {
    Group group = groupRepository.findById(groupId).orElseThrow(NotFoundException::new);
    int memberCount = getGroupMembers(group).size();

    return new GroupResponse(group, memberCount + 1); // host user 수를 추가
  }

  @Transactional(readOnly = true)
  protected List<GroupMember> getGroupMembers(Group group) {
    return groupMemberRepository.findByGroup(group);
  }
}
