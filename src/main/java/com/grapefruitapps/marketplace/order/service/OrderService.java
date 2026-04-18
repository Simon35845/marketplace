package com.grapefruitapps.marketplace.order.service;

import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.cart.service.CartService;
import com.grapefruitapps.marketplace.order.dto.OrderDto;
import com.grapefruitapps.marketplace.order.dto.OrderFilter;
import com.grapefruitapps.marketplace.order.dto.OrderItemDto;
import com.grapefruitapps.marketplace.order.dto.OrderRequestDto;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.order.entity.OrderItem;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import com.grapefruitapps.marketplace.order.repository.OrderItemRepository;
import com.grapefruitapps.marketplace.order.repository.OrderRepository;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.service.UserService;
import com.grapefruitapps.marketplace.utils.PaginationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        log.debug("Get order by order_id={}, user_id={}", orderId, userId);
        Order order = findOrderByIdWithAllDetails(orderId);
        checkOrderAccess(order, userId);
        return orderMapper.toOrderDto(order);
    }

    public OrderItemDto getOrderItemById(Long itemId, Long userId) {
        log.debug("Get order item by item_id={}, user_id={}", itemId, userId);
        OrderItem item = findOrderItemByIdWithAllDetails(itemId);
        checkOrderAccess(item.getOrder(), userId);
        return orderMapper.toOrderItemDto(item);
    }

    public List<OrderDto> getBuyerOrdersByFilter(OrderFilter filter, Long buyerId) {
        log.debug("Get buyer orders by filter, buyer_id={}", buyerId);
        Pageable pageable = PaginationUtil.getPageable(filter.pageSize(), filter.pageNumber());

        List<Order> orders = orderRepository.findOrdersByFilter(
                filter.orderNumber(),
                buyerId,
                null,
                filter.sellerId(),
                filter.sellerName(),
                filter.deliveryType(),
                filter.status(),
                filter.shippingAddress(),
                pageable
        );

        log.debug("Found {} orders", orders.size());
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    public List<OrderDto> getSellerOrdersByFilter(OrderFilter filter, Long sellerId) {
        log.debug("Get seller orders by filter, seller_id={}", sellerId);
        Pageable pageable = PaginationUtil.getPageable(filter.pageSize(), filter.pageNumber());

        List<Order> orders = orderRepository.findOrdersByFilter(
                filter.orderNumber(),
                filter.buyerId(),
                filter.buyerName(),
                sellerId,
                null,
                filter.deliveryType(),
                filter.status(),
                filter.shippingAddress(),
                pageable
        );

        log.debug("Found {} orders", orders.size());
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    @Transactional
    public List<OrderDto> createOrdersFromCart(OrderRequestDto orderRequestDto, Long buyerId) {
        log.info("Creating orders from cart: buyer_id={}", buyerId);
        Cart cart = cartService.findCartByBuyerIdWithAllDetails(buyerId);
        cartService.checkCartIsEmpty(cart);
        userService.checkUserActivity(cart.getBuyer());

        List<Order> ordersToSave = groupOrdersBySellers(orderRequestDto, cart);
        List<Order> savedOrders = orderRepository.saveAll(ordersToSave);
        cartService.clearCart(buyerId);
        log.info("Orders was saved, count of orders: {}", savedOrders.size());
        return savedOrders.stream().map(orderMapper::toOrderDto).toList();
    }

    public List<Order> groupOrdersBySellers(OrderRequestDto orderRequestDto, Cart cart) {
        log.debug("Grouping orders by sellers cart_id={}", cart.getId());
        return cart.getCartItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller()))
                .entrySet().stream()
                .map(entry -> {
                    User seller = entry.getKey();
                    List<CartItem> sellerItems = entry.getValue();
                    userService.checkUserActivity(seller);
                    return createOneOrder(cart.getBuyer(), seller, sellerItems, orderRequestDto);
                })
                .toList();
    }

    private Order createOneOrder(
            User buyer, User seller,
            List<CartItem> cartItems, OrderRequestDto orderRequestDto
    ) {
        log.debug("Creating one order: buyer_id={}, seller_id={}", buyer.getId(), seller.getId());
        Order order = Order.builder()
                .orderNumber(generator.generate())
                .buyer(buyer)
                .seller(seller)
                .orderItems(new ArrayList<>())
                .deliveryType(orderRequestDto.deliveryType())
                .status(OrderStatus.PENDING)
                .shippingAddress(orderRequestDto.shippingAddress())
                .creationDateTime(LocalDateTime.now())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = createOrderItem(order, cartItem);
            order.getOrderItems().add(orderItem);
            totalPrice = totalPrice.add(orderItem.getSubTotalPrice());
        }

        order.setTotalPrice(totalPrice);
        return order;
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        log.debug("Creating order item:  order_id={}, cartItem_id={}", order.getId(), cartItem.getId());
        Product product = cartItem.getProduct();
        productService.checkProductAvailability(product);
        BigDecimal subTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(cartItem.getQuantity())
                .unitPrice(product.getPrice())
                .subTotalPrice(subTotalPrice)
                .build();
    }

    @Transactional
    public OrderItemDto changeItemQuantity(Long itemId, Integer quantity, Long buyerId) {
        log.info("Updating order item quantity: item_id={}, quantity={}, buyer_id={}", itemId, quantity, buyerId);
        OrderItem item = findOrderItemByIdWithAllDetails(itemId);
        Order order = item.getOrder();
        userService.checkUserActivity(item.getOrder().getBuyer());
        checkOrderItemOwnerShip(item, buyerId);
        checkOrderIsPending(order);

        recalculateTotalPrice(order, item, quantity);
        orderItemRepository.save(item);
        orderRepository.save(order);
        log.info("Order item quantity was updated");
        return orderMapper.toOrderItemDto(item);
    }

    @Transactional
    public void deleteItem(Long itemId, Long buyerId) {
        log.info("Deleting order item: itemId={}, buyer_id={}", itemId, buyerId);
        OrderItem item = findOrderItemByIdWithAllDetails(itemId);
        Order order = item.getOrder();
        checkOrderItemOwnerShip(item, buyerId);
        checkOrderIsPending(order);

        recalculateTotalPrice(order, item, 0);
        orderRepository.save(order);
        orderItemRepository.deleteById(itemId);
        log.info("Order item was deleted");
    }

    public void recalculateTotalPrice(Order order, OrderItem item, int newQuantity) {
        log.debug("Recalculating total price");
        if (newQuantity == 0) {
            BigDecimal newTotalPrice = order.getTotalPrice().subtract(item.getSubTotalPrice());
            order.setTotalPrice(newTotalPrice);
        } else {
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal oldSubTotalPrice = item.getSubTotalPrice();
            BigDecimal newSubTotalPrice = unitPrice.multiply(BigDecimal.valueOf(newQuantity));
            BigDecimal newTotalPrice = order.getTotalPrice().add(newSubTotalPrice).subtract(oldSubTotalPrice);
            item.setQuantity(newQuantity);
            item.setSubTotalPrice(newSubTotalPrice);
            order.setTotalPrice(newTotalPrice);
        }
    }

    @Transactional
    public OrderDto updateOrder(Long orderId, OrderRequestDto orderRequestDto, Long buyerId) {
        log.info("Updating order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        userService.checkUserActivity(order.getBuyer());
        checkOrderAccess(order, buyerId);
        checkOrderIsPending(order);

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
        checkOrderIsPending(order);

        orderRepository.deleteById(orderId);
        log.info("Order was deleted: order_id={}, buyer_id={}", orderId, buyerId);
    }

    @Transactional
    public void placeOrder(Long orderId, Long buyerId) {
        log.info("Placing order: order_id={}, buyer_id={}", orderId, buyerId);
        Order order = findOrderByIdWithAllDetails(orderId);
        userService.checkUserActivity(order.getBuyer());
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

    private @NonNull OrderItem findOrderItemByIdWithAllDetails(Long id) {
        log.debug("Finding order item with id={}", id);
        return orderItemRepository.findByIdWithAllDetails(id).orElseThrow(() -> {
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

    private void checkOrderIsPending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify or delete order with status: " + order.getStatus());
        }
    }
}
