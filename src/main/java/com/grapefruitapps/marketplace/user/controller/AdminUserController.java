package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import com.grapefruitapps.marketplace.user.dto.*;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDataDto> getUserById(@PathVariable Long id) {
        log.info("Called getUserDataById: id={}", id);
        UserDataDto userDataDto = userService.getUserDataById(id);
        return ResponseEntity.ok(userDataDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDataDto>> getUsersByFilter(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        UserDataFilter userDataFilter = new UserDataFilter(
                name,
                phone,
                email,
                status,
                role,
                pageSize,
                pageNumber
        );

        log.info("Called getUsersByFilter");
        List<UserDataDto> userDataDtoList = userService.getUsersDataByFilter(userDataFilter);
        return ResponseEntity.ok(userDataDtoList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteUser: id={}", id);
        userService.deleteUser(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/grant-admin")
    public ResponseEntity<Void> grantAdminRole(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called grantAdminRole: id={}", id);
        userService.grantAdminRole(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/revoke-admin")
    public ResponseEntity<Void> revokeAdminRole(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called revokeAdminRole: id={}", id);
        userService.revokeAdminRole(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}