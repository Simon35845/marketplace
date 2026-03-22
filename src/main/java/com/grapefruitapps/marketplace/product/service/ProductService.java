package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.repository.ProductRepository;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.dto.ProductResponseDto;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserService userService;

    public ProductResponseDto getProductById(Long id) {
        Product product = findProductById(id);
        return productMapper.toDto(product);
    }

    public List<ProductResponseDto> getAllProducts() {
        log.debug("Get all products");
        List<Product> products = productRepository.findAll();
        log.debug("Found {} products", products.size());
        return products.stream().map(productMapper::toDto).toList();
    }

    public List<ProductResponseDto> getProductsBySellerId(Long sellerId) {
        log.debug("Get products by seller id");
        List<Product> products = productRepository.findBySellerId(sellerId);
        log.debug("Found {} products by seller id: {}", products.size(), sellerId);
        return products.stream().map(productMapper::toDto).toList();
    }

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto, Long sellerId) {
        log.info("Create new product");
        User seller = userService.findUserById(sellerId);
        Product productToSave = productMapper.toEntity(productRequestDto);
        productToSave.setSeller(seller);

        Product savedProduct = productRepository.save(productToSave);
        log.info("Product was created, product_id: {}, seller_id: {}", savedProduct.getId(), seller.getId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(
            Long productId,
            ProductRequestDto productRequestDto,
            Long sellerId
    ) {
        log.info("Update product with id: {}", productId);
        checkProductOwnership(productId, sellerId);

        Product productToSave = findProductById(productId);
        productToSave.setName(productRequestDto.name());
        productToSave.setPrice(productRequestDto.price());
        productToSave.setCategory(productRequestDto.category());
        productToSave.setDescription(productRequestDto.description());

        Product savedProduct = productRepository.save(productToSave);
        log.info("Product was updated, id: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        log.info("Delete product with id: {}", productId);
        checkProductOwnership(productId, sellerId);

        if (!productRepository.existsById(productId)) {
            log.warn("Product with id {} not found in database", productId);
            throw new EntityNotFoundException("Not found product by id: " + productId);
        }
        productRepository.deleteById(productId);
        log.info("Product was deleted, id: {}", productId);
    }

    private @NonNull Product findProductById(Long id) {
        log.debug("Get product by id: {}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product with id {} not found in database", id);
            return new EntityNotFoundException("Not found product by id: " + id);
        });
        log.debug("Found product with id: {}", id);
        return product;
    }

    private void checkProductOwnership(Long productId, Long sellerId) {
        Product product = findProductById(productId);
        if (!product.getSeller().getId().equals(sellerId)) {
            log.warn("User {} attempted to access product {} owned by {}",
                    sellerId, productId, product.getSeller().getId());
            throw new AccessDeniedException("You don't have permission to access this product");
        }
    }
}
