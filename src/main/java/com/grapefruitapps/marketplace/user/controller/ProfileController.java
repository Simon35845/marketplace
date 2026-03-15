package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import com.grapefruitapps.marketplace.user.dto.PasswordDto;
import com.grapefruitapps.marketplace.user.dto.UserRequestDto;
import com.grapefruitapps.marketplace.user.dto.UserResponseDto;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getCurrentUser: id={}", userDetails.getId());
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponseDto> updateUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UserRequestDto userRequestDto
    ) {
        log.info("Called updateUser: id={}", userDetails.getId());
        UserResponseDto updatedUser = userService.updateUser(userDetails.getId(), userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid PasswordDto passwordDto
    ) {
        log.info("Called changePassword: id={}", userDetails.getId());
        userService.changePassword(userDetails.getId(), passwordDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/deletion")
    public ResponseEntity<Void> requestDeletion(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called requestDeletion: id={}", userDetails.getId());
        userService.markUserForDeletion(userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
