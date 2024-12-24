package com.potato_y.where_are_you.post.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
    @NotNull @Size(max = 20) String title,
    @NotNull String content
) {

}
