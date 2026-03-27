package com.grapefruitapps.marketplace.cart.service;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.cart.repository.CartItemRepository;
import com.grapefruitapps.marketplace.cart.repository.CartRepository;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.entity.ProductStatus;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartMapper cartMapper;

    public CartDto getCart(Long buyerId) {
        log.debug("Get cart by buyer_id={}", buyerId);
        Cart cart = findCartByBuyerId(buyerId);
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto clearCart(Long buyerId){
        log.debug("Clear cart by buyer_id={}", buyerId);
        Cart cart = findCartByBuyerId(buyerId);
        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.getCartItems().clear();
        log.info("Cart was cleared");
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto createCartItem(Long productId, Integer quantity, Long buyerId) {
        log.info("Creating cart item: product_id={}, quantity={}, buyer_id={}", productId, quantity, buyerId);
        isQuantityPositive(quantity);
        Cart cart = getOrCreateCart(buyerId);
        Product product = productService.findProductById(productId);

        checkProductOwnerShip(buyerId, product);
        checkProductSold(product);

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
            log.info("Increased quantity of existing item: id={}, new quantity={}",
                    cartItem.getId(), cartItem.getQuantity());
        } else {
            CartItem cartItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(cartItem);
            log.info("Created new cart item: id={}", cartItem.getId());
        }
        return getCart(buyerId);
    }

    @Transactional
    public CartDto updateCartItemQuantity(Long cartItemId, Integer quantity, Long buyerId) {
        log.info("Updating cart item quantity: cartItemId={}, quantity={}, buyer_id={}", cartItemId, quantity, buyerId);
        isQuantityPositive(quantity);
        CartItem cartItem = findCartItemById(cartItemId);
        checkCartItemOwnerShip(cartItem, buyerId);

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        log.info("Cart item quantity was updated");
        return getCart(buyerId);
    }

    @Transactional
    public CartDto deleteCartItem(Long cartItemId, Long buyerId) {
        log.info("Deleting cart item: cartItemId={}, buyer_id={}", cartItemId, buyerId);

        CartItem cartItem = findCartItemById(cartItemId);

        checkCartItemOwnerShip(cartItem, buyerId);

        cartItemRepository.deleteById(cartItemId);
        log.info("Cart item was deleted");
        return getCart(buyerId);
    }

    private void checkCartItemOwnerShip(CartItem cartItem, Long buyerId) {
        log.debug("Checking cart item ownership, cartItem_id={}, buyerId_id={}", cartItem.getId(), buyerId);
        if (!cartItem.getCart().getBuyer().getId().equals(buyerId)) {
            throw new AccessDeniedException("This cart item does not belong to the user");
        }
    }

    private Cart findCartByBuyerId(Long buyerId) {
        log.debug("Finding cart by buyer_id={}", buyerId);
        return cartRepository.findByBuyerId(buyerId).orElseThrow(() ->
                new EntityNotFoundException("Not found cart for current user"));
    }

    private void isQuantityPositive(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private CartItem findCartItemById(Long id) {
        log.debug("Finding cart item with id={}", id);
        return cartItemRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found cart item by id: " + id));
    }

    private @NonNull Cart getOrCreateCart(Long buyerId) {
        log.debug("Getting or creating cart for buyer_id={}", buyerId);
        User buyer = userService.findUserById(buyerId);
        return cartRepository.findByBuyerId(buyerId).orElseGet(() -> {
            log.info("Creating new cart for buyer_id={}", buyerId);
            Cart newCart = new Cart(buyer);
            return cartRepository.save(newCart);
        });
    }

    private void checkProductOwnerShip(Long buyerId, Product product) {
        if (product.getSeller().getId().equals(buyerId)) {
            throw new IllegalArgumentException("Cannot add your own product to cart");
        }
    }

    private void checkProductSold(Product product) {
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new IllegalStateException("Cannot add sold product to cart");
        }
    }
}
