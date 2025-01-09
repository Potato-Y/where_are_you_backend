package com.potato_y.timely.location;

import com.potato_y.timely.location.dto.ShareLocationResponse;
import com.potato_y.timely.location.dto.StateMessage.StateMessageRequest;
import com.potato_y.timely.location.dto.StateMessage.StateMessageResponse;
import com.potato_y.timely.location.dto.UpdateUserLocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/locations")
public class LocationShareApiController {

  private final LocationShareService locationShareService;

  @PutMapping("/{scheduleId}")
  public ResponseEntity<Void> updateUserLocation(
      @PathVariable Long scheduleId, @Validated @RequestBody UpdateUserLocationRequest request) {
    locationShareService.updateUserLocation(scheduleId, request);

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/{scheduleId}/state-message")
  public ResponseEntity<StateMessageResponse> updateStateMessage(@PathVariable Long scheduleId,
      @RequestBody StateMessageRequest request) {
    String message = locationShareService.updateStateMessage(scheduleId, request);

    return ResponseEntity.status(HttpStatus.OK).body(new StateMessageResponse(message));
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<ShareLocationResponse> getScheduleMemberLocations(
      @PathVariable Long scheduleId) {
    ShareLocationResponse responses = locationShareService.getScheduleMemberLocations(
        scheduleId);

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }
}
