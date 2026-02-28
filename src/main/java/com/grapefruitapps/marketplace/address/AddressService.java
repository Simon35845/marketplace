package com.grapefruitapps.marketplace.address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public AddressService(AddressRepository addressRepository, AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    public List<AddressDto> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(addressMapper::toDto).toList();
    }

    public AddressDto createAddress(AddressDto addressDto){
        log.info("Create new address");
        Address addressToSave = addressMapper.toEntity(addressDto);
        Address savedAddress;
        try {
            savedAddress = addressRepository.save(addressToSave);
            log.info("Address was created, id: {}", savedAddress.getId());
        } catch (DataIntegrityViolationException e) {
            log.warn("Saving address exception");
            throw new IllegalStateException("Address already exists");
        }
        return addressMapper.toDto(savedAddress);
    }
}
