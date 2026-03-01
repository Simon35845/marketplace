package com.grapefruitapps.marketplace.user;

import com.grapefruitapps.marketplace.address.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

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

    public List<UserDto> searchAllByFilter(UserFilter userFilter) {
        log.debug("Get all users by filter");
        int pageSize = userFilter.pageSize() != null ? userFilter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = userFilter.pageNumber() != null ? userFilter.pageNumber() : DEFAULT_PAGE_NUMBER;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<User> users = userRepository.searchAllByFilter(
                userFilter.name(),
                userFilter.phone(),
                userFilter.email(),
                userFilter.address().country(),
                userFilter.address().city(),
                userFilter.address().street(),
                userFilter.address().house(),
                pageable
        );
        log.debug("Found {} users ", users.size());
        return users.stream().map(userMapper::toDto).toList();
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating new user");
        User userToSave = userMapper.toEntity(userDto);
        Address savedAddress = findOrCreateAddress(userToSave.getAddress());
        userToSave.setAddress(savedAddress);

        User savedUser = userRepository.save(userToSave);
        log.info("User was created, id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user with id: {}", id);
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
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with id {} not found in database", id);
            throw new EntityNotFoundException("Not found user by id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User was deleted, id: {}", id);
    }

    private Address findOrCreateAddress(Address address) {
        log.debug("Finding or creating address: {} {} {} {} {}",
                address.getCountry(), address.getCity(), address.getStreet(),
                address.getHouse(), address.getApartment());

        return addressRepository.findExistingAddress(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment()
        ).orElseGet(() -> addressRepository.save(address));
    }
}