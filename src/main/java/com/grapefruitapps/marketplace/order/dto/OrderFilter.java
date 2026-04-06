package com.grapefruitapps.marketplace.order.dto;

import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;

public record OrderFilter(
        String orderNumber,
        Long buyerId,
        String buyerName,
        Long sellerId,
        String sellerName,
        DeliveryType deliveryType,
        OrderStatus status,
        String shippingAddress,
        Integer pageSize,
        Integer pageNumber
) {
}
