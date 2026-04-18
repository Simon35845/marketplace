package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserDataDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    private final UserMapper userMapper = new UserMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final String formattedDateTime = currentDateTime.format(FORMATTER);

    @Test
    void toDtoTest() {
        User user = User.builder()
                .id(1L)
                .username("Anna_3293")
                .name("Anna Rich")
                .email("annarich@gmail.com")
                .phone("+721021901342")
                .creationDateTime(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        UserDto expectedDto = new UserDto(
                1L,
                "Anna Rich",
                "annarich@gmail.com",
                "+721021901342",
                formattedDateTime
        );

        UserDto result = userMapper.toDto(user);
        assertEquals(expectedDto, result);
    }

    @Test
    void toDataDtoTest() {
        User user = User.builder()
                .id(1L)
                .username("Anna_3293")
                .name("Anna Rich")
                .email("annarich@gmail.com")
                .phone("+721021901342")
                .creationDateTime(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();

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

        UserDataDto result = userMapper.toDataDto(user);
        assertEquals(expectedDto, result);
    }
}