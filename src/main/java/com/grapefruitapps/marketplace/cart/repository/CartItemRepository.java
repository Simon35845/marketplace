package com.grapefruitapps.marketplace.cart.repository;

import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("""
            SELECT ci FROM CartItem ci
            LEFT JOIN FETCH ci.cart c
            LEFT JOIN FETCH c.buyer
            LEFT JOIN FETCH ci.product p
            LEFT JOIN FETCH p.seller
            WHERE ci.id = :id
            """)
    Optional<CartItem> findByCartItemIdWithAllDetails(@Param("id") Long id);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);
}
