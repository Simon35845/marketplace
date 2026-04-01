package com.grapefruitapps.marketplace.order.dto;

import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDto(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
