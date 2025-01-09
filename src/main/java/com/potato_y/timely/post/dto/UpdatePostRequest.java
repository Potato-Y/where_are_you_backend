package com.potato_y.timely.post.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
    @NotNull @Size(max = 20) String title,
    @NotNull String content
) {

}
