package com.potato_y.where_are_you.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotBlank @Size(max = 20) String groupName
) {

}
