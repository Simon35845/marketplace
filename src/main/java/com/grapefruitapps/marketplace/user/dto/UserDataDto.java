package com.grapefruitapps.marketplace.user.dto;

import com.grapefruitapps.marketplace.user.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record UserDataDto(
        Long id,
        String username,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt,
        UserStatus status,
        List<String> roles
) {
}
