package com.grapefruitapps.marketplace.user.dto;

public record AuthDto(
        String accessToken,
        String refreshToken
) {
}
