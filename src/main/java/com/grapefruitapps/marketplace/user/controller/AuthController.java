package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.user.dto.UserRegisterDto;
import com.grapefruitapps.marketplace.user.dto.UserResponseDto;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ) {
        log.info("Called createUser");
        UserResponseDto createdUser = userService.createUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
