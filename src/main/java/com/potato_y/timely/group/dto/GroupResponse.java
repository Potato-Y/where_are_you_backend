package com.potato_y.timely.group.dto;

import com.potato_y.timely.group.domain.Group;
import com.potato_y.timely.user.dto.UserResponse;
import java.time.LocalDateTime;

public record GroupResponse(
    Long id,
    String groupName,
    UserResponse hostUser,
    int coverColor,
    LocalDateTime createAt,
    int memberNumber
) {

  public GroupResponse(Group group, int memberNumber) {
    this(
        group.getId(),
        group.getGroupName(),
        new UserResponse(group.getHostUser()),
        group.getCoverColor(),
        group.getCreatedAt(),
        memberNumber
    );
  }
}
