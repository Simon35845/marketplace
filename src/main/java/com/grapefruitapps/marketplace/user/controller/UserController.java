package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import com.grapefruitapps.marketplace.user.dto.*;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        UserFilter userFilter = new UserFilter(
                name,
                phone,
                email,
                status,
                role,
                pageSize,
                pageNumber
        );

        log.info("Called getAllUsers");
        return ResponseEntity.ok(userService.searchAllByFilter(userFilter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id
    ) {
        log.info("Called getUserById: id={}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ) {
        log.info("Called createUser");
        UserResponseDto createdUser = userService.createUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserRequestDto userRequestDto
    ) {
        log.info("Called updateUser: id={}, user={}", id, userRequestDto);
        UserResponseDto updatedUser = userService.updateUser(id, userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        log.info("Called deleteUser: id={}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid PasswordDto passwordDto
    ) {
        log.info("Called changePassword");
        userService.changePassword(userDetails.getId(), passwordDto);
        return ResponseEntity.ok().build();
    }
}