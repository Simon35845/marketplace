package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.dto.ProductResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponseDto toDto(Product product){
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getSeller().getId()
        );
    }

    public Product toEntity(ProductRequestDto productRequestDto){
        return new Product(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.category(),
                productRequestDto.description()
        );
    }
}
