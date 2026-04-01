package com.grapefruitapps.marketplace.order.service;

import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.cart.service.CartService;
import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderRequestDto;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.order.entity.OrderItem;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import com.grapefruitapps.marketplace.order.repository.OrderItemRepository;
import com.grapefruitapps.marketplace.order.repository.OrderRepository;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderNumberGenerator generator;

    public OrderDto getOrderById(Long orderId, Long userId) {
        log.debug("Get order by id={}", orderId);
        Order order = findByIdWithAllDetails(orderId);
        checkOrderAccess(order, userId);
        return orderMapper.toOrderDto(order);
    }

    public List<OrderDto> getOrdersBySellerId(Long sellerId) {
        log.debug("Get orders by seller_id={}", sellerId);
        List<Order> orders = orderRepository.findOrdersBySellerId(sellerId);
        log.debug("Found {} orders", orders.size());
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    public List<OrderDto> getOrdersByBuyerId(Long buyerId) {
        log.debug("Get orders by buyer_id={}", buyerId);
        List<Order> orders = orderRepository.findOrdersByBuyerId(buyerId);
        log.debug("Found {} orders", orders.size());
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    @Transactional
    public List<OrderDto> createOrdersFromCart(OrderRequestDto orderRequestDto, Long buyerId) {
        log.info("Creating order from cart: buyer_id={}", buyerId);
        Cart cart = cartService.findByBuyerIdWithAllDetails(buyerId);
        cartService.checkCartIsEmpty(buyerId, cart);
        String shippingAddress = orderRequestDto.shippingAddress();

        List<Order> orders = new ArrayList<>();

        if (cart.getCartItems().size() == 1) {
            Long sellerId = cart.getCartItems().getFirst().getProduct().getSeller().getId();
            Order createdOrder = createOneOrder(cart, sellerId, shippingAddress);
            orders.add(createdOrder);
        } else {
            Set<Long> sellerIds = new HashSet<>();
            for (CartItem cartItem : cart.getCartItems()) {
                sellerIds.add(cartItem.getProduct().getSeller().getId());
            }

            for (Long sellerId : sellerIds) {
                Order createdOrder = createOneOrder(cart, sellerId, shippingAddress);
                orders.add(createdOrder);
            }
        }

        cartService.clearCart(buyerId);
        log.info("Orders was created, count of orders: {}", orders.size());
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    private Order createOneOrder(Cart cart, Long sellerId, String shippingAddress) {
        log.info("Creating order: buyer_id={}, seller_id={}", cart.getBuyer().getId(), sellerId);
        Order orderToSave = Order.builder()
                .orderNumber(generator.generate())
                .buyer(cart.getBuyer())
                .seller(userService.findUserById(sellerId))
                .status(OrderStatus.PENDING)
                .shippingAddress(shippingAddress)
                .creationDateTime(LocalDateTime.now())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            if (productService.isProductOwner(cartItem.getProduct(), sellerId)) {
                OrderItem orderItem = createOrderItem(cartItem, orderToSave);
                orderToSave.getOrderItems().add(orderItem);
                totalPrice = totalPrice.add(orderItem.getSubTotalPrice());
            }
        }
        orderToSave.setTotalPrice(totalPrice);
        log.info("Order was created: buyer_id={}, seller_id={}", cart.getBuyer().getId(), sellerId);
        return orderRepository.save(orderToSave);
    }

    private OrderItem createOrderItem(CartItem cartItem, Order order) {
        Product product = cartItem.getProduct();
        productService.markProductAsSold(product, order.getSeller().getId());
        BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(cartItem.getQuantity())
                .unitPrice(product.getPrice())
                .subTotalPrice(subTotal)
                .build();
    }

    private @NonNull Order findByIdWithAllDetails(Long id) {
        log.debug("Finding order by id {}", id);
        return orderRepository.findByIdWithAllDetails(id).orElseThrow(() -> {
            log.warn("Order by id {} not found in database", id);
            return new EntityNotFoundException("Not found order by id: " + id);
        });
    }

    private void checkOrderAccess(Order order, Long userId) {
        log.debug("Checking order access: order_id={}, userId={}", order.getId(), userId);
        if (!order.getSeller().getId().equals(userId) && !order.getBuyer().getId().equals(userId)) {
            log.warn("User {} attempted to access order {} without permission", userId, order.getId());
            throw new AccessDeniedException("You don't have permission to access this order");
        }
    }
}
