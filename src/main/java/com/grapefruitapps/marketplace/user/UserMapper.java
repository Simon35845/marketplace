package com.grapefruitapps.marketplace.user;

import com.grapefruitapps.marketplace.address.Address;
import com.grapefruitapps.marketplace.address.AddressDto;
import com.grapefruitapps.marketplace.address.AddressMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private final AddressMapper addressMapper;

    public UserMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public UserDto toDto(User user) {
        AddressDto addressDto = addressMapper.toDto(user.getAddress());
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                addressDto
        );
    }

    public User toEntity(UserDto userDto) {
        Address address = addressMapper.toEntity(userDto.address());
        return new User(
                userDto.name(),
                userDto.phone(),
                userDto.email(),
                address
        );
    }
}
