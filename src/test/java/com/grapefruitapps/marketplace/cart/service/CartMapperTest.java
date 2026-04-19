package com.grapefruitapps.marketplace.cart.service;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemDto;
import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartMapperTest {
    private final CartMapper cartMapper = new CartMapper();
    private Cart cart;

    @BeforeEach
    void init() {
        User buyer = User.builder()
                .id(1L)
                .username("alina9328")
                .name("Alina Vesnovskaya")
                .email("alinavesna0302@yandex.ru")
                .status(UserStatus.ACTIVE)
                .build();

       User seller = User.builder()
                .id(4L)
                .username("nick9429")
                .name("Nikita Lampovetz")
                .email("nicklamp421@gmail.com")
                .status(UserStatus.ACTIVE)
                .build();

       Product product1 = Product.builder()
                .id(7L)
                .name("RAM 8GB")
                .price(BigDecimal.valueOf(500.00))
                .seller(seller)
                .build();

       Product product2 = Product.builder()
                .id(8L)
                .name("Computer Mouse")
                .price(BigDecimal.valueOf(70.00))
                .seller(seller)
                .build();

        cart = new Cart(buyer);
        cart.setId(1L);

       CartItem cartItem1 = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product1)
                .quantity(4)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .id(2L)
                .cart(cart)
                .product(product2)
                .quantity(2)
                .build();

        cart.setCartItems(List.of(cartItem1, cartItem2));
    }

    @Test
    void toCartDto() {

       CartItemDto cartItemDto1 = new CartItemDto(
                1L,
                7L,
                "RAM 8GB",
                BigDecimal.valueOf(500.00),
                4,
                BigDecimal.valueOf(2000.00),
                4L,
                "Nikita Lampovetz"
        );

        CartItemDto cartItemDto2 = new CartItemDto(
                2L,
                8L,
                "Computer Mouse",
                BigDecimal.valueOf(70.00),
                2,
                BigDecimal.valueOf(140.00),
                4L,
                "Nikita Lampovetz"
        );

        CartDto expectedDto = new CartDto(
                List.of(cartItemDto1, cartItemDto2),
                6,
                BigDecimal.valueOf(2140.00)
        );

        CartDto actualDto = cartMapper.toCartDto(cart);
        assertEquals(expectedDto, actualDto);
    }
}