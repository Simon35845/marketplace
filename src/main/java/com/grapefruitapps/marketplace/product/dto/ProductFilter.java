package com.grapefruitapps.marketplace.product.dto;

public record ProductFilter (
        String name,
        String category,
        Long sellerId,
        String sellerName,
        Integer pageSize,
        Integer pageNumber
) {
}
