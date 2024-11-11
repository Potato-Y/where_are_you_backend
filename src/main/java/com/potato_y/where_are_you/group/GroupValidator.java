package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.user.domain.User;

public class GroupValidator {

  public static void validateGroupHostUser(Group group, User user) {
    if (!group.getHostUser().equals(user)) {
      throw new ForbiddenException("사용자가 그룹의 호스트가 아닙니다.");
    }
  }

  public static void validateGroupId(Group group, Long groupId) {
    if (!group.getId().equals(groupId)) {
      throw new BadRequestException("일정과 그룹 id가 일치하지 않습니다");
    }
  }
}
