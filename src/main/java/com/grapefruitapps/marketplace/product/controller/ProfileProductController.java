package com.grapefruitapps.marketplace.product.controller;

import com.grapefruitapps.marketplace.product.dto.ProductDetailsDto;
import com.grapefruitapps.marketplace.product.dto.ProductDetailsFilter;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.entity.ProductStatus;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProfileProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDetailsDto>> getAllProducts(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) ProductStatus status,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ProductDetailsFilter filter = new ProductDetailsFilter(
                name,
                category,
                status,
                pageSize,
                pageNumber
        );
        log.info("Called getAllProducts, seller_id={}", userDetails.getId());
        return ResponseEntity.ok(productService.getProductsByFilter(filter, userDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getProductById: product_id={}, seller_id={}", id, userDetails.getId());
        return ResponseEntity.ok(productService.getProductById(id, userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<ProductDetailsDto> createProduct(
            @RequestBody @Valid ProductRequestDto productRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called createProduct: product={}, seller_id={}", productRequestDto, userDetails.getId());
        ProductDetailsDto createdProduct = productService.createProduct(productRequestDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDto productRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called updateProduct: product_id={}, product={}, seller_id={}",
                id, productRequestDto, userDetails.getId());
        ProductDetailsDto updatedProduct = productService.updateProduct(id, productRequestDto, userDetails.getId());
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteProduct: product_id={}, seller_id={}", id, userDetails.getId());
        productService.deleteProduct(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
