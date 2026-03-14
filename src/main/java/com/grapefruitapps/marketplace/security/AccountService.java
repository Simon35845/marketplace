package com.grapefruitapps.marketplace.security;

import com.grapefruitapps.marketplace.profile.Profile;
import com.grapefruitapps.marketplace.profile.ProfileDto;
import com.grapefruitapps.marketplace.profile.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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

    @Transactional
    public void changePassword(Long id, PasswordDto passwordDto) {
        log.info("Changing password in account, id: {}", id);
        Account account = accountRepository.findById(id).orElseThrow(() -> {
            log.warn("Account with id {} not found in database", id);
            return new EntityNotFoundException("Not found account by id: " + id);
        });

        if(!passwordEncoder.matches(passwordDto.currentPassword(), account.getPassword())){
            log.warn("Current password is incorrect for account id: {}", id);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if(passwordEncoder.matches(passwordDto.newPassword(), account.getPassword())){
            log.warn("New password matches current password for account id: {}", id);
            throw new IllegalArgumentException("New password must be different from current password");
        }

        account.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
        accountRepository.save(account);
        log.info("Password was changed, account id: {}", id);
    }
}
