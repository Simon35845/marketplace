package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.dto.ProductDetailsDto;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.dto.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getSeller() != null ? product.getSeller().getId() : null
        );
    }

    public ProductDetailsDto toDetailsDto(Product product) {
        return new ProductDetailsDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getStatus(),
                product.getCreationDateTime(),
                product.getSaleDateTime()
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
