package com.grapefruitapps.marketplace.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCredentialsDto(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
