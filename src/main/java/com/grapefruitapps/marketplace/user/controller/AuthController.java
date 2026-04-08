package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.user.dto.AuthDto;
import com.grapefruitapps.marketplace.user.dto.UserCredentialsDto;
import com.grapefruitapps.marketplace.user.dto.UserRegisterDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ) {
        log.info("Called createUser");
        UserDto userDto = userService.createUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/log-in")
    public ResponseEntity<AuthDto> logIn(
            @RequestBody @Valid UserCredentialsDto userCredentialsDto
    ) {
        log.info("Called logIn");
        AuthDto authDto = userService.logIn(userCredentialsDto);
        return ResponseEntity.ok(authDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDto> refresh(
            @RequestBody AuthDto authDto
    ) {
        log.info("Called refresh");
        return ResponseEntity.ok(userService.refreshToken(authDto));
    }
}
