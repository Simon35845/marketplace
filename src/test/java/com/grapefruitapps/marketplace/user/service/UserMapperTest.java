package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserDataDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    private UserMapper userMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toDto() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .username("Anna_3293")
                .name("Anna")
                .email("anna@mail.com")
                .phone("+721021901342")
                .creationDateTime(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        UserDto expectedDto = new UserDto(
                1L,
                "Anna",
                "anna@mail.com",
                "+721021901342",
                currentDateTime.format(formatter)
        );

        UserDto result = userMapper.toDto(user);
        assertEquals(expectedDto, result);
    }

    @Test
    void toDataDto() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .username("Anna_3293")
                .name("Anna")
                .email("anna@mail.com")
                .phone("+721021901342")
                .creationDateTime(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        UserDataDto expectedDto = new UserDataDto(
                1L,
                "Anna_3293",
                "Anna",
                "anna@mail.com",
                "+721021901342",
                currentDateTime.format(formatter),
                UserStatus.ACTIVE,
                List.of("ROLE_USER")
        );

        UserDataDto result = userMapper.toDataDto(user);
        assertEquals(expectedDto, result);
    }
}