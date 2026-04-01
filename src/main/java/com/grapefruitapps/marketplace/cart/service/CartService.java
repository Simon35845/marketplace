package com.grapefruitapps.marketplace.cart.service;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemRequestDto;
import com.grapefruitapps.marketplace.cart.entity.Cart;
import com.grapefruitapps.marketplace.cart.entity.CartItem;
import com.grapefruitapps.marketplace.cart.repository.CartItemRepository;
import com.grapefruitapps.marketplace.cart.repository.CartRepository;
import com.grapefruitapps.marketplace.product.entity.Product;
import com.grapefruitapps.marketplace.product.service.ProductService;
import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Cart cart = findByBuyerIdWithAllDetails(buyerId);
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto clearCart(Long buyerId) {
        log.debug("Clear cart by buyer_id={}", buyerId);
        Cart cart = findCartByBuyerId(buyerId);
        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.getCartItems().clear();
        log.info("Cart was cleared");
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto addItemToCart(CartItemRequestDto itemDto, Long buyerId) {
        long productId = itemDto.productId();
        int quantity = itemDto.quantity();
        log.info("Adding item to cart: product_id={}, quantity={}, buyer_id={}", productId, quantity, buyerId);
        Cart cart = getOrCreateCart(buyerId);
        Product product = productService.findProductById(productId);

        if (productService.isProductOwner(product, buyerId)) {
            throw new IllegalArgumentException("Cannot add your own product to cart");
        }
        productService.checkProductNotSold(product);
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
            log.info("Increased quantity of existing item: id={}, new quantity={}",
                    item.getId(), item.getQuantity());
        } else {
            CartItem item = new CartItem(cart, product, quantity);
            cartItemRepository.save(item);
            log.info("Item added to cart: id={}", item.getId());
        }
        return getCart(buyerId);
    }

    @Transactional
    public CartDto changeItemQuantity(Long itemId, Integer quantity, Long buyerId) {
        log.info("Updating cart item quantity: item_id={}, quantity={}, buyer_id={}", itemId, quantity, buyerId);
        CartItem item = findCartItemByIdWithCartAndBuyer(itemId);
        checkCartItemOwnerShip(item, buyerId);

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        log.info("Cart item quantity was updated");
        return getCart(buyerId);
    }

    @Transactional
    public CartDto deleteItem(Long itemId, Long buyerId) {
        log.info("Deleting cart item: cartItemId={}, buyer_id={}", itemId, buyerId);
        CartItem item = findCartItemByIdWithCartAndBuyer(itemId);
        checkCartItemOwnerShip(item, buyerId);

        cartItemRepository.deleteById(itemId);
        log.info("Cart item was deleted");
        return getCart(buyerId);
    }



    public @NonNull Cart  findCartByBuyerId(Long buyerId) {
        log.debug("Finding cart by buyer_id={}", buyerId);
        return cartRepository.findByBuyerId(buyerId).orElseThrow(() -> {
            log.warn("Cart with buyer_id {} not found in database", buyerId);
            return new EntityNotFoundException("Not found cart for current user");
        });
    }

    public @NonNull Cart findByBuyerIdWithAllDetails(Long buyerId) {
        log.debug("Finding cart by buyer_id={} with items and products", buyerId);
        return cartRepository.findByBuyerIdWithAllDetails(buyerId).orElseThrow(() -> {
            log.warn("Cart with buyer_id {} not found in database", buyerId);
            return new EntityNotFoundException("Not found cart for current user");
        });
    }

    private @NonNull CartItem findCartItemByIdWithCartAndBuyer(Long id) {
        log.debug("Finding cart item with id={}", id);
        return cartItemRepository.findByIdWithCartAndBuyer(id).orElseThrow(() -> {
            log.warn("Cart item with id {} not found in database", id);
            return new EntityNotFoundException("Not found cart item by id: " + id);
        });
    }

    private @NonNull Cart getOrCreateCart(Long buyerId) {
        log.debug("Getting or creating cart for buyer_id={}", buyerId);
        User buyer = userService.findUserById(buyerId);
        return cartRepository.findByBuyerId(buyerId).orElseGet(() -> {
            log.info("Creating new cart for buyer_id={}", buyerId);
            return cartRepository.save(new Cart(buyer));
        });
    }

    private void checkCartItemOwnerShip(CartItem item, Long buyerId) {
        log.debug("Checking cart item ownership, item_id={}, buyerId_id={}", item.getId(), buyerId);
        if (!item.getCart().getBuyer().getId().equals(buyerId)) {
            log.warn("Buyer with id={} attempted to access cart item with id={} owned by another buyer with id={}",
                    buyerId, item.getId(), item.getCart().getBuyer().getId());
            throw new AccessDeniedException("This cart item does not belong to the user");
        }
    }

    public void checkCartIsEmpty(Long buyerId, Cart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
    }
}
