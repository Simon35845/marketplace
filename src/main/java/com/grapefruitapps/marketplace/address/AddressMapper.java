package com.grapefruitapps.marketplace.address;

import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressDto toDto(Address address){
        return new AddressDto(
                address.getId(),
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment()
        );
    }

    public Address toEntity(AddressDto addressDto){
        return new Address(
                addressDto.country(),
                addressDto.city(),
                addressDto.street(),
                addressDto.house(),
                addressDto.apartment()
        );
    }
}
