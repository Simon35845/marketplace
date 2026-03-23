package com.grapefruitapps.marketplace.product.repository;

import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.entity.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        select p from Product p
                where (:sellerId is null or p.seller.id = :sellerId)
        and (:name is null or p.name = :name)
        and (:category is null or p.category = :category)
        and (:status is null or p.status = :status)
        order by p.id
        """)
    List<Product> findProductsByFilter(
            @Param("sellerId") Long sellerId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("status") ProductStatus status,
            Pageable pageable
    );
}
