package com.grapefruitapps.marketplace.product.dto;

import java.math.BigDecimal;

public record ProductDataDto(
        Long id,
        String name,
        BigDecimal price,
        String category,
        String description,
        boolean isVisible,
        boolean isPublished,
        String creationDateTime
) {
}
