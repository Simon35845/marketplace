package com.grapefruitapps.marketplace.cart.controller;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemDto;
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
@RequestMapping("/buyer/cart")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
@Validated
public class BuyerCartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCartByBuyerId(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getCartByBuyerId: buyer_id={}", userDetails.getId());
        CartDto cartDto = cartService.getCartByBuyerId(userDetails.getId());
        return ResponseEntity.ok(cartDto);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<CartItemDto> getCartItemById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getCartItemById: item_id={}, buyer_id={}",id, userDetails.getId());
        CartItemDto cartItemDto = cartService.getCartItemById(id, userDetails.getId());
        return ResponseEntity.ok(cartItemDto);
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
    public ResponseEntity<CartItemDto> changeItemQuantity(
            @PathVariable Long id,
            @RequestParam @Positive(message = "Quantity must be positive") Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called changeItemQuantity: item_id={}, quantity={}, buyer_id={}",
                id, quantity, userDetails.getId());
        CartItemDto cartItemDto = cartService.changeItemQuantity(id, quantity, userDetails.getId());
        return ResponseEntity.ok(cartItemDto);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteCartItem: item_id={}, buyer_id={}", id, userDetails.getId());
        cartService.deleteItem(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called clearCart: buyer_id={}", userDetails.getId());
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}