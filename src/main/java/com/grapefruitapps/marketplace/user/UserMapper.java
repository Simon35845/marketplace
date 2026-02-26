package com.grapefruitapps.marketplace.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail()
        );
    }

    public User toEntity(UserDto userDto) {
        return new User(
                userDto.name(),
                userDto.phone(),
                userDto.email()
        );
    }
}
