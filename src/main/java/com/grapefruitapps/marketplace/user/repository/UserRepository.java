package com.grapefruitapps.marketplace.user.repository;

import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
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
            SELECT DISTINCT u FROM User u
            LEFT JOIN u.roles r
            WHERE (:roleName IS NULL OR r.name = :roleName)
            AND (:name IS NULL OR u.name = :name)
            AND (:phone IS NULL OR u.phone = :phone)
            AND (:email IS NULL OR u.email = :email)
            AND (:status IS NULL OR u.status = :status)
            ORDER BY u.id
            """)
    List<User> findUsersByFilter(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("status") UserStatus status,
            @Param("roleName") String roleName,
            Pageable pageable
    );
}