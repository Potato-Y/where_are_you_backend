package com.potato_y.where_are_you.user;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;

  public User findById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
  }

  public User createUser(OAuthInfoResponse response) {
    User user = User.builder()
        .serviceId(response.getId())
        .oAuthProvider(response.getOAuthProvider())
        .email(response.getEmail())
        .nickname(response.getNickname())
        .build();

    return userRepository.save(user);
  }
}
