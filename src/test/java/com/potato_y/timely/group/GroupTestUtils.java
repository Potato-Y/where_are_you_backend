package com.potato_y.timely.group;

import com.potato_y.timely.group.domain.Group;
import com.potato_y.timely.group.domain.GroupInviteCode;
import com.potato_y.timely.group.domain.GroupMember;
import com.potato_y.timely.group.domain.GroupMemberType;
import com.potato_y.timely.user.domain.User;

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

  public static GroupInviteCode createGroupInviteCode(String code, Long groupId) {
    return GroupInviteCode.builder()
        .code(code)
        .groupId(groupId)
        .build();
  }
}
