package com.grapefruitapps.marketplace.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        @NotNull
        @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
        String name,
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,
        String category,
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}