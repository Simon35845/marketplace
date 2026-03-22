package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.exception.DuplicateFieldException;
import com.grapefruitapps.marketplace.user.dto.*;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.repository.RoleRepository;
import com.grapefruitapps.marketplace.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserService {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public @NonNull User findUserById(Long id) {
        log.info("Get user by id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id {} not found in database", id);
            return new EntityNotFoundException("Not found user by id: " + id);
        });
        log.info("Found user with id: {}", id);
        return user;
    }

    public UserResponseDto getUserById(Long id) {
        User user = findUserById(id);
        log.debug("User entity mapped to DTO, user id: {}", id);
        return userMapper.toDto(user);
    }

    public List<UserResponseDto> searchAllByFilter(UserFilter userFilter) {
        log.debug("Get all users by filter");
        int pageSize = userFilter.pageSize() != null ? userFilter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = userFilter.pageNumber() != null ? userFilter.pageNumber() : DEFAULT_PAGE_NUMBER;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<User> users = userRepository.searchAllByFilter(
                userFilter.name(),
                userFilter.phone(),
                userFilter.email(),
                userFilter.status(),
                userFilter.role(),
                pageable
        );

        log.debug("Found {} users ", users.size());
        return users.stream().map(userMapper::toDto).toList();
    }

    @Transactional
    public UserResponseDto createUser(UserRegisterDto userRegisterDto) {
        log.info("Creating new user");
        if (userRepository.existsByUsername(userRegisterDto.username())) {
            log.warn("User with username {} already exists", userRegisterDto.username());
            throw new DuplicateFieldException("Username already taken");
        }
        if (userRepository.existsByEmail(userRegisterDto.email())) {
            log.warn("User with email {} already exists", userRegisterDto.email());
            throw new DuplicateFieldException("Email already taken");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER");
        User userToSave = User.builder()
                .username(userRegisterDto.username())
                .password(passwordEncoder.encode(userRegisterDto.password()))
                .name(userRegisterDto.name())
                .email(userRegisterDto.email())
                .phone(userRegisterDto.phone())
                .createdAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .roles(List.of(roleUser))
                .build();

        User savedUser = userRepository.save(userToSave);
        log.info("User was created, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        log.info("Updating user with id: {}", id);
        User user = findUserById(id);

        if (!user.getEmail().equals(userRequestDto.email())) {
            if (userRepository.existsByEmail(userRequestDto.email())) {
                log.warn("User with email {} already exists", userRequestDto.email());
                throw new DuplicateFieldException("Email already taken");
            }
        }

        user.setName(userRequestDto.name());
        user.setEmail(userRequestDto.email());
        user.setPhone(userRequestDto.phone());

        User savedUser = userRepository.save(user);
        log.info("User was updated, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with id {} not found in database", id);
            throw new EntityNotFoundException("Not found user by id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User was deleted, id: {}", id);
    }

    @Transactional
    public void markUserForDeletion(Long id) {
        log.info("Marking user for deletion, id: {}", id);
        User user = findUserById(id);

        user.setStatus(UserStatus.TO_DELETE);
        log.info("User marked for deletion, id: {}", id);
    }

    @Transactional
    public void changePassword(Long id, PasswordDto passwordDto) {
        log.info("Changing password in user, id: {}", id);
        User user = findUserById(id);

        if (!passwordEncoder.matches(passwordDto.currentPassword(), user.getPassword())) {
            log.warn("Current password is incorrect for user id: {}", id);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(passwordDto.newPassword(), user.getPassword())) {
            log.warn("New password matches current password for user id: {}", id);
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
        userRepository.save(user);
        log.info("Password was changed, user id: {}", id);
    }
}