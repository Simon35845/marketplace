package com.grapefruitapps.marketplace.security;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AccountDto> createAccount(
            @RequestBody @Valid SignUpRequest signUpRequest
    ) {
        log.info("Called createAccount");
        AccountDto createdAccount = accountService.createAccount(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }
}

