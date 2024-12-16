package com.potato_y.where_are_you.post.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequest(
    @NotNull String title,
    @NotNull String content,
    List<MultipartFile> files
) {

  public boolean isFiles() {
    return files != null && !files.isEmpty();
  }
}
