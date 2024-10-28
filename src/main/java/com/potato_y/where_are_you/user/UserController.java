package com.potato_y.where_are_you.user;

import com.potato_y.where_are_you.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyAccount() {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getMyAccount());
  }
}
