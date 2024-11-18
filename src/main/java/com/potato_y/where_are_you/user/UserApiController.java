package com.potato_y.where_are_you.user;

import com.potato_y.where_are_you.user.domain.UserLate;
import com.potato_y.where_are_you.user.dto.UserLateRequest;
import com.potato_y.where_are_you.user.dto.UserLateResponse;
import com.potato_y.where_are_you.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserApiController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyAccount() {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getMyAccount());
  }

  @PostMapping("/late")
  public ResponseEntity<UserLateResponse> upLateCount(
      @Validated @RequestBody UserLateRequest request) {
    UserLate userLate = userService.updateUserLate(request);

    return ResponseEntity.status(HttpStatus.OK).body(new UserLateResponse(userLate));
  }
}
