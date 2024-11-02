package com.potato_y.where_are_you.group.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotNull @Size(max = 20) String groupName
) {

}
