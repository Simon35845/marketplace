package com.grapefruitapps.marketplace.security;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AccountDetails accountDetails,
            @RequestBody @Valid PasswordDto passwordDto
    ){
        log.info("Called changePassword");
        accountService.changePassword(accountDetails.getId(), passwordDto);
        return ResponseEntity.ok().build();
    }
}

