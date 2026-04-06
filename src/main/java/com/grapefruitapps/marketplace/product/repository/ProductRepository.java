package com.grapefruitapps.marketplace.product.repository;

import com.grapefruitapps.marketplace.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.seller
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithSeller(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.seller s
            WHERE (:name IS NULL OR p.name = :name)
            AND (:category IS NULL OR p.category = :category)
            AND (:sellerId IS NULL OR s.id = :sellerId)
            AND (:sellerName IS NULL OR s.name = :sellerName)
            AND (:isVisible IS NULL OR p.isVisible = :isVisible)
            AND (:isPublished IS NULL OR p.isPublished = :isPublished)
            ORDER BY p.id
            """)
    List<Product> findProductsByFilter(
            @Param("name") String name,
            @Param("category") String category,
            @Param("sellerId") Long sellerId,
            @Param("sellerName") String sellerName,
            @Param("isVisible") Boolean isVisible,
            @Param("isPublished") Boolean isPublished,
            Pageable pageable
    );
}
