package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.user.domain.User;

public class GroupValidator {

  public static void validateGroupHostUser(Group group, User user) {
    if (!group.getHostUser().equals(user)) {
      throw new ForbiddenException("사용자가 그룹의 호스트가 아닙니다.");
    }
  }
}
