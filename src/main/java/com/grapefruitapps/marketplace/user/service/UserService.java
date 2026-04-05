package com.grapefruitapps.marketplace.user.service;

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
        log.debug("Get user by id={}", id);
        checkUserActivity(user);
        return userMapper.toDto(user);
    }

    public UserDataDto getUserDataById(Long id) {
        User user = findUserById(id);
        log.debug("Get user data by id={}", id);
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
                UserStatus.ACTIVE,
                "ROLE_USER",
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

        List<User> users = userRepository.findUsersByFilter(
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
            throw new IllegalStateException("Username already taken");
        }
        if (userRepository.existsByEmail(userRegisterDto.email())) {
            throw new IllegalStateException("Email already taken");
        }

        Role roleUser = findRoleByName("ROLE_USER");
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
        log.info("User was created, id={}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        log.info("Updating user with id={}", id);
        User user = findUserById(id);

        if (!user.getEmail().equals(userUpdateDto.email())) {
            if (userRepository.existsByEmail(userUpdateDto.email())) {
                throw new IllegalStateException("Email already taken");
            }
        }

        user.setName(userUpdateDto.name());
        user.setEmail(userUpdateDto.email());
        user.setPhone(userUpdateDto.phone());

        User savedUser = userRepository.save(user);
        log.info("User was updated, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void changePassword(Long id, PasswordDto passwordDto) {
        log.info("Changing password from user with id={}", id);
        User user = findUserById(id);

        if (!passwordEncoder.matches(passwordDto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(passwordDto.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
        userRepository.save(user);
        log.info("Password was changed, user_id={}", id);
    }

    @Transactional
    public void deletionRequest(Long id) {
        log.info("Requesting deletion, user_id={}", id);
        User user = findUserById(id);

        if(user.getStatus() != UserStatus.ACTIVE){
            throw new IllegalStateException("Cannot request deletion for user with status "
                    + UserStatus.TO_DELETE);
        }

        user.setStatus(UserStatus.TO_DELETE);
        log.info("User marked for deletion, id: {}", id);
    }

    @Transactional
    public void cancelDeletionRequest(Long id) {
        log.info("Canceling deletion request, user_id={}", id);
        User user = findUserById(id);

        if(user.getStatus() != UserStatus.TO_DELETE){
            throw new IllegalStateException("Cannot cancel deletion request for user with status "
                    + UserStatus.ACTIVE);
        }

        user.setStatus(UserStatus.ACTIVE);
        log.info("User marked for deletion, id: {}", id);
    }

    @Transactional
    public void deleteUser(Long userId, Long adminId) {
        log.info("Admin with id={} deleting user with id={}", adminId, userId);

        if (adminId.equals(userId)) {
            throw new IllegalArgumentException("Cannot delete your own account. Use deletion request instead");
        }

        User user = findUserById(userId);
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("User was deleted, id={}", userId);
    }

    @Transactional
    public void grantAdminRole(Long userId, Long adminId) {
        log.info("Admin with id={} granting admin role to user with id={}", adminId, userId);

        if (adminId.equals(userId)) {
            throw new IllegalArgumentException("Cannot change your own role");
        }

        User user = findUserById(userId);
        Role adminRole = findRoleByName("ROLE_ADMIN");

        if (user.getRoles().contains(adminRole)) {
            throw new IllegalStateException("User already has admin role");
        }

        user.getRoles().add(adminRole);
        userRepository.save(user);
        log.info("Admin role granted to user, id={}", userId);
    }

    @Transactional
    public void revokeAdminRole(Long userId, Long adminId) {
        log.info("Admin with id={} revoking admin role from user with id={}", adminId, userId);

        if (adminId.equals(userId)) {
            throw new IllegalArgumentException("Cannot change your own role");
        }

        User user = findUserById(userId);
        Role adminRole = findRoleByName("ROLE_ADMIN");

        if (!user.getRoles().contains(adminRole)) {
            throw new IllegalStateException("User does not have admin role");
        }

        user.getRoles().remove(adminRole);
        userRepository.save(user);
        log.info("Admin role revoked from user, id={}", userId);
    }


    public @NonNull User findUserById(Long id) {
        log.info("Finding user by id={}", id);
        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id={} not found in database", id);
            return new EntityNotFoundException("Not found user by id: " + id);
        });
    }

    private @NonNull Role findRoleByName(String roleName) {
        log.debug("Finding role by name={}", roleName);
        return roleRepository.findByName(roleName).orElseThrow(() -> {
            log.warn("Role with name={} not found in database", roleName);
            return new EntityNotFoundException("Role not found");
        });
    }

    public void checkUserActivity(User user){
        log.debug("Checking user is not active, user_id={}", user);
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new IllegalStateException("User is not active");
        }
    }
}