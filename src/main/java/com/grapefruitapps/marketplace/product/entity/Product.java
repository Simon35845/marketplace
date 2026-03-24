package com.grapefruitapps.marketplace.product.entity;

import com.grapefruitapps.marketplace.order.entity.OrderItem;
import com.grapefruitapps.marketplace.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@NoArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "creation_date_time", nullable = false, updatable = false)
    private LocalDateTime creationDateTime;

    @Column(name = "sale_date_time")
    private LocalDateTime saleDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    public Product(String name, BigDecimal price, String category, String description) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
    }
}
