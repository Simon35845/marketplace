package com.grapefruitapps.marketplace.order.repository;

import com.grapefruitapps.marketplace.order.entity.DeliveryType;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.order.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.buyer
            LEFT JOIN FETCH o.seller
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithAllDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.buyer b
            LEFT JOIN FETCH o.seller s
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            WHERE (:orderNumber IS NULL OR o.orderNumber = :orderNumber)
            AND (:buyerId IS NULL OR b.id = :buyerId)
            AND (:buyerName IS NULL OR b.name = :buyerName)
            AND (:sellerId IS NULL OR s.id = :sellerId)
            AND (:sellerName IS NULL OR s.name = :sellerName)
            AND (:deliveryType IS NULL OR o.deliveryType = :deliveryType)
            AND (:status IS NULL OR o.status= :status)
            AND (:shippingAddress IS NULL OR o.shippingAddress = :shippingAddress)
            ORDER BY o.id
            """)
    List<Order> findOrdersByFilter(
            @Param("orderNumber") String orderNumber,
            @Param("buyerId") Long buyerId,
            @Param("buyerName") String buyerName,
            @Param("sellerId") Long sellerId,
            @Param("sellerName") String sellerName,
            @Param("deliveryType") DeliveryType deliveryType,
            @Param("status") OrderStatus status,
            @Param("shippingAddress") String shippingAddress,
            Pageable pageable
    );
}
