package com.potato_y.timely.firebase.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(@NotBlank String token) {

}
