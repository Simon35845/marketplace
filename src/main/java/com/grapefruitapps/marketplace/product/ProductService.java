package com.grapefruitapps.marketplace.product;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductDto getProductById(Long id) {
        log.debug("Get product by id: {}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product with id {} not found in database", id);
            return new EntityNotFoundException("Not found product by id: " + id);
        });
        log.debug("Found product with id: {}", id);
        return productMapper.toDto(product);
    }

    public List<ProductDto> getAllProducts() {
        log.debug("Get all products");
        List<Product> products = productRepository.findAll();
        log.debug("Found {} products", products.size());
        return products.stream().map(productMapper::toDto).toList();
    }

    public ProductDto createProduct(ProductDto productDto) {
        log.info("Create new product");
        Product productToSave = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(productToSave);
        log.info("Product was created, id: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        log.info("Update product with id: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product with id {} not found in database", id);
            throw new EntityNotFoundException("Not found product by id: " + id);
        }

        Product productToSave = productMapper.toEntity(productDto);
        productToSave.setId(id);
        Product savedProduct = productRepository.save(productToSave);
        log.info("Product was updated, id: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    public void deleteProduct(Long id){
        log.info("Delete product with id: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product with id {} not found in database", id);
            throw new EntityNotFoundException("Not found product by id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product was deleted, id: {}", id);
    }
}
