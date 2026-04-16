package com.grapefruitapps.marketplace.order.service;

import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.cart.service.CartService;
import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderItemDto;
import com.grapefruitapps.marketplace.order.dto.OrderRequestDto;
import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.order.entity.OrderItem;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import com.grapefruitapps.marketplace.order.repository.OrderItemRepository;
import com.grapefruitapps.marketplace.order.repository.OrderRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private OrderNumberGenerator generator;

    @InjectMocks
    private OrderService orderService;

    private LocalDateTime dateTime;
    private String formattedDateTime;
    private User buyer;
    private User seller1;
    private User seller2;
    private Product product1;
    private Product product2;
    private Product product3;
    private Cart cart;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private CartItem cartItem3;
    private OrderRequestDto orderRequestDto;
    private OrderDto orderDto1;
    private OrderDto orderDto2;
    private OrderItemDto orderItemDto1;
    private OrderItemDto orderItemDto2;
    private OrderItemDto orderItemDto3;
    private Order order1;
    private Order order2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private OrderItem orderItem3;

    @BeforeEach
    void init() {
        initDateTime();
        initUsers();
        initProducts();
        initCart();
        initOrders();
        initDto();
    }

    @Test
    void createOrdersFromCart() {
        when(cartService.findCartByBuyerIdWithAllDetails(buyer.getId())).thenReturn(cart);
        doNothing().when(userService).checkUserActivity(buyer);
        doNothing().when(cartService).checkCartIsEmpty(cart);

        when(userService.findUserById(seller1.getId())).thenReturn(seller1);
        doNothing().when(userService).checkUserActivity(seller1);
        when(userService.findUserById(seller2.getId())).thenReturn(seller2);
        doNothing().when(userService).checkUserActivity(seller2);

        when(generator.generate()).thenReturn("Y923MB72", "B534GG3N");
        when(productService.isProductOwner(cartItem1.getProduct(), seller1.getId())).thenReturn(true);
        when(productService.isProductOwner(cartItem2.getProduct(), seller1.getId())).thenReturn(true);
        when(productService.isProductOwner(cartItem3.getProduct(), seller1.getId())).thenReturn(false);
        when(productService.isProductOwner(cartItem1.getProduct(), seller2.getId())).thenReturn(false);
        when(productService.isProductOwner(cartItem2.getProduct(), seller2.getId())).thenReturn(false);
        when(productService.isProductOwner(cartItem2.getProduct(), seller2.getId())).thenReturn(true);

        doNothing().when(productService).checkProductAvailability(any(Product.class));
        when(orderRepository.save(any(Order.class))).thenReturn(order1, order2);
        doNothing().when(cartService).clearCart(buyer.getId());

        when(orderMapper.toOrderDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toOrderDto(order2)).thenReturn(orderDto2);

        List<OrderDto> result = orderService.createOrdersFromCart(orderRequestDto, buyer.getId());
        List<OrderDto> expected = List.of(orderDto1, orderDto2);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected, result);

        verify(productService, times(3)).checkProductAvailability(any(Product.class));
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cartService).clearCart(buyer.getId());
    }

    private void initDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        dateTime = LocalDateTime.now();
        formattedDateTime = dateTime.format(formatter);
    }

    private void initUsers() {
        buyer = User.builder()
                .id(1L)
                .username("pavel2302")
                .name("Pavel Bashmakov")
                .email("pavelbashmakov@gmail.com")
                .status(UserStatus.ACTIVE)
                .build();

        seller1 = User.builder()
                .id(2L)
                .username("elena1882")
                .name("Yan Lanbitz")
                .email("yandhanbitz@gmail.com")
                .status(UserStatus.ACTIVE)
                .build();

        seller2 = User.builder()
                .id(3L)
                .username("victoria12814")
                .name("Viktoria Berry")
                .email("victoriaberry@gmail.com")
                .status(UserStatus.ACTIVE)
                .build();
    }

    private void initProducts() {
        product1 = Product.builder()
                .id(1L)
                .name("Skateboard")
                .price(BigDecimal.valueOf(230.00))
                .isVisible(true)
                .isPublished(true)
                .seller(seller1)
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Tomato seeds")
                .price(BigDecimal.valueOf(12.00))
                .isVisible(true)
                .isPublished(true)
                .seller(seller1)
                .build();

        product3 = Product.builder()
                .id(3L)
                .name("Painting")
                .price(BigDecimal.valueOf(180.00))
                .isVisible(true)
                .isPublished(true)
                .seller(seller2)
                .build();
    }

    private void initCart() {
        cart = new Cart(buyer);
        cart.setId(1L);

        cartItem1 = new CartItem(cart, product1, 1);
        cartItem1.setId(1L);
        cartItem2 = new CartItem(cart, product2, 3);
        cartItem2.setId(2L);
        cartItem3 = new CartItem(cart, product3, 2);
        cartItem3.setId(3L);
        cart.setCartItems(List.of(cartItem1, cartItem2, cartItem3));
    }

    private void initOrders() {
        order1 = Order.builder()
                .id(1L)
                .orderNumber("Y923MB72")
                .buyer(buyer)
                .seller(seller1)
                .totalPrice(BigDecimal.valueOf(266.00))
                .deliveryType(DeliveryType.PAYMENT_ON_DELIVERY)
                .status(OrderStatus.PENDING)
                .shippingAddress("Belarus, Minsk")
                .creationDateTime(dateTime)
                .build();

        order2 = Order.builder()
                .id(2L)
                .orderNumber("B534GG3N")
                .buyer(buyer)
                .seller(seller2)
                .totalPrice(BigDecimal.valueOf(360.00))
                .deliveryType(DeliveryType.PAYMENT_ON_DELIVERY)
                .status(OrderStatus.PENDING)
                .shippingAddress("Belarus, Minsk")
                .creationDateTime(dateTime)
                .build();

        orderItem1 = OrderItem.builder()
                .id(1L)
                .order(order1)
                .product(product1)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(230.00))
                .subTotalPrice(BigDecimal.valueOf(230.00))
                .build();

        orderItem2 = OrderItem.builder()
                .id(2L)
                .order(order1)
                .product(product2)
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(12.00))
                .subTotalPrice(BigDecimal.valueOf(36.00))
                .build();

        orderItem3 = OrderItem.builder()
                .id(3L)
                .order(order2)
                .product(product3)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(180.00))
                .subTotalPrice(BigDecimal.valueOf(360.00))
                .build();
    }

    private void initDto() {
        orderRequestDto = new OrderRequestDto(
                DeliveryType.PAYMENT_ON_DELIVERY,
                "Belarus, Minsk"
        );

        orderItemDto1 = new OrderItemDto(
                1L,
                1L,
                "Skateboard",
                BigDecimal.valueOf(230.00),
                1,
                BigDecimal.valueOf(230.00)
        );

        orderItemDto2 = new OrderItemDto(
                2L,
                2L,
                "Tomato seeds",
                BigDecimal.valueOf(12.00),
                3,
                BigDecimal.valueOf(36.00)
        );

        orderItemDto3 = new OrderItemDto(
                3L,
                3L,
                "Painting",
                BigDecimal.valueOf(180.00),
                2,
                BigDecimal.valueOf(180.00)
        );

        orderDto1 = new OrderDto(
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

        orderDto2 = new OrderDto(
                2L,
                "B534GG3N",
                1L,
                "Pavel Bashmakov",
                3L,
                "Viktoria Berry",
                List.of(orderItemDto3),
                2,
                BigDecimal.valueOf(360.00),
                DeliveryType.PAYMENT_ON_DELIVERY,
                OrderStatus.PENDING,
                "Belarus, Minsk",
                formattedDateTime
        );
    }
}