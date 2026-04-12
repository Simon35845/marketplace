package com.grapefruitapps.marketplace.order.controller;

import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderFilter;
import com.grapefruitapps.marketplace.order.dto.OrderItemDto;
import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import com.grapefruitapps.marketplace.order.service.OrderService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/order")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
public class SellerOrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getOrderById: order_id={}, user_id={}", id, userDetails.getId());
        OrderDto orderDto = orderService.getOrderById(id, userDetails.getId());
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<OrderItemDto> getOrderItemById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getOrderItemById: order_id={}, user_id={}", id, userDetails.getId());
        OrderItemDto orderItemDto = orderService.getOrderItemById(id, userDetails.getId());
        return ResponseEntity.ok(orderItemDto);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrdersByFilter(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) String buyerName,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) DeliveryType deliveryType,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer pageNumber,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        OrderFilter filter = new OrderFilter(
                orderNumber,
                buyerId,
                buyerName,
                sellerId,
                sellerName,
                deliveryType,
                status,
                shippingAddress,
                pageSize,
                pageNumber
        );

        log.info("Called getOrdersByFilter: seller_id={}", userDetails.getId());
        List<OrderDto> orderDtoList = orderService.getSellerOrdersByFilter(filter, userDetails.getId());
        return ResponseEntity.ok(orderDtoList);
    }
}
