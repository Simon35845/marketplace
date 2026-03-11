package com.grapefruitapps.marketplace.security;

import com.grapefruitapps.marketplace.profile.Profile;
import com.grapefruitapps.marketplace.profile.ProfileDto;
import com.grapefruitapps.marketplace.profile.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository,
                          ProfileRepository profileRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountDto createAccount(SignUpRequest signUpRequest) {
        log.info("Creating new account");
        Role roleUser = roleRepository.findByName("ROLE_USER");

        Account accountToSave = new Account(
                signUpRequest.username(),
                passwordEncoder.encode(signUpRequest.password()),
                LocalDateTime.now(),
                AccountStatus.ACTIVE,
                List.of(roleUser)
        );
        Account savedAccount = accountRepository.save(accountToSave);
        Profile profile = new Profile(
                signUpRequest.name(),
                signUpRequest.email(),
                signUpRequest.phone(),
                savedAccount
        );
        Profile savedProfile = profileRepository.save(profile);
        log.info("Account was created, id: {}", savedAccount.getId());
        return new AccountDto(
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getCreatedAt(),
                savedAccount.getStatus(),
                savedAccount.getRoles().stream().map(Role::getName).toList(),
                new ProfileDto(
                        savedProfile.getId(),
                        savedProfile.getName(),
                        savedProfile.getEmail(),
                        savedProfile.getPhone()
                )
        );
    }
}
