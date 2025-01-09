package com.potato_y.where_are_you.utils;

import com.potato_y.where_are_you.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtils {

  public static void setAuthentication(User user) {
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
  }
}
