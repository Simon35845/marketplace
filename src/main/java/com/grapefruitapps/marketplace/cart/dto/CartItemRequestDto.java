package com.grapefruitapps.marketplace.cart.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record CartItemRequestDto(
        @NotNull(message = "Product id is required")
        Long productId,

        @Positive(message = "Quantity of products must be positive")
        Integer quantity
) {
}
