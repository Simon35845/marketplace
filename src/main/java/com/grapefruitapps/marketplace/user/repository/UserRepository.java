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
            select distinct u from User u
            left join u.roles r
                        with (:roleName is null or r.name = :roleName)
            where (:name is null or u.name = :name)
            and (:phone is null or u.phone = :phone)
            and (:email is null or u.email = :email)
            and (:status is null or u.status = :status)
            order by u.id
            """)
    List<User> searchAllByFilter(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("status") UserStatus status,
            @Param("roleName") String roleName,
            Pageable pageable
    );
}