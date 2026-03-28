package com.grapefruitapps.marketplace.cart.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record CartItemRequestDto(
        @NotNull(message = "Product id is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity
) {
}
