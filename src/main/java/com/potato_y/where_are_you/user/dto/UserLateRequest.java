package com.potato_y.where_are_you.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserLateRequest(
    @NotNull Boolean isLate
) {

}
