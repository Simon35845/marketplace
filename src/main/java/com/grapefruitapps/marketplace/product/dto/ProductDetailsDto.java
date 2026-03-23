package com.grapefruitapps.marketplace.product.dto;

import com.grapefruitapps.marketplace.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailsDto(
        Long id,
        String name,
        BigDecimal price,
        String category,
        String description,
        ProductStatus status,
        LocalDateTime creationDateTime,
        LocalDateTime saleDateTime
) {
}
