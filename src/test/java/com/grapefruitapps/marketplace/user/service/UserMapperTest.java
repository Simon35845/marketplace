package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserResponseDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toDto() {
        Long id = 1L;
        LocalDateTime currentDateTime = LocalDateTime.now();

        User user = User.builder()
                .id(id)
                .username("Anna_3293")
                .name("Anna")
                .email("anna@mail.com")
                .createdAt(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        UserResponseDto expectedDto = new UserResponseDto(
                id,
                "Anna_3293",
                "Anna",
                "anna@mail.com",
                null,
                currentDateTime,
                UserStatus.ACTIVE,
                List.of("ROLE_USER")
        );

        UserResponseDto result = userMapper.toDto(user);
        assertEquals(expectedDto, result);
    }
}