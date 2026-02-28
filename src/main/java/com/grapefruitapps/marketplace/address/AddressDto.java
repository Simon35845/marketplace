package com.grapefruitapps.marketplace.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressDto(
        Long id,
        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
        String country,
        @NotBlank(message = "City is required")
        @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
        String city,
        @NotBlank(message = "Street is required")
        @Size(min = 2, max = 100, message = "Street must be between 2 and 100 characters")
        String street,
        @NotBlank(message = "House is required")
        @Size(max = 20, message = "House must not exceed 20 characters")
        String house,
        @NotBlank(message = "Apartment is required")
        @Size(max = 20, message = "Apartment must not exceed 20 characters")
        String apartment
) {
}
