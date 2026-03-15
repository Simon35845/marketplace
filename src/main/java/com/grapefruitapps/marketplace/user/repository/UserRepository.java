package com.grapefruitapps.marketplace.user.repository;

import com.grapefruitapps.marketplace.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("""
            select u from User u
            where (:name is null or u.name = :name)
            and (:phone is null or u.phone = :phone)
            and (:email is null or u.email = :email)
            order by u.id
            """)
    List<User> searchAllByFilter(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            Pageable pageable
    );
}
