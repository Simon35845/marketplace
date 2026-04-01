package com.grapefruitapps.marketplace.cart.repository;

import com.grapefruitapps.marketplace.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByBuyerId(Long buyerId);

    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.buyer
            LEFT JOIN FETCH c.cartItems ci
            LEFT JOIN FETCH ci.product p
            LEFT JOIN FETCH p.seller
            WHERE c.buyer.id = :buyerId
            """)
    Optional<Cart> findByBuyerIdWithAllDetails(@Param("buyerId") Long buyerId);
}
