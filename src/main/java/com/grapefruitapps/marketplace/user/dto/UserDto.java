package com.grapefruitapps.marketplace.user.dto;

public record UserDto(
        Long id,
        String name,
        String email,
        String phone,
        String creationDateTime
) {
}
