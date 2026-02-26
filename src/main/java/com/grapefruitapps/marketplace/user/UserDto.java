package com.grapefruitapps.marketplace.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserDto(
        Long id,
        @NotNull
        @Size(min = 2, max = 100, message = "User name must be between 3 and 100 characters")
        String name,
        @Pattern(
                regexp = "^\\+?[0-9]+(?:-[0-9]+)*$",
                message = "Phone must contain only digits, optional plus at start, and hyphens between digits"
        )
        @Size(min = 7, max = 20, message = "Phone must be between 7 and 20 characters")
        String phone,
        @NotNull
        @Size(min = 10, max = 50, message = "Email must be between 10 and 50 characters")
        String email
) {
}
