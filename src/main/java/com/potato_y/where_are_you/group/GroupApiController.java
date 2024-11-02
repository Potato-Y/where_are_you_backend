package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
import com.potato_y.where_are_you.group.dto.GroupResponse;
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
@RequestMapping("/v1/groups")
public class GroupApiController {

  private final GroupService groupService;

  @PostMapping("")
  public ResponseEntity<GroupResponse> createGroup(
      @Validated @RequestBody CreateGroupRequest request) {
    GroupResponse response = groupService.createGroup(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{groupId}")
  public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId) {
    GroupResponse response = groupService.getGroupResponse(groupId);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
