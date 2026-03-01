package com.grapefruitapps.marketplace.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("""
            select a from Address a
            where a.country = :country
            and a.city = :city
            and a.street = :street
            and a.house = :house
            and a.apartment = :apartment
            """)
    Optional<Address> findExistingAddress(
            @Param("country") String country,
            @Param("city") String city,
            @Param("street") String street,
            @Param("house") String house,
            @Param("apartment") String apartment
    );
}

