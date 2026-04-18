package com.grapefruitapps.marketplace.order.service;

import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderItemDto;
import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.order.entity.OrderItem;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {
    private final OrderMapper orderMapper = new OrderMapper();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime creationDateTime = LocalDateTime.now();
    private final String formattedDateTime = creationDateTime.format(FORMATTER);

    @Test
    void toOrderDtoTest() {
        User buyer = User.builder()
                .id(1L)
                .username("pavel2302")
                .name("Pavel Bashmakov")
                .build();

        User seller = User.builder()
                .id(2L)
                .username("yanlanbitz443")
                .name("Yan Lanbitz")
                .build();

        Product product1 = Product.builder()
                .id(1L)
                .name("Skateboard")
                .price(BigDecimal.valueOf(230.00))
                .seller(seller)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Tomato seeds")
                .price(BigDecimal.valueOf(12.00))
                .seller(seller)
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderNumber("Y923MB72")
                .buyer(buyer)
                .seller(seller)
                .totalPrice(BigDecimal.valueOf(266.00))
                .deliveryType(DeliveryType.PAYMENT_ON_DELIVERY)
                .status(OrderStatus.PENDING)
                .shippingAddress("Belarus, Minsk")
                .creationDateTime(creationDateTime)
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product1)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(230.00))
                .subTotalPrice(BigDecimal.valueOf(230.00))
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .id(2L)
                .order(order)
                .product(product2)
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(12.00))
                .subTotalPrice(BigDecimal.valueOf(36.00))
                .build();

        order.setOrderItems(List.of(orderItem1, orderItem2));

        OrderItemDto orderItemDto1 = new OrderItemDto(
                1L,
                1L,
                "Skateboard",
                BigDecimal.valueOf(230.00),
                1,
                BigDecimal.valueOf(230.00)
        );

        OrderItemDto orderItemDto2 = new OrderItemDto(
                2L,
                2L,
                "Tomato seeds",
                BigDecimal.valueOf(12.00),
                3,
                BigDecimal.valueOf(36.00)
        );

        OrderDto expectedOrderDto = new OrderDto(
                1L,
                "Y923MB72",
                1L,
                "Pavel Bashmakov",
                2L,
                "Yan Lanbitz",
                List.of(orderItemDto1, orderItemDto2),
                4,
                BigDecimal.valueOf(266.00),
                DeliveryType.PAYMENT_ON_DELIVERY,
                OrderStatus.PENDING,
                "Belarus, Minsk",
                formattedDateTime
        );

        OrderDto result = orderMapper.toOrderDto(order);
        assertEquals(expectedOrderDto, result);
    }
}