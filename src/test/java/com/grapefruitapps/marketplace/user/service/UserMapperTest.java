package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserDataDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final UserMapper userMapper = new UserMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final String formattedDateTime = currentDateTime.format(FORMATTER);
    private User user;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(1L)
                .username("Anna_3293")
                .name("Anna Rich")
                .email("annarich@gmail.com")
                .phone("+721021901342")
                .creationDateTime(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();
    }

    @Test
    void toDtoTest() {
        UserDto expectedDto = new UserDto(
                1L,
                "Anna Rich",
                "annarich@gmail.com",
                "+721021901342",
                formattedDateTime
        );

        UserDto actualDto = userMapper.toDto(user);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void toDataDtoTest() {
        UserDataDto expectedDto = new UserDataDto(
                1L,
                "Anna_3293",
                "Anna Rich",
                "annarich@gmail.com",
                "+721021901342",
                formattedDateTime,
                UserStatus.ACTIVE,
                List.of("ROLE_USER")
        );

        UserDataDto actualDto = userMapper.toDataDto(user);
        assertEquals(expectedDto, actualDto);
    }
}