package com.potato_y.where_are_you.post.dto;

import com.potato_y.where_are_you.post.domain.Post;
import com.potato_y.where_are_you.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record PostResponse(
    Long postId,
    Long groupId,
    UserResponse createUser,
    String title,
    String content,
    LocalDateTime createAt,
    LocalDateTime updateAt,
    List<String> files
) {

  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getId(),
        post.getGroup().getId(),
        new UserResponse(post.getUser()),
        post.getTitle(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdateAt(),
        new ArrayList<>()
    );
  }

  public static PostResponse from(Post post, List<String> files) {
    return new PostResponse(
        post.getId(),
        post.getGroup().getId(),
        new UserResponse(post.getUser()),
        post.getTitle(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdateAt(),
        files
    );
  }
}
