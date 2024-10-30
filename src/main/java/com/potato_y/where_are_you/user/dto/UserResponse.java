package com.potato_y.where_are_you.user.dto;

import com.potato_y.where_are_you.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserResponse {

  private Long userId;
  private String email;
  private String nickname;

  public UserResponse(User user) {
    this.userId = user.getId();
    this.email = user.getEmail();
    this.nickname = user.getNickname();
  }
}