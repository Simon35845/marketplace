package com.grapefruitapps.marketplace.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public AccountDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Get account by username: {}", username);
        Account account = accountRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Not found account by username: " + username)
        );
        log.info("Found account with username: {}", username);
        return new AccountDetails(account);
    }
}
