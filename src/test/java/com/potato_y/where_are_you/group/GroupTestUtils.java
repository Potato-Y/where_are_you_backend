package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupInviteCode;
import com.potato_y.where_are_you.group.domain.GroupMember;
import com.potato_y.where_are_you.group.domain.GroupMemberType;
import com.potato_y.where_are_you.user.domain.User;

public class GroupTestUtils {

  public static Group createGroup(String groupName, User hostUser) {
    return Group.builder()
        .groupName(groupName)
        .hostUser(hostUser)
        .build();
  }

  public static GroupMember createGroupHost(Group group, User user) {
    return GroupMember.builder()
        .group(group)
        .user(user)
        .memberType(GroupMemberType.HOST)
        .build();
  }

  public static GroupMember createGroupMember(Group group, User user) {
    return GroupMember.builder()
        .group(group)
        .user(user)
        .memberType(GroupMemberType.MEMBER)
        .build();
  }

  public static GroupInviteCode createGroupInviteCode(Group group, User user, String code) {
    return GroupInviteCode.builder()
        .group(group)
        .createUser(user)
        .code(code)
        .build();
  }
}
