package com.grapefruitapps.marketplace.product.controller;

import com.grapefruitapps.marketplace.product.dto.ProductDto;
import com.grapefruitapps.marketplace.product.dto.ProductFilter;
import com.grapefruitapps.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Slf4j
@RequiredArgsConstructor
public class PublicProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable Long id
    ) {
        log.info("Called getProductById: id={}", id);
        ProductDto productDto = productService.getProductById(id);
        return ResponseEntity.ok(productDto);
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getProductsByFilter(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "sellerId", required = false) Long sellerId,
            @RequestParam(name = "sellerName", required = false) String sellerName,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        ProductFilter filter = new ProductFilter(
                name,
                category,
                sellerId,
                sellerName,
                pageSize,
                pageNumber
        );
        log.info("Called getProductsByFilter");
        List<ProductDto> productDtoList = productService.getProductsByFilter(filter);
        return ResponseEntity.ok(productDtoList);
    }
}
