package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupMember;
import com.potato_y.where_are_you.user.domain.User;

public class GroupTestUtils {

  public static User createUser(String email, String nickname, String serviceId) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .serviceId(serviceId)
        .oAuthProvider(OAuthProvider.KAKAO)
        .build();
  }

  public static Group createGroup(String groupName, User hostUser) {
    return Group.builder()
        .groupName(groupName)
        .hostUser(hostUser)
        .build();
  }

  public static GroupMember createGroupMember(Group group, User user) {
    return GroupMember.builder()
        .group(group)
        .user(user)
        .build();
  }
}
