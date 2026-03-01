package com.grapefruitapps.marketplace.user;

import com.grapefruitapps.marketplace.address.AddressFilter;

public record UserFilter(
        String name,
        String phone,
        String email,
        AddressFilter address,
        Integer pageSize,
        Integer pageNumber
) {
}
