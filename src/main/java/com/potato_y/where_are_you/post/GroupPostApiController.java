package com.potato_y.where_are_you.post;

import com.potato_y.where_are_you.post.dto.CreatePostRequest;
import com.potato_y.where_are_you.post.dto.PostResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("")
  public ResponseEntity<List<PostResponse>> getGroupPosts(
      @PathVariable Long groupId,
      @RequestParam(defaultValue = "0") int page
  ) {
    List<PostResponse> responses = groupPostService.getGroupPosts(groupId, page);

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deleteGroupPost(@PathVariable Long groupId,
      @PathVariable Long postId) {
    groupPostService.deleteGroupPost(groupId, postId);

    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
