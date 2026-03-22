package com.grapefruitapps.marketplace.user.dto;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String username,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt
) {
}
