package com.potato_y.timely.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserLateRequest(
    @NotNull Boolean isLate
) {

}
