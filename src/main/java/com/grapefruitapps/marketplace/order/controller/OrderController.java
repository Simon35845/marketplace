package com.grapefruitapps.marketplace.order.controller;

import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderRequestDto;
import com.grapefruitapps.marketplace.order.service.OrderService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile/order")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getOrderById: order_id={}, user_id={}", id, userDetails.getId());
        return ResponseEntity.ok(orderService.getOrderById(id, userDetails.getId()));
    }

    @GetMapping("/buyer")
    public ResponseEntity<List<OrderDto>> getOrdersByBuyerId(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getOrdersByBuyerId: buyer_id={}", userDetails.getId());
        return ResponseEntity.ok(orderService.getOrdersByBuyerId(userDetails.getId()));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderDto>> getOrdersBySellerId(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getOrdersBySellerId: seller_id={}", userDetails.getId());
        return ResponseEntity.ok(orderService.getOrdersBySellerId(userDetails.getId()));
    }

    @PostMapping("/from-cart")
    public ResponseEntity<List<OrderDto>> createOrdersFromCart(
            @RequestBody @Valid OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called createOrdersFromCart: buyer_id={}", userDetails.getId());
        List<OrderDto> createdOrders = orderService.createOrdersFromCart(orderRequestDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrders);
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<OrderDto> changeItemQuantity(
            @PathVariable Long id,
            @RequestParam @Positive(message = "Quantity must be positive") Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called changeItemQuantity: item_id={}, quantity={}, buyer_id={}",
                id, quantity, userDetails.getId());
        OrderDto orderDto = orderService.changeItemQuantity(id, quantity, userDetails.getId());
        return ResponseEntity.ok(orderDto);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<OrderDto> deleteOrderItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteOrderItem: item_id={}, buyer_id={}", id, userDetails.getId());
        OrderDto orderDto = orderService.deleteItem(id, userDetails.getId());
        return ResponseEntity.ok(orderDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable Long id,
            @RequestBody @Valid OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called updateOrder: order_id={}, buyer_id={}", id, userDetails.getId());
        OrderDto updatedOrder = orderService.updateOrder(id, orderRequestDto, userDetails.getId());
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteOrder: order_id={}, buyer_id={}", id, userDetails.getId());
        orderService.deleteOrder(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/place")
    public ResponseEntity<Void> placeOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called placeOrder: order_id={}, buyer_id={}", id, userDetails.getId());
        orderService.placeOrder(id, userDetails.getId());
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> completeOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called completeOrder: order_id={}, buyer_id={}", id, userDetails.getId());
        orderService.completeOrder(id, userDetails.getId());
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called cancelOrder: order_id={}, buyer_id={}", id, userDetails.getId());
        orderService.cancelOrder(id, userDetails.getId());
        return ResponseEntity.accepted().build();
    }
}
