package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.user.dto.UserResponseDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    public UserService userService;

    @Test
    void getUserById_UserExists() {
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

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);
        UserResponseDto result = userService.getUserById(id);

        assertEquals(expectedDto, result);
        verify(userRepository).findById(id);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_UserNotFound(){
        Long id = 79L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, ()-> userService.getUserById(id));
        assertEquals("Not found user by id: " + id, exception.getMessage());
        verify(userRepository).findById(id);
        verifyNoInteractions(userMapper);
    }
}