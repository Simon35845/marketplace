package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.product.dto.*;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.repository.ProductRepository;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserService userService;

    public ProductDto getProductById(Long id) {
        log.debug("Get product by id={}", id);
        Product product = findProductById(id);
        checkProductAvailability(product);
        return productMapper.toDto(product);
    }

    public ProductDetailsDto getProductById(Long productId, Long sellerId) {
        log.debug("Get product by product_id={} and seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);
        return productMapper.toDetailsDto(product);
    }

    public List<ProductDto> getProductsByFilter(ProductFilter filter) {
        log.debug("Get products by filter");
        int pageSize = filter.pageSize() != null ? filter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : DEFAULT_PAGE_NUMBER;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<Product> products = productRepository.findProductsByFilter(
                filter.sellerId(),
                filter.name(),
                filter.category(),
                true,
                true,
                pageable
        );

        List<Product> activeProducts = products.stream()
                .filter(p->p.getSeller().getStatus() == UserStatus.ACTIVE)
                .toList();

        log.debug("Found {} products", activeProducts.size());
        return activeProducts.stream().map(productMapper::toDto).toList();
    }

    public List<ProductDetailsDto> getProductsByFilter(ProductDetailsFilter filter, Long sellerId) {
        log.debug("Get products by filter by seller_id={}", sellerId);
        int pageSize = filter.pageSize() != null ? filter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : DEFAULT_PAGE_NUMBER;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<Product> products = productRepository.findProductsByFilter(
                sellerId,
                filter.name(),
                filter.category(),
                filter.isVisible(),
                filter.isPublished(),
                pageable
        );
        log.debug("Found {} products by seller_id={}", products.size(), sellerId);
        return products.stream().map(productMapper::toDetailsDto).toList();
    }

    @Transactional
    public ProductDetailsDto createProduct(ProductRequestDto productRequestDto, Long sellerId) {
        log.info("Creating new product: seller_id={}", sellerId);
        User seller = userService.findUserById(sellerId);
        userService.checkUserActivity(seller);
        Product product = productMapper.toEntity(productRequestDto);
        product.setSeller(seller);
        product.setVisible(false);
        product.setPublished(false);
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
        log.info("Updating product: product_id={}, seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        userService.checkUserActivity(product.getSeller());
        checkProductOwnership(product, sellerId);

        if (product.isPublished()) {
            throw new IllegalStateException("Cannot modify published product");
        }

        product.setName(productRequestDto.name());
        product.setPrice(productRequestDto.price());
        product.setCategory(productRequestDto.category());
        product.setDescription(productRequestDto.description());

        Product savedProduct = productRepository.save(product);
        log.info("Product was updated: product_id={}, seller_id={}", productId, sellerId);
        return productMapper.toDetailsDto(savedProduct);
    }

    @Transactional
    public void publishProduct(Long productId, Long sellerId) {
        log.info("Publishing product: product_id={}, seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        userService.checkUserActivity(product.getSeller());
        checkProductOwnership(product, sellerId);

        if (product.isPublished()) {
            throw new IllegalStateException("This product has already been published");
        }

        product.setPublished(true);
        product.setVisible(true);
        productRepository.save(product);
        log.info("Product was published: product_id={}, seller_id={}", productId, sellerId);
    }

    @Transactional
    public void changeProductVisibility(Long productId, boolean isVisible, Long sellerId) {
        log.info("Changing product visibility, product_id={}, isVisible={}, seller_id={}",
                productId, isVisible, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);

        if (!product.isPublished()) {
            throw new IllegalStateException("Unpublished product cannot be visible");
        }

        product.setVisible(isVisible);
        productRepository.save(product);
        log.info("Product visibility was changed: product_id={}, seller_id={}", productId, sellerId);
    }

    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        log.info("Deleting product: product_id={}, seller_id={}", productId, sellerId);
        Product product = findProductById(productId);
        checkProductOwnership(product, sellerId);

        if (!product.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete product which related to orders");
        }

        productRepository.deleteById(productId);
        log.info("Product was deleted: product_id={}, seller_id={}", productId, sellerId);
    }

    public @NonNull Product findProductById(Long id) {
        log.debug("Finding product by id: {}", id);
        Product product = productRepository.findByIdWithSeller(id).orElseThrow(() -> {
            log.warn("Product with id {} not found in database", id);
            return new EntityNotFoundException("Not found product by id: " + id);
        });
        log.debug("Found product with id: {}", id);
        return product;
    }

    private void checkProductOwnership(Product product, Long sellerId) {
        log.debug("Checking product ownership, product_id={}, seller_id={}", product.getId(), sellerId);
        if (!isProductOwner(product, sellerId)) {
            log.warn("Seller with id={} attempted to access product with id={} owned by another seller with id={}",
                    sellerId, product.getId(), product.getSeller().getId());
            throw new AccessDeniedException("You don't have permission to access this product");
        }
    }

    public void checkProductAvailability(Product product) {
        log.debug("Checking product availability, product_id={}", product.getId());
        boolean isActiveSeller = product.getSeller().getStatus() == UserStatus.ACTIVE;

        if (!product.isPublished() || !product.isVisible() || !isActiveSeller) {
            throw new IllegalStateException("Product is not available");
        }
    }

    public boolean isProductOwner(Product product, Long sellerId) {
        return product.getSeller().getId().equals(sellerId);
    }
}