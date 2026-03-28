package com.grapefruitapps.marketplace.cart.controller;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemRequestDto;
import com.grapefruitapps.marketplace.cart.service.CartService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile/cart")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getCart: buyer_id={}", userDetails.getId());
        CartDto cartDto = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDto> clearCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called clearCart: buyer_id={}", userDetails.getId());
        CartDto cartDto = cartService.clearCart(userDetails.getId());
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(
            @RequestBody @Valid CartItemRequestDto itemDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called addItemToCart: product_id={}, quantity={}, buyer_id={}",
                itemDto.productId(), itemDto.quantity(), userDetails.getId());
        CartDto cartDto = cartService.addItemToCart(itemDto, userDetails.getId());
        return ResponseEntity.ok(cartDto);
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<CartDto> changeItemQuantity(
            @PathVariable Long id,
            @RequestParam @Positive(message = "Quantity must be positive") Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called changeItemQuantity: item_id={}, quantity={}, buyer_id={}",
                id, quantity, userDetails.getId());
        CartDto cartDto = cartService.changeItemQuantity(id, quantity, userDetails.getId());
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartDto> deleteCartItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteCartItem: item_id={}, buyer_id={}", id, userDetails.getId());
        CartDto cartDto = cartService.deleteItem(id, userDetails.getId());
        return ResponseEntity.ok(cartDto);
    }
}