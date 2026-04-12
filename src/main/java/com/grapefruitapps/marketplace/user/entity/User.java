package com.grapefruitapps.marketplace.user.entity;

import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.order.entity.Order;
import com.grapefruitapps.marketplace.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, updatable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "creation_date_time", nullable = false, updatable = false)
    private LocalDateTime creationDateTime;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private List<Role> roles;

    @OneToMany(mappedBy = "seller")
    private List<Product> products;

    @OneToMany(mappedBy = "buyer")
    private List<Order> purchases;

    @OneToMany(mappedBy = "seller")
    private List<Order> sales;

    @OneToOne(mappedBy = "buyer")
    private Cart cart;
}
