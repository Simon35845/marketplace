package com.grapefruitapps.marketplace.user;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto getUserById(Long id) {
        log.debug("Get user by id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id {} not found in database", id);
            return new EntityNotFoundException("Not found user by id: " + id);
        });
        log.debug("Found user with id: {}", id);
        return userMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.debug("Get all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users ", users.size());
        return users.stream().map(userMapper::toDto).toList();
    }

    public UserDto createUser(UserDto userDto) {
        log.info("Create new user");
        User userToSave = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(userToSave);
        log.info("User was created, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Update user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with id {} not found in database", id);
            throw new EntityNotFoundException("Not found user by id: " + id);
        }

        User userToSave = userMapper.toEntity(userDto);
        userToSave.setId(id);
        User savedUser = userRepository.save(userToSave);
        log.info("User was updated, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public void deleteUser(Long id) {
        log.info("Delete user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with id {} not found in database", id);
            throw new EntityNotFoundException("Not found user by id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User was deleted, id: {}", id);
    }
}
