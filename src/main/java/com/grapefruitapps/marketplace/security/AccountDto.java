package com.grapefruitapps.marketplace.security;

import com.grapefruitapps.marketplace.profile.ProfileDto;

import java.time.LocalDateTime;
import java.util.List;

public record AccountDto(
        Long id,
        String username,
        LocalDateTime createdAt,
        AccountStatus status,
        List<String> roles,
        ProfileDto profile

) {
}
