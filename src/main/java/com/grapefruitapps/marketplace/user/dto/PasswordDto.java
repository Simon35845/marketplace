package com.grapefruitapps.marketplace.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordDto(
        @NotBlank(message = "Password is required")
        String currentPassword,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {
}
