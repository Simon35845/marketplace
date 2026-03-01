package com.grapefruitapps.marketplace.exception;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String message,
        String detailedMessage,
        LocalDateTime errorDateTime
) {
}
