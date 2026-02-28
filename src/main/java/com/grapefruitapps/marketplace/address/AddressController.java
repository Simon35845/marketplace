package com.grapefruitapps.marketplace.address;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/addresses")
public class AddressController {
    private static final Logger log = LoggerFactory.getLogger(AddressController.class);
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressDto>> getAllAddresses(){
        log.info("Called getAllAddresses");
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(
            @RequestBody @Valid AddressDto addressDto
    ){
        log.info("Called createAddress");
        AddressDto createdAddress = addressService.createAddress(addressDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
    }
}
