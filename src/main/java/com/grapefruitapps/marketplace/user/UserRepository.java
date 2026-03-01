package com.grapefruitapps.marketplace.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
            select u from User u
            where (:name is null or u.name = :name)
            and (:phone is null or u.phone = :phone)
            and (:email is null or u.email = :email)
            and (:country is null or u.address.country = :country)
            and (:city is null or u.address.city = :city)
            and (:street is null or u.address.street = :street)
            and (:house is null or u.address.house = :house)
            order by u.id
            """)
    List<User> searchAllByFilter(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("country") String country,
            @Param("city") String city,
            @Param("street") String street,
            @Param("house") String house,
            Pageable pageable
    );
}
