package com.grapefruitapps.marketplace.profile;

import com.grapefruitapps.marketplace.security.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@NoArgsConstructor
@Getter
@Setter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public Profile(String name, String email, String phone, Account account) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.account = account;
    }
}