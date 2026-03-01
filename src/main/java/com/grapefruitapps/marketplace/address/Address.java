package com.grapefruitapps.marketplace.address;

import com.grapefruitapps.marketplace.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "country", "city", "street", "house", "apartment"
        }))
@Getter
@Setter
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String street;

    @Column(nullable = false, length = 20)
    private String house;

    @Column(nullable = false, length = 20)
    private String apartment;

    @OneToOne(mappedBy = "address")
    private User user;

    public Address(String country, String city, String street, String house, String apartment) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.house = house;
        this.apartment = apartment;
    }
}
