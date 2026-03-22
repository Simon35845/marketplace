package com.grapefruitapps.marketplace.product.dto;

import java.math.BigDecimal;

public record ProductResponseDto (
        Long id,
        String name,
        BigDecimal price,
        String category,
        String description,
        Long sellerId
) {
}
