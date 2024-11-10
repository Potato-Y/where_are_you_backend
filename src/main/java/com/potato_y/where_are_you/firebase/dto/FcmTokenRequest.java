package com.potato_y.where_are_you.firebase.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(@NotBlank String token) {

}
