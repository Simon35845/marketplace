package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import com.grapefruitapps.marketplace.user.dto.PasswordDto;
import com.grapefruitapps.marketplace.user.dto.UserUpdateDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getCurrentUser: id={}", userDetails.getId());
        UserDto userDto = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDto> updateUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UserUpdateDto userUpdateDto
    ) {
        log.info("Called updateUser: id={}", userDetails.getId());
        UserDto userDto = userService.updateUser(userDetails.getId(), userUpdateDto);
        return ResponseEntity.ok(userDto);
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
    public ResponseEntity<Void> deletionRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deletionRequest: id={}", userDetails.getId());
        userService.deletionRequest(userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/deletion/cancel")
    public ResponseEntity<Void> cancelDeletionRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called cancelDeletionRequest: id={}", userDetails.getId());
        userService.cancelDeletionRequest(userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
