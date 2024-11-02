package com.potato_y.where_are_you.group.dto;

import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.user.dto.UserResponse;
import java.time.LocalDateTime;

public record GroupResponse(
    Long id,
    String groupName,
    UserResponse userResponse,
    LocalDateTime createAt,
    int memberNumber
) {

  public GroupResponse(Group group, int memberNumber) {
    this(
        group.getId(),
        group.getGroupName(),
        new UserResponse(group.getHostUser()),
        group.getCreatedAt(),
        memberNumber
    );
  }
}
