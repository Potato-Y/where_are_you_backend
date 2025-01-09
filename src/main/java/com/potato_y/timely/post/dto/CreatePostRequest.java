package com.potato_y.timely.post.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequest(
    @NotNull @Size(max = 20) String title,
    @NotNull String content,
    List<MultipartFile> files
) {

  public boolean isFiles() {
    return files != null && !files.isEmpty();
  }
}
