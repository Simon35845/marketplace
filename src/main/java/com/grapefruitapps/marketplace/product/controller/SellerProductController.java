package com.grapefruitapps.marketplace.product.controller;

import com.grapefruitapps.marketplace.product.dto.ProductDataDto;
import com.grapefruitapps.marketplace.product.dto.ProductDataFilter;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/seller/products")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RequiredArgsConstructor
@Validated
public class SellerProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDataDto> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called getProductById: product_id={}, seller_id={}", id, userDetails.getId());
        ProductDataDto productDataDto = productService.getProductDataById(id, userDetails.getId());
        return ResponseEntity.ok(productDataDto);
    }

    @GetMapping
    public ResponseEntity<List<ProductDataDto>> getProductsByFilter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isVisible,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer pageNumber,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ProductDataFilter filter = new ProductDataFilter(
                name,
                category,
                isVisible,
                isPublished,
                pageSize,
                pageNumber
        );
        log.info("Called getProductsByFilter, seller_id={}", userDetails.getId());
        List<ProductDataDto> productDataDtoList = productService.getProductsDataByFilter(filter, userDetails.getId());
        return ResponseEntity.ok(productDataDtoList);
    }

    @PostMapping
    public ResponseEntity<ProductDataDto> createProduct(
            @RequestBody @Valid ProductRequestDto productRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called createProduct: product={}, seller_id={}", productRequestDto, userDetails.getId());
        ProductDataDto productDataDto = productService.createProduct(productRequestDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productDataDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDataDto> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDto productRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called updateProduct: product_id={}, seller_id={}", id, userDetails.getId());
        ProductDataDto productDataDto = productService.updateProduct(id, productRequestDto, userDetails.getId());
        return ResponseEntity.ok(productDataDto);
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductDataDto> changeProductPrice(
            @PathVariable Long id,
            @RequestParam
            @Positive(message = "Price must be positive")
            @Digits(integer = 10, fraction = 2, message = "Price must have exactly 2 decimal places")
            BigDecimal price,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called changeProductPrice: product_id={}, seller_id={}", id, userDetails.getId());
        ProductDataDto productDataDto = productService.changeProductPrice(id, price, userDetails.getId());
        return ResponseEntity.ok(productDataDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called deleteProduct: product_id={}, seller_id={}", id, userDetails.getId());
        productService.deleteProduct(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publishProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called publishProduct: product_id={}, seller_id={}", id, userDetails.getId());
        productService.publishProduct(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Void> changeProductVisibility(
            @PathVariable Long id,
            @RequestParam boolean isVisible,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Called changeProductVisibility: product_id={}, isVisible={}, seller_id={}",
                id, isVisible, userDetails.getId());
        productService.changeProductVisibility(id, isVisible, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
