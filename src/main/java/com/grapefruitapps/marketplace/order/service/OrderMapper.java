package com.grapefruitapps.marketplace.order.service;

import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderItemDto;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {
    public OrderItemDto toOrderItemDto(OrderItem item){
        return new OrderItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubTotalPrice()
        );
    }

    public OrderDto toOrderDto(Order order){
        List<OrderItemDto> itemDtos = new ArrayList<>();
        int numberOfItems = 0;
        for(OrderItem item: order.getOrderItems()){
            OrderItemDto itemDto = toOrderItemDto(item);
            itemDtos.add(itemDto);
            numberOfItems += itemDto.quantity();
        }

        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getBuyer().getId(),
                order.getBuyer().getName(),
                order.getSeller().getId(),
                order.getSeller().getName(),
                itemDtos,
                numberOfItems,
                order.getTotalPrice(),
                order.getDeliveryType(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getCreationDateTime()
        );
    }
}
