package com.grapefruitapps.marketplace.product;

import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product){
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription()
        );
    }

    public Product toEntity(ProductDto productDto){
        return new Product(
                productDto.name(),
                productDto.price(),
                productDto.category(),
                productDto.description()
        );
    }
}
