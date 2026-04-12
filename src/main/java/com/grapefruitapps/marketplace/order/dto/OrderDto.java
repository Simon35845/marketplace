package com.grapefruitapps.marketplace.order.dto;

import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        Long id,
        String orderNumber,
        Long buyerId,
        String buyerName,
        Long sellerId,
        String sellerName,
        List<OrderItemDto> items,
        Integer numberOfItems,
        BigDecimal totalPrice,
        DeliveryType deliveryType,
        OrderStatus status,
        String shippingAddress,
        String creationDateTime
) {
}
