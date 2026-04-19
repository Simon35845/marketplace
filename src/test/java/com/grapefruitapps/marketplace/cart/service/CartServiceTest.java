package com.grapefruitapps.marketplace.cart.service;

import com.grapefruitapps.marketplace.cart.dto.CartItemDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemRequestDto;
import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.cart.repository.CartItemRepository;
import com.grapefruitapps.marketplace.cart.repository.CartRepository;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private CartItemDto cartItemDto;

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

        product = Product.builder()
                .id(7L)
                .name("RAM 8GB")
                .price(BigDecimal.valueOf(500.00))
                .isVisible(true)
                .isPublished(true)
                .seller(seller)
                .build();

        cart = new Cart(buyer);

        cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(4)
                .build();

        cartItemDto = new CartItemDto(
                1L,
                7L,
                "RAM 8GB",
                BigDecimal.valueOf(500.00),
                4,
                BigDecimal.valueOf(2000.00),
                4L,
                "Nikita Lampovetz"
        );
    }

    @Test
    void addItemToCartTest_createNewCartItem() {
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto(7L, 4);
        Long buyerId = 1L;

        when(userService.findUserById(buyerId)).thenReturn(cart.getBuyer());
        when(cartRepository.findByBuyerId(buyerId)).thenReturn(Optional.of(cart));
        doNothing().when(userService).checkUserActivity(cart.getBuyer());
        when(productService.findProductById(cartItemRequestDto.productId())).thenReturn(product);
        when(productService.isProductOwner(product, buyerId)).thenReturn(false);
        doNothing().when(productService).checkProductAvailability(product);
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartMapper.toCartItemDto(cartItem)).thenReturn(cartItemDto);

        cartService.addItemToCart(cartItemRequestDto, buyerId);

        verify(cartRepository).findByBuyerId(buyerId);
        verify(productService).checkProductAvailability(product);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCartTest_buyerIsProductOwner() {
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto(7L, 4);
        Long buyerId = 1L;

        when(userService.findUserById(buyerId)).thenReturn(cart.getBuyer());
        when(cartRepository.findByBuyerId(buyerId)).thenReturn(Optional.of(cart));
        doNothing().when(userService).checkUserActivity(cart.getBuyer());
        when(productService.findProductById(cartItemRequestDto.productId())).thenReturn(product);
        when(productService.isProductOwner(product, buyerId)).thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                ()->cartService.addItemToCart(cartItemRequestDto, 1L));

        assertEquals("Cannot add your own product to cart", exception.getMessage());
        verify(cartRepository).findByBuyerId(buyerId);
        verify(userService).checkUserActivity(cart.getBuyer());
    }
}