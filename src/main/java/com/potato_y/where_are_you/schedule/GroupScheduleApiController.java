package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.schedule.dto.GetGroupScheduleListResponse;
import com.potato_y.where_are_you.schedule.dto.GroupScheduleResponse;
import com.potato_y.where_are_you.user.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/groups/{groupId}/schedule")
public class GroupScheduleApiController {

  private final GroupScheduleService groupScheduleService;

  @PostMapping("")
  public ResponseEntity<GroupScheduleResponse> createGroupSchedule(@PathVariable Long groupId,
      @Validated @RequestBody CreateGroupScheduleRequest request) {
    GroupScheduleResponse response = groupScheduleService.createSchedule(groupId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<Void> deleteGroupSchedule(@PathVariable Long groupId,
      @PathVariable Long scheduleId) {
    groupScheduleService.deleteGroupSchedule(groupId, scheduleId);

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("")
  public ResponseEntity<List<GetGroupScheduleListResponse>> getGroupSchedules(
      @PathVariable Long groupId) {
    List<GetGroupScheduleListResponse> responses = groupScheduleService.getGroupSchedules(groupId);

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }

  @PostMapping("/{scheduleId}/participation")
  public ResponseEntity<Void> registerParticipation(@PathVariable Long groupId,
      @PathVariable Long scheduleId) {
    groupScheduleService.registerParticipation(groupId, scheduleId);

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/{scheduleId}/participation")
  public ResponseEntity<Void> cancelParticipation(@PathVariable Long groupId,
      @PathVariable Long scheduleId) {
    groupScheduleService.cancelParticipation(groupId, scheduleId);

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/{scheduleId}/participation")
  public ResponseEntity<List<UserResponse>> getParticipationList(@PathVariable Long groupId,
      @PathVariable Long scheduleId) {
    List<UserResponse> responses = groupScheduleService.getParticipationList(groupId, scheduleId);

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }
}
