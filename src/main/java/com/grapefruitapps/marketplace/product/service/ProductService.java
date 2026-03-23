package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.dto.ProductDetailsDto;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.entity.ProductStatus;
import com.grapefruitapps.marketplace.product.repository.ProductRepository;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.dto.ProductDto;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserService userService;

    public ProductDto getProductById(Long id) {
        log.debug("Get product by id={}", id);
        Product product = findProductById(id);
        return productMapper.toDto(product);
    }

    public ProductDetailsDto getProduct(Long productId, Long sellerId) {
        log.debug("Get product by product_id={} and seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);
        return productMapper.toDetailsDto(product);
    }

    public List<ProductDto> getAllProducts() {
        log.debug("Get all products");
        List<Product> products = productRepository.findAll();
        log.debug("Found {} products", products.size());
        return products.stream().map(productMapper::toDto).toList();
    }

    public List<ProductDetailsDto> getProductsBySellerId(Long sellerId) {
        log.debug("Get products by seller_id={}", sellerId);
        List<Product> products = productRepository.findBySellerId(sellerId);
        log.debug("Found {} products by seller_id={}", products.size(), sellerId);
        return products.stream().map(productMapper::toDetailsDto).toList();
    }

    @Transactional
    public ProductDetailsDto createProduct(ProductRequestDto productRequestDto, Long sellerId) {
        log.info("Create new product, seller_id={}", sellerId);
        User seller = userService.findUserById(sellerId);
        Product product = productMapper.toEntity(productRequestDto);
        product.setSeller(seller);
        product.setStatus(ProductStatus.CREATED);
        product.setCreationDateTime(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("Product was created, product_id={}, seller_id={}", savedProduct.getId(), seller.getId());
        return productMapper.toDetailsDto(savedProduct);
    }

    @Transactional
    public ProductDetailsDto updateProduct(
            Long productId,
            ProductRequestDto productRequestDto,
            Long sellerId
    ) {
        log.info("Update product with product_id={} and seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);
        checkProductNotSold(product);

        product.setName(productRequestDto.name());
        product.setPrice(productRequestDto.price());
        product.setCategory(productRequestDto.category());
        product.setDescription(productRequestDto.description());

        Product savedProduct = productRepository.save(product);
        log.info("Product was updated, product_id={}, seller_id={}", savedProduct.getId(), sellerId);
        return productMapper.toDetailsDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        log.info("Delete product with product_id={} and seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);
        checkProductNotSold(product);

        productRepository.deleteById(productId);
        log.info("Product was deleted, product_id={}, seller_id={}", productId, sellerId);
    }

    @Transactional
    public ProductDetailsDto markProductAsSold(Long productId, Long sellerId) {
        log.info("Mark product as sold, product_id={} and seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);
        checkProductNotSold(product);

        product.setStatus(ProductStatus.SOLD);
        product.setSaleDateTime(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("Product marked as sold, product_id={}, seller_id={}", productId, sellerId);
        return productMapper.toDetailsDto(savedProduct);
    }

    private @NonNull Product findProductById(Long id) {
        log.debug("Finding product by id: {}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product with id {} not found in database", id);
            return new EntityNotFoundException("Not found product by id: " + id);
        });
        log.debug("Found product with id: {}", id);
        return product;
    }

    private void checkProductOwnership(Product product, Long sellerId) {
        log.debug("Checking product ownership, product_id={}, seller_id={}", product.getId(), sellerId);
        if (!product.getSeller().getId().equals(sellerId)) {
            log.warn("Seller with id={} attempted to access product with id={} owned by another seller with id={}",
                    sellerId, product.getId(), product.getSeller().getId());
            throw new AccessDeniedException("You don't have permission to access this product");
        }
    }

    private void checkProductNotSold(Product product) {
        log.debug("Checking that product is not sold, product_id={}", product.getId());
        if (product.getStatus() == ProductStatus.SOLD) {
            log.warn("Attempt to modify sold product with id={}", product.getId());
            throw new IllegalStateException("Cannot modify sold product");
        }
    }
}
