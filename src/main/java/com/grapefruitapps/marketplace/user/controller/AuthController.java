package com.grapefruitapps.marketplace.user.controller;

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

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ) {
        log.info("Called createUser");
        UserDto createdUser = userService.createUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
