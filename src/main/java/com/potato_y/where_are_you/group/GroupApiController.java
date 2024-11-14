package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.group.dto.CreateGroupRequest;
import com.potato_y.where_are_you.group.dto.GroupInviteCodeResponse;
import com.potato_y.where_are_you.group.dto.GroupResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PutMapping("/{groupId}")
  public ResponseEntity<GroupResponse> updateGroup(@PathVariable Long groupId,
      @Validated @RequestBody CreateGroupRequest request) {
    GroupResponse response = groupService.updateGroup(groupId, request);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/{groupId}/invite-code")
  public ResponseEntity<GroupInviteCodeResponse> createGroupInviteCode(@PathVariable Long groupId) {
    GroupInviteCodeResponse response = groupService.createInviteCode(groupId);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/signup/{inviteCode}")
  public ResponseEntity<GroupResponse> signupGroup(@PathVariable String inviteCode) {
    GroupResponse response = groupService.signupGroup(inviteCode);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("")
  public ResponseEntity<List<GroupResponse>> getGroupList() {
    List<GroupResponse> responses = groupService.getGroupList();

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }

  @DeleteMapping("/{groupId}")
  public ResponseEntity<Void> deleteOrLeaveGroup(@PathVariable Long groupId) {
    groupService.deleteOrLeaveGroup(groupId);

    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
