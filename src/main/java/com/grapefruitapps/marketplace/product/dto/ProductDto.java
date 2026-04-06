package com.grapefruitapps.marketplace.product.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        BigDecimal price,
        String category,
        String description,
        Long sellerId,
        String sellerName
) {
}
