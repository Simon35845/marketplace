package com.grapefruitapps.marketplace.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
        String name,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        @Digits(integer = 10, fraction = 2, message = "Price must have exactly 2 decimal places")
        BigDecimal price,

        @Size(max = 100, message = "Category must be less than 100 characters")
        String category,

        @Size(max = 1000, message = "Description must be less than 1000 characters")
        String description
) {
}