package com.grapefruitapps.marketplace.order.repository;

import com.grapefruitapps.marketplace.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
            SELECT oi FROM OrderItem oi
            LEFT JOIN FETCH oi.order o
            LEFT JOIN FETCH o.buyer b
            WHERE oi.id=:id
            """)
    Optional<OrderItem> findByIdWithOrderAndBuyer(@Param("id") Long id);
}
