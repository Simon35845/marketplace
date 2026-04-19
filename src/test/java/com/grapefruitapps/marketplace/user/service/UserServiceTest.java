package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.security.JwtService;
import com.grapefruitapps.marketplace.user.dto.UserCredentialsDto;
import com.grapefruitapps.marketplace.user.dto.UserDto;
import com.grapefruitapps.marketplace.user.dto.UserRegisterDto;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.repository.RoleRepository;
import com.grapefruitapps.marketplace.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    public UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final String formattedDateTime = currentDateTime.format(FORMATTER);

    private User user;
    private Role roleUser;
    private UserDto userDto;
    private UserRegisterDto userRegisterDto;
    private UserCredentialsDto userCredentialsDto;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(1L)
                .username("Anna_3293")
                .password("encoded_password")
                .name("Anna Rich")
                .email("annarich@gmail.com")
                .phone("+721021901342")
                .creationDateTime(currentDateTime)
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        roleUser = new Role("ROLE_USER");

        userDto = new UserDto(
                1L,
                "Anna Rich",
                "annarich@gmail.com",
                "+721021901342",
                formattedDateTime
        );

        userRegisterDto = new UserRegisterDto(
                "Anna_3293",
                "49kdfssa034",
                "annarich@gmail.com",
                "annarich@gmail.com",
                "+721021901342"
        );

        userCredentialsDto = new UserCredentialsDto(
                "Anna_3293",
                "79kdfssd034"
        );
    }

    @Test
    void findUserByIdTest_userNotFound() {
        Long id = 79L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.getUserById(id));
        assertEquals("Not found user by id: " + id, exception.getMessage());
        verify(userRepository).findById(id);
    }

    @Test
    void logIn_nonCorrectPassword(){
        when(userRepository.findByUsername(userCredentialsDto.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userCredentialsDto.password(), user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class,()-> userService.logIn(userCredentialsDto));
        assertEquals("Password is not correct", exception.getMessage());
    }

    @Test
    void createUserTest_ifUsernameAlreadyTaken() {
        when(userRepository.existsByUsername(userRegisterDto.username())).thenReturn(true);
        Exception exception = assertThrows(IllegalStateException.class, () -> userService.createUser(userRegisterDto));
        assertEquals("Username already taken", exception.getMessage());
    }

    @Test
    void createUserTest_createNewUser() {
        when(userRepository.existsByUsername(userRegisterDto.username())).thenReturn(false);
        when(userRepository.existsByEmail(userRegisterDto.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        userService.createUser(userRegisterDto);
        verify(userRepository).existsByUsername(userRegisterDto.username());
        verify(userRepository).existsByEmail(userRegisterDto.email());
        verify(userRepository).save(user);
    }
}