package com.grapefruitapps.marketplace.user.dto;

import com.grapefruitapps.marketplace.user.entity.UserStatus;

public record UserDataFilter(
        String name,
        String email,
        String phone,
        UserStatus status,
        String role,
        Integer pageSize,
        Integer pageNumber
) {
}
