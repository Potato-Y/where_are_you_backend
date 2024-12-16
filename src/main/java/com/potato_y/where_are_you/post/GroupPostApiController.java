package com.potato_y.where_are_you.post;

import com.potato_y.where_are_you.post.dto.CreatePostRequest;
import com.potato_y.where_are_you.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/groups/{groupId}/post")
public class GroupPostApiController {

  private final GroupPostService groupPostService;

  @PostMapping("")
  public ResponseEntity<PostResponse> createGroupPost(@PathVariable Long groupId,
      @Validated @ModelAttribute CreatePostRequest request) {
    PostResponse response = groupPostService.createGroupPost(groupId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{postId}")
  public ResponseEntity<PostResponse> getGroupPost(@PathVariable Long groupId,
      @PathVariable Long postId) {
    PostResponse response = groupPostService.getGroupPost(groupId, postId);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
