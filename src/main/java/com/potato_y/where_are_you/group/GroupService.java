package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupRepository;
import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
import com.potato_y.where_are_you.group.dto.GroupResponse;
import com.potato_y.where_are_you.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final CurrentUserProvider currentUserProvider;

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
}
