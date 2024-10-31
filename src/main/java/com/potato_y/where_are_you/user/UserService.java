package com.potato_y.where_are_you.user;

import static com.potato_y.where_are_you.authentication.utils.RandomStringGenerator.generateSecureRandomString;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import com.potato_y.where_are_you.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

  private static final int USER_RANDOM_PASSWORD_SIZE = 40;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;

  public User findById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
  }

  public User createUser(OAuthInfoResponse response) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    User user = User.builder()
        .serviceId(response.getId())
        .oAuthProvider(response.getOAuthProvider())
        .password(encoder.encode(generateSecureRandomString(USER_RANDOM_PASSWORD_SIZE)))
        .email(response.getEmail())
        .nickname(response.getNickname())
        .build();

    return userRepository.save(user);
  }

  public UserResponse getMyAccount() {
    return new UserResponse(currentUserProvider.getCurrentUser());
  }
}
