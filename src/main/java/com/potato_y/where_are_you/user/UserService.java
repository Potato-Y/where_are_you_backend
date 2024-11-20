package com.potato_y.where_are_you.user;

import static com.potato_y.where_are_you.authentication.utils.RandomStringGenerator.generateSecureRandomString;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthInfoResponse;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserLate;
import com.potato_y.where_are_you.user.domain.UserLateRepository;
import com.potato_y.where_are_you.user.domain.UserRepository;
import com.potato_y.where_are_you.user.dto.UserLateRequest;
import com.potato_y.where_are_you.user.dto.UserLateResponse;
import com.potato_y.where_are_you.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

  private static final int USER_RANDOM_PASSWORD_SIZE = 40;
  private final UserRepository userRepository;
  private final UserLateRepository userLateRepository;
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

  @Transactional
  public UserLateResponse updateUserLate(UserLateRequest dto) {
    User user = currentUserProvider.getCurrentUser();
    UserLate userLate = userLateRepository.findByUser(user)
        .orElseGet(() -> createUserLate(user));

    userLate.upCount(dto.isLate());
    return new UserLateResponse(userLate);
  }

  @Transactional
  public UserLate getUserLate(Long userId) {
    User user = findById(userId);
    return userLateRepository.findByUser(user)
        .orElseGet(() -> createUserLate(user));
  }

  private UserLate createUserLate(User user) {
    return userLateRepository.save(UserLate.builder()
        .user(user)
        .build());
  }
}
