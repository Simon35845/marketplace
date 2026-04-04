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
            SELECT p FROM Product p
                    WHERE (:sellerId IS NULL OR p.seller.id = :sellerId)
            AND (:name IS NULL OR p.name = :name)
            AND (:category IS NULL OR p.category = :category)
            AND (:isVisible IS NULL OR p.isVisible = :isVisible)
            AND (:isPublished IS NULL OR p.isPublished = :isPublished)
            ORDER BY p.id
            """)
    List<Product> findProductsByFilter(
            @Param("sellerId") Long sellerId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("isVisible") Boolean isVisible,
            @Param("isPublished") Boolean isPublished,
            Pageable pageable
    );
}
