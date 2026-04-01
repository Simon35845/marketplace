package com.grapefruitapps.marketplace.order.repository;

import com.grapefruitapps.marketplace.order.entity.Order;
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
            SELECT o FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            WHERE o.buyer.id = :buyerId
            """)
    List<Order> findOrdersByBuyerId(@Param("buyerId") Long buyerId);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            WHERE o.seller.id = :sellerId
            """)
    List<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId);
}
