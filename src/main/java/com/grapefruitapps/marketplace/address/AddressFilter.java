package com.grapefruitapps.marketplace.address;

public record AddressFilter(
        String country,
        String city,
        String street,
        String house
) {
}
