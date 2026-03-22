package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserResponseDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getStatus(),
                user.getRoles().stream().map(Role::getName).toList()
        );
    }
}
