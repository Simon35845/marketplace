package com.grapefruitapps.marketplace.product.dto;

public record ProductDetailsFilter(
        String name,
        String category,
        Boolean isVisible,
        Boolean isPublished,
        Integer pageSize,
        Integer pageNumber
) {
}
