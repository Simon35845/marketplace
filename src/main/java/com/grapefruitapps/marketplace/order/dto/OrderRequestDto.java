package com.grapefruitapps.marketplace.order.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderRequestDto(
        @NotBlank(message = "Shipping address is required")
        String shippingAddress
) {
}
