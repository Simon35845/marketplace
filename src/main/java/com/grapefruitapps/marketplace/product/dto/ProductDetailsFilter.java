package com.grapefruitapps.marketplace.product.dto;

import com.grapefruitapps.marketplace.product.entity.ProductStatus;

public record ProductDetailsFilter(
        String name,
        String category,
        ProductStatus status,
        Integer pageSize,
        Integer pageNumber
) {
}
