package com.grapefruitapps.marketplace.user.dto;

import com.grapefruitapps.marketplace.user.entity.UserStatus;

public record UserFilter(
        String name,
        String phone,
        String email,
        UserStatus status,
        String role,
        Integer pageSize,
        Integer pageNumber
) {
}
