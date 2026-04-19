package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.dto.ProductDataDto;
import com.grapefruitapps.marketplace.product.dto.ProductDto;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {
    private ProductMapper productMapper = new ProductMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final String formattedDateTime = currentDateTime.format(FORMATTER);
    private Product product;

    @BeforeEach
    void init() {
        User seller = User.builder()
                .id(4L)
                .username("nick9429")
                .name("Nikita Lampovetz")
                .email("nicklamp421@gmail.com")
                .status(UserStatus.ACTIVE)
                .build();

        product = Product.builder()
                .id(7L)
                .name("RAM 8GB")
                .price(BigDecimal.valueOf(500.00))
                .category("Computer parts")
                .description("Kingston")
                .isVisible(true)
                .isPublished(true)
                .creationDateTime(currentDateTime)
                .seller(seller)
                .build();
    }

    @Test
    void toDto() {
        ProductDto expectedDto = new ProductDto(
                7L,
                "RAM 8GB",
                BigDecimal.valueOf(500.00),
                "Computer parts",
                "Kingston",
                4L,
                "Nikita Lampovetz"
        );

        ProductDto actualDto = productMapper.toDto(product);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void toDetailsDto() {
        ProductDataDto expectedDto = new ProductDataDto(
                7L,
                "RAM 8GB",
                BigDecimal.valueOf(500.00),
                "Computer parts",
                "Kingston",
                true,
                true,
                formattedDateTime
        );

        ProductDataDto actualDto = productMapper.toDetailsDto(product);
        assertEquals(expectedDto, actualDto);
    }
}