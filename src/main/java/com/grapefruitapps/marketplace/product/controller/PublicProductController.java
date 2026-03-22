package com.grapefruitapps.marketplace.product.controller;

import com.grapefruitapps.marketplace.product.dto.ProductResponseDto;
import com.grapefruitapps.marketplace.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@Slf4j
public class PublicProductController {
    private final ProductService productService;

    public PublicProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        log.info("Called getAllProducts");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
            @PathVariable Long id
    ) {
        log.info("Called getProductById: id={}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
