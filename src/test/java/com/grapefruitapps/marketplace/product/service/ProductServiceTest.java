package com.grapefruitapps.marketplace.product.service;

import com.grapefruitapps.marketplace.order.entity.OrderItem;
import com.grapefruitapps.marketplace.product.dto.ProductDataDto;
import com.grapefruitapps.marketplace.product.dto.ProductRequestDto;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.repository.ProductRepository;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.entity.UserStatus;
import com.grapefruitapps.marketplace.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductService productService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final String formattedDateTime = currentDateTime.format(FORMATTER);

    private User seller;
    private Product product1;
    private Product product2;
    private ProductDataDto productDataDto;
    private ProductRequestDto productRequestDto;

    @BeforeEach
    void init() {
        seller = User.builder()
                .id(4L)
                .username("nick9429")
                .name("Nikita Lampovetz")
                .email("nicklamp421@gmail.com")
                .status(UserStatus.ACTIVE)
                .build();

        product1 = Product.builder()
                .id(7L)
                .name("RAM 8GB")
                .price(BigDecimal.valueOf(500.00))
                .category("Computer parts")
                .description("Kingston")
                .isVisible(true)
                .isPublished(true)
                .creationDateTime(currentDateTime)
                .seller(seller)
                .cartItems(new ArrayList<>())
                .orderItems(new ArrayList<>())
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .product(product2)
                .quantity(2)
                .build();

        product2 = Product.builder()
                .id(8L)
                .name("Computer Mouse")
                .price(BigDecimal.valueOf(70.00))
                .category("Computer parts")
                .description("Logitech")
                .isVisible(true)
                .isPublished(true)
                .creationDateTime(currentDateTime)
                .seller(seller)
                .cartItems(new ArrayList<>())
                .orderItems(List.of(orderItem1))
                .build();

        productDataDto = new ProductDataDto(
                7L,
                "RAM 8GB",
                BigDecimal.valueOf(500.00),
                "Computer parts",
                "8GB",
                true,
                true,
                formattedDateTime
        );

        productRequestDto = new ProductRequestDto(
                "RAM 8GB",
                BigDecimal.valueOf(500.00),
                "Computer parts",
                "Kingston"
        );
    }

    @Test
    void createProductTest_createProduct() {
        Long sellerId = 4L;

        when(userService.findUserById(sellerId)).thenReturn(seller);
        doNothing().when(userService).checkUserActivity(seller);
        when(productRepository.save(any(Product.class))).thenReturn(product1);
        when(productMapper.toDetailsDto(product1)).thenReturn(productDataDto);

        productService.createProduct(productRequestDto, sellerId);
        verify(userService).findUserById(sellerId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProductTest_productNotUsedInCartItemOrOrderItem() {
        Long productId = 7L;
        Long sellerId = 4L;

        when(productRepository.findByIdWithSeller(productId)).thenReturn(Optional.of(product1));
        doNothing().when(productRepository).deleteById(productId);
        productService.deleteProduct(productId, sellerId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProductTest_productUsedInOrderItem() {
        Long productId = 8L;
        Long sellerId = 4L;

        when(productRepository.findByIdWithSeller(productId)).thenReturn(Optional.of(product2));

        Exception exception = assertThrows(IllegalStateException.class, ()->
                productService.deleteProduct(productId, sellerId));

        assertEquals("Cannot delete product which related to orders", exception.getMessage());
    }

    @Test
    void publishProductTest_productAlreadyPublished(){
        Long productId = 8L;
        Long sellerId = 4L;

        when(productRepository.findByIdWithSeller(productId)).thenReturn(Optional.of(product2));
        doNothing().when(userService).checkUserActivity(seller);

        Exception exception = assertThrows(IllegalStateException.class, ()->
                productService.publishProduct(productId, sellerId));

        assertEquals("This product has already been published", exception.getMessage());
    }
}