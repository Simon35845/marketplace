package com.grapefruitapps.marketplace.user.controller;

import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.dto.UserFilter;
import com.grapefruitapps.marketplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class PublicUserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        UserFilter userFilter = new UserFilter(
                name,
                phone,
                email,
                pageSize,
                pageNumber
        );
        log.info("Called getAllUsers");
        return ResponseEntity.ok(userService.getUsersByFilter(userFilter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Called getUserById: id={}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
