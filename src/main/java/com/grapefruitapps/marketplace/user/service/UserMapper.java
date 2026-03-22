package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserDataDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt()
        );
    }

    public UserDataDto toDataDto(User user) {
        return new UserDataDto(
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
