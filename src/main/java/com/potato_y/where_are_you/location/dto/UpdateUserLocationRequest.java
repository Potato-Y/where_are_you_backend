package com.potato_y.where_are_you.location.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserLocationRequest(
    @NotNull double locationLatitude,
    @NotNull double locationLongitude
) {

}
