package com.potato_y.where_are_you.firebase;

import com.potato_y.where_are_you.firebase.dto.FcmTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/fcm")
public class FcmController {

  private final FcmService fcmService;

  @PostMapping("")
  public ResponseEntity<Void> saveOrUpdateFcmToken(
      @Validated @RequestBody FcmTokenRequest request) {
    fcmService.saveOrUpdateFcmToken(request);

    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
