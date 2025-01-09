package com.potato_y.timely.location.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserLocationRequest(
    @NotNull double locationLatitude,
    @NotNull double locationLongitude
) {

}
