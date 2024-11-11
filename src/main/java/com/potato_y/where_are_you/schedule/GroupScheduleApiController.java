package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.schedule.dto.GroupScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
}
