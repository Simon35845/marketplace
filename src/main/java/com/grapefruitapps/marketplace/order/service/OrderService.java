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
        Order order = findOrderByIdWithAllDetails(orderId);
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
        cartService.checkCartIsEmpty(cart);
        List<Order> orders = new ArrayList<>();

        if (cart.getCartItems().size() == 1) {
            Long sellerId = cart.getCartItems().getFirst().getProduct().getSeller().getId();
            Order createdOrder = createOneOrder(cart, sellerId, orderRequestDto);
            orders.add(createdOrder);
        } else {
            Set<Long> sellerIds = new HashSet<>();
            for (CartItem cartItem : cart.getCartItems()) {
                sellerIds.add(cartItem.getProduct().getSeller().getId());
            }

            for (Long sellerId : sellerIds) {
                Order createdOrder = createOneOrder(cart, sellerId, orderRequestDto);
                orders.add(createdOrder);
            }
        }

        cartService.clearCart(buyerId);
        log.info("Orders was created, count of orders: {}", orders.size());
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    private OrderItem createOrderItem(CartItem cartItem, Order order) {
        Product product = cartItem.getProduct();
        productService.checkProductAvailability(product);
        BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(cartItem.getQuantity())
                .unitPrice(product.getPrice())
                .subTotalPrice(subTotal)
                .build();
    }

    private Order createOneOrder(Cart cart, Long sellerId, OrderRequestDto orderRequestDto) {
        log.info("Creating order: buyer_id={}, seller_id={}", cart.getBuyer().getId(), sellerId);
        Order orderToSave = Order.builder()
                .orderNumber(generator.generate())
                .buyer(cart.getBuyer())
                .seller(userService.findUserById(sellerId))
                .deliveryType(orderRequestDto.deliveryType())
                .status(OrderStatus.PENDING)
                .shippingAddress(orderRequestDto.shippingAddress())
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

    @Transactional
    public OrderDto changeItemQuantity(Long itemId, Integer quantity, Long buyerId) {
        log.info("Updating order item quantity: item_id={}, quantity={}, buyer_id={}", itemId, quantity, buyerId);
        OrderItem item = findOrderItemByIdWithDetails(itemId);
        checkOrderItemOwnerShip(item, buyerId);

        item.setQuantity(quantity);
        orderItemRepository.save(item);
        log.info("Order item quantity was updated");
        return getOrderById(item.getOrder().getId(), buyerId);
    }

    @Transactional
    public OrderDto deleteItem(Long itemId, Long buyerId) {
        log.info("Deleting order item: itemId={}, buyer_id={}", itemId, buyerId);
        OrderItem item = findOrderItemByIdWithDetails(itemId);
        checkOrderItemOwnerShip(item, buyerId);

        orderItemRepository.deleteById(itemId);
        log.info("Order item was deleted");
        return getOrderById(item.getOrder().getId(), buyerId);
    }

        @Transactional
    public OrderDto updateOrder(Long orderId, OrderRequestDto orderRequestDto, Long buyerId) {
        log.info("Updating order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        checkOrderAccess(order, buyerId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify order with status: " + order.getStatus());
        }

        order.setDeliveryType(orderRequestDto.deliveryType());
        order.setShippingAddress(orderRequestDto.shippingAddress());

        Order savedOrder = orderRepository.save(order);
        log.info("Order was updated: order_id={}, buyer_id={}", orderId, buyerId);
        return orderMapper.toOrderDto(savedOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long buyerId) {
        log.info("Deleting order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        checkOrderAccess(order, buyerId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot delete order with status: " + order.getStatus());
        }

        orderRepository.deleteById(orderId);
        log.info("Order was deleted: order_id={}, buyer_id={}", orderId, buyerId);
    }

    @Transactional
    public void placeOrder(Long orderId, Long buyerId) {
        log.info("Placing order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        checkOrderAccess(order, buyerId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot place order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);
        log.info("Order was placed: order_id={}, buyer_id={}", orderId, buyerId);
    }

    @Transactional
    public void completeOrder(Long orderId, Long buyerId) {
        log.info("Completing order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        checkOrderAccess(order, buyerId);

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        log.info("Order was completed: order_id={}, buyer_id={}", orderId, buyerId);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long buyerId) {
        log.info("Canceling order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        checkOrderAccess(order, buyerId);

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order was cancelled: order_id={}, buyer_id={}", orderId, buyerId);
    }

    private @NonNull Order findOrderByIdWithAllDetails(Long id) {
        log.debug("Finding order by id {}", id);
        return orderRepository.findByIdWithAllDetails(id).orElseThrow(() -> {
            log.warn("Order by id {} not found in database", id);
            return new EntityNotFoundException("Not found order by id: " + id);
        });
    }

    private @NonNull OrderItem findOrderItemByIdWithDetails(Long id) {
        log.debug("Finding order item with id={}", id);
        return orderItemRepository.findByIdWithOrderAndBuyer(id).orElseThrow(() -> {
            log.warn("Order item with id {} not found in database", id);
            return new EntityNotFoundException("Not found order item by id: " + id);
        });
    }

    private void checkOrderItemOwnerShip(OrderItem item, Long buyerId) {
        log.debug("Checking order item ownership, item_id={}, buyerId_id={}", item.getId(), buyerId);
        if (!item.getOrder().getBuyer().getId().equals(buyerId)) {
            log.warn("Buyer with id={} attempted to access order item with id={} owned by another buyer with id={}",
                    buyerId, item.getId(), item.getOrder().getBuyer().getId());
            throw new AccessDeniedException("This order item does not belong to the user");
        }
    }

    private void checkOrderAccess(Order order, Long userId) {
        log.debug("Checking order access: order_id={}, userId={}", order.getId(), userId);
        if (!order.getSeller().getId().equals(userId) && !order.getBuyer().getId().equals(userId)) {
            log.warn("User {} attempted to access order {} without permission", userId, order.getId());
            throw new AccessDeniedException("You don't have permission to access this order");
        }
    }
}
