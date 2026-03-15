package com.grapefruitapps.marketplace.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterDto(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @Pattern(
                regexp = "^\\+?[0-9]+(?:-[0-9]+)*$",
                message = "Phone must contain only digits, optional plus at start, and hyphens between digits"
        )
        @Size(min = 7, max = 20, message = "Phone must be between 7 and 20 characters")
        String phone
) {
}
