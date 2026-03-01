package com.grapefruitapps.marketplace.user;

import com.grapefruitapps.marketplace.address.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AddressRepository addressRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.addressRepository = addressRepository;
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

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Create new user");
        User userToSave = userMapper.toEntity(userDto);
        Address savedAddress = findOrCreateAddress(userToSave.getAddress());
        userToSave.setAddress(savedAddress);

        User savedUser = userRepository.save(userToSave);
        log.info("User was created, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Update user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with id {} not found in database", id);
            throw new EntityNotFoundException("Not found user by id: " + id);
        }

        User userToSave = userMapper.toEntity(userDto);
        Address savedAddress = findOrCreateAddress(userToSave.getAddress());
        userToSave.setId(id);
        userToSave.setAddress(savedAddress);

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

    private Address findOrCreateAddress(Address address) {
        log.info("Finding or creating address: {} {} {} {} {}",
                address.getCountry(), address.getCity(), address.getStreet(),
                address.getHouse(), address.getApartment());
        Optional<Address> existingAddress = addressRepository.findExistingAddress(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment()
        );

        Address savedAddress;
        if (existingAddress.isPresent()) {
            savedAddress = existingAddress.get();
            log.info("Address already exists, id: {}", savedAddress.getId());
        } else {
            savedAddress = addressRepository.save(address);
            log.info("Address was created, id: {}", savedAddress.getId());
        }
        return savedAddress;
    }
}