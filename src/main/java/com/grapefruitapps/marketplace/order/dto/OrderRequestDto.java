package com.grapefruitapps.marketplace.order.dto;

import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OrderRequestDto(
        @NotNull(message = "Order type is required")
        @Pattern(regexp = "PREPAYMENT|PAYMENT_ON_DELIVERY|PICKUP",
                message = "Permitted order types: PREPAYMENT, PAYMENT_ON_DELIVERY, PICKUP")
        DeliveryType deliveryType,

        String shippingAddress
) {

    @AssertTrue(message = "Shipping address is required for delivery (PREPAYMENT or PAYMENT_ON_DELIVERY)")
    public boolean isShippingAddressValid() {
        return switch (deliveryType) {
            case PICKUP -> shippingAddress == null;
            case PREPAYMENT, PAYMENT_ON_DELIVERY ->
                    shippingAddress != null && !shippingAddress.isBlank();
        };
    }
}
