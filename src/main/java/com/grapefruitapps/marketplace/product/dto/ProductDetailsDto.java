package com.grapefruitapps.marketplace.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailsDto(
        Long id,
        String name,
        BigDecimal price,
        String category,
        String description,
        boolean isVisible,
        boolean isPublished,
        LocalDateTime creationDateTime
) {
}
