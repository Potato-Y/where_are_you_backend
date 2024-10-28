package com.potato_y.where_are_you.authentication;

import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CurrentUserProvider {

  private final UserRepository userRepository;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    return userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new NotFoundException("Unexpected user"));
  }
}
