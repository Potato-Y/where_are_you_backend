package com.potato_y.where_are_you.location;

import com.potato_y.where_are_you.location.dto.UpdateUserLocationRequest;
import com.potato_y.where_are_you.location.dto.UserLocationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/locations")
public class LocationShareApiController {

  private final LocationShareService locationShareService;

  @PostMapping("")
  public ResponseEntity<List<UserLocationResponse>> updateUserLocation(
      @Validated @RequestBody UpdateUserLocationRequest request) {
    List<UserLocationResponse> responses = locationShareService.updateUserLocation(request);

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<List<UserLocationResponse>> getScheduleMemberLocations(
      @PathVariable Long scheduleId) {
    List<UserLocationResponse> responses = locationShareService.getScheduleMemberLocations(
        scheduleId);

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }
}
