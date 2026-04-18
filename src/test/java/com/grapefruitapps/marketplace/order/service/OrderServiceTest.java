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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final String formattedDateTime = currentDateTime.format(FORMATTER);

    private User buyer;
    private User seller1;
    private User seller2;
    private Product product1;
    private Product product2;
    private Product product3;
    private Cart cart;
    private Order order1;
    private Order order2;
    private OrderDto orderDto1;
    private OrderDto orderDto2;

    @BeforeEach
    void init() {
        initUsers();
        initProducts();
        initCart();
        initOrders();
        initOrderDtos();
    }

    @Test
    void groupOrdersBySellersTest() {
        OrderRequestDto orderRequestDto = new OrderRequestDto(
                DeliveryType.PAYMENT_ON_DELIVERY, "Belarus, Minsk");

        when(generator.generate()).thenReturn("Y923MB72", "B534GG3N");
        doNothing().when(productService).checkProductAvailability(any(Product.class));

        List<Order> actualOrders = orderService.groupOrdersBySellers(orderRequestDto, cart);
        Order actualOrder1 = actualOrders.get(0);
        Order actualOrder2 = actualOrders.get(1);

        assertEquals(2, actualOrders.size());
        assertEquals(2,actualOrder1.getOrderItems().size());
        assertEquals(1,actualOrder2.getOrderItems().size());
        assertEquals("Yan Lanbitz", actualOrder1.getSeller().getName());
        assertEquals("Viktoria Berry", actualOrder2.getSeller().getName());
        assertEquals(BigDecimal.valueOf(266.00), actualOrder1.getTotalPrice());
        assertEquals(BigDecimal.valueOf(360.00), actualOrder2.getTotalPrice());
        verify(productService, times(3)).checkProductAvailability(any(Product.class));
    }

    @Test
    void createOrdersFromCartTest() {
        OrderRequestDto orderRequestDto = new OrderRequestDto(
                DeliveryType.PAYMENT_ON_DELIVERY, "Belarus, Minsk");
        Long buyerId = 1L;

        when(cartService.findCartByBuyerIdWithAllDetails(buyer.getId())).thenReturn(cart);
        doNothing().when(cartService).checkCartIsEmpty(cart);
        doNothing().when(userService).checkUserActivity(any(User.class));
        when(generator.generate()).thenReturn("Y923MB72", "B534GG3N");
        doNothing().when(productService).checkProductAvailability(any(Product.class));
        when(orderRepository.saveAll(anyList())).thenReturn(List.of(order1, order2));
        doNothing().when(cartService).clearCart(buyer.getId());
        when(orderMapper.toOrderDto(any(Order.class))).thenReturn(orderDto1, orderDto2);

        List<OrderDto> resultOrderDtos = orderService.createOrdersFromCart(orderRequestDto, buyerId);

        assertEquals(2, resultOrderDtos.size());
        verify(productService, times(3)).checkProductAvailability(any(Product.class));
        verify(orderRepository).saveAll(anyList());
        verify(cartService).clearCart(buyer.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 120.00",
            "2, 220.00",
            "5, 370.00"
    })
    void recalculateTotalPriceTest(int newQuantity, BigDecimal expectedTotalPrice){
        Order order = Order.builder()
                .totalPrice(BigDecimal.valueOf(270.00))
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(50.00))
                .subTotalPrice(BigDecimal.valueOf(150.00))
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .quantity(4)
                .unitPrice(BigDecimal.valueOf(30.00))
                .subTotalPrice(BigDecimal.valueOf(120.00))
                .build();
        order.setOrderItems(List.of(orderItem1, orderItem2));

        orderService.recalculateTotalPrice(order, orderItem1, newQuantity);
        assertEquals(0,expectedTotalPrice.compareTo(order.getTotalPrice()));
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
                .username("yanlanbitz443")
                .name("Yan Lanbitz")
                .email("yanlanbitz@gmail.com")
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

        CartItem cartItem1 = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product1)
                .quantity(1)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .id(2L)
                .cart(cart)
                .product(product2)
                .quantity(3)
                .build();

        CartItem cartItem3 = CartItem.builder()
                .id(3L)
                .cart(cart)
                .product(product3)
                .quantity(2)
                .build();

        cart.setCartItems(List.of(cartItem1, cartItem2, cartItem3));
    }

    private void initOrders() {
        order1 = Order.builder()
                .orderNumber("Y923MB72")
                .buyer(buyer)
                .seller(seller1)
                .totalPrice(BigDecimal.valueOf(266.00))
                .deliveryType(DeliveryType.PAYMENT_ON_DELIVERY)
                .status(OrderStatus.PENDING)
                .shippingAddress("Belarus, Minsk")
                .creationDateTime(currentDateTime)
                .build();

        order2 = Order.builder()
                .orderNumber("B534GG3N")
                .buyer(buyer)
                .seller(seller2)
                .totalPrice(BigDecimal.valueOf(360.00))
                .deliveryType(DeliveryType.PAYMENT_ON_DELIVERY)
                .status(OrderStatus.PENDING)
                .shippingAddress("Belarus, Minsk")
                .creationDateTime(currentDateTime)
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .order(order1)
                .product(product1)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(230.00))
                .subTotalPrice(BigDecimal.valueOf(230.00))
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .order(order1)
                .product(product2)
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(12.00))
                .subTotalPrice(BigDecimal.valueOf(36.00))
                .build();

        OrderItem orderItem3 = OrderItem.builder()
                .order(order2)
                .product(product3)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(180.00))
                .subTotalPrice(BigDecimal.valueOf(360.00))
                .build();

        order1.setOrderItems(List.of(orderItem1, orderItem2));
        order2.setOrderItems(List.of(orderItem3));
    }

    private void initOrderDtos() {
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

        OrderItemDto orderItemDto3 = new OrderItemDto(
                3L,
                3L,
                "Painting",
                BigDecimal.valueOf(180.00),
                2,
                BigDecimal.valueOf(360.00)
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