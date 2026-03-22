package com.grapefruitapps.marketplace.user.service;

import com.grapefruitapps.marketplace.exception.DuplicateFieldException;
import com.grapefruitapps.marketplace.user.dto.*;
import com.grapefruitapps.marketplace.user.entity.Role;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.repository.RoleRepository;
import com.grapefruitapps.marketplace.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserService {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto getUserById(Long id) {
        User user = findUserById(id);
        log.debug("Get user by id: {}", id);
        return userMapper.toDto(user);
    }

    public UserDataDto getUserDataById(Long id) {
        User user = findUserById(id);
        log.debug("Get user data by id: {}", id);
        return userMapper.toDataDto(user);
    }

    public List<UserDto> getUsersByFilter(UserFilter userFilter) {
        log.debug("Get users by filter");
        int pageSize = userFilter.pageSize() != null ? userFilter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = userFilter.pageNumber() != null ? userFilter.pageNumber() : DEFAULT_PAGE_NUMBER;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<User> users = userRepository.findUsersByFilter(
                userFilter.name(),
                userFilter.phone(),
                userFilter.email(),
                pageable
        );

        log.debug("Found {} users ", users.size());
        return users.stream().map(userMapper::toDto).toList();
    }

    public List<UserDataDto> getUserDataByFilter(UserDataFilter userDataFilter) {
        log.debug("Get user data by filter");
        int pageSize = userDataFilter.pageSize() != null ? userDataFilter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = userDataFilter.pageNumber() != null ? userDataFilter.pageNumber() : DEFAULT_PAGE_NUMBER;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<User> users = userRepository.findUserDataByFilter(
                userDataFilter.name(),
                userDataFilter.phone(),
                userDataFilter.email(),
                userDataFilter.status(),
                userDataFilter.role(),
                pageable
        );

        log.debug("Found {} users ", users.size());
        return users.stream().map(userMapper::toDataDto).toList();
    }

    @Transactional
    public UserDto createUser(UserRegisterDto userRegisterDto) {
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
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        log.info("Updating user with id: {}", id);
        User user = findUserById(id);

        if (!user.getEmail().equals(userUpdateDto.email())) {
            if (userRepository.existsByEmail(userUpdateDto.email())) {
                log.warn("User with email {} already exists", userUpdateDto.email());
                throw new DuplicateFieldException("Email already taken");
            }
        }

        user.setName(userUpdateDto.name());
        user.setEmail(userUpdateDto.email());
        user.setPhone(userUpdateDto.phone());

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

    public @NonNull User findUserById(Long id) {
        log.info("Finding user by id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id {} not found in database", id);
            return new EntityNotFoundException("Not found user by id: " + id);
        });
        log.info("Found user with id: {}", id);
        return user;
    }
}