package com.grapefruitapps.marketplace.product.controller;

import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.dto.ProductResponseDto;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile/products")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class ProfileProductController {
    private final ProductService productService;

    public ProfileProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getProductsBySellerId(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getAllProducts");
        return ResponseEntity.ok(productService.getProductsBySellerId(userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestBody @Valid ProductRequestDto productRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called createProduct");
        ProductResponseDto createdProduct = productService.createProduct(productRequestDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDto productRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called updateProduct: id={}, product={}", id, productRequestDto);
        ProductResponseDto updatedProduct = productService.updateProduct(id, productRequestDto, userDetails.getId());
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteProduct: id={}", id);
        productService.deleteProduct(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
