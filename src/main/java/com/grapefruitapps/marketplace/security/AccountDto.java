package com.grapefruitapps.marketplace.security;

import com.grapefruitapps.marketplace.profile.ProfileDto;

import java.time.LocalDateTime;
import java.util.List;

public record AccountDto(
        Long id,
        String username,
        AccountStatus status,
        LocalDateTime createdAt,
        ProfileDto profile,
        List<String> roles
) {
}
