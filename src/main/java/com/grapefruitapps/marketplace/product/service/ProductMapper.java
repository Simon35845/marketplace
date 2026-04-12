package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.dto.ProductDataDto;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.dto.ProductDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ProductMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getSeller().getId(),
                product.getSeller().getName()
        );
    }

    public ProductDataDto toDetailsDto(Product product) {
        return new ProductDataDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.isVisible(),
                product.isPublished(),
                product.getCreationDateTime().format(FORMATTER)
        );
    }

    public Product toEntity(ProductRequestDto productRequestDto) {
        return new Product(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.category(),
                productRequestDto.description()
        );
    }
}
