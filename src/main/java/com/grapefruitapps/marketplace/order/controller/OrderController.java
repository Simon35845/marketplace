package com.grapefruitapps.marketplace.order.controller;

import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderRequestDto;
import com.grapefruitapps.marketplace.order.service.OrderService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile/order")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
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
}
