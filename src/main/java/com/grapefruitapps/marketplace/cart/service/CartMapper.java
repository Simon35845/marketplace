package com.grapefruitapps.marketplace.cart.service;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemDto;
import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {
    public CartItemDto toCartItemDto(CartItem item){
        BigDecimal subTotalPrice = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                subTotalPrice,
                item.getProduct().getSeller().getId(),
                item.getProduct().getSeller().getName()
        );
    }

    public CartDto toCartDto(Cart cart) {
        List<CartItemDto> items = new ArrayList<>();
        int numberOfItems = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cart.getCartItems()) {
            CartItemDto itemDto = toCartItemDto(item);
            items.add(itemDto);
            numberOfItems += itemDto.quantity();
            totalPrice = totalPrice.add(itemDto.subTotalPrice());
        }
        return new CartDto(items, numberOfItems, totalPrice);
    }
}
