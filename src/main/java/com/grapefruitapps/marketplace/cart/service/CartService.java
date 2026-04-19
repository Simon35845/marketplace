package com.grapefruitapps.marketplace.cart.service;

import com.grapefruitapps.marketplace.cart.dto.CartDto;
import com.grapefruitapps.marketplace.cart.dto.CartItemDto;
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
    private final CartMapper cartMapper;
    private final UserService userService;
    private final ProductService productService;

    public CartDto getCartByBuyerId(Long buyerId) {
        log.debug("Get cart by buyer_id={}", buyerId);
        Cart cart = findCartByBuyerIdWithAllDetails(buyerId);
        return cartMapper.toCartDto(cart);
    }

    public CartItemDto getCartItemById(Long itemId, Long buyerId) {
        log.debug("Get cart item by item_id={}, buyer_id={}", itemId, buyerId);
        CartItem item = findCartItemByIdWithAllDetails(itemId);
        checkCartItemOwnerShip(item, buyerId);
        return cartMapper.toCartItemDto(item);
    }

    @Transactional
    public void clearCart(Long buyerId) {
        log.debug("Clear cart by buyer_id={}", buyerId);
        Cart cart = findCartByBuyerId(buyerId);
        cartItemRepository.deleteAllByCartId(cart.getId());
        log.info("Cart was cleared");
    }

    @Transactional
    public CartItemDto addItemToCart(CartItemRequestDto itemDto, Long buyerId) {
        log.info("Adding item to cart: product_id={}, quantity={}, buyer_id={}", itemDto.productId(), itemDto.quantity(), buyerId);
        Cart cart = getOrCreateCart(buyerId);
        userService.checkUserActivity(cart.getBuyer());
        Product product = productService.findProductById(itemDto.productId());

        if (productService.isProductOwner(product, buyerId)) {
            throw new IllegalStateException("Cannot add your own product to cart");
        }
        productService.checkProductAvailability(product);
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        CartItem item;

        if (existingItem.isPresent()) {
            item = existingItem.get();
            item.setQuantity(item.getQuantity() + itemDto.quantity());
            cartItemRepository.save(item);
            log.info("Increased quantity of existing item: id={}, new quantity={}",
                    item.getId(), item.getQuantity());
        } else {
            CartItem itemToSave = new CartItem(cart, product, itemDto.quantity());
            item = cartItemRepository.save(itemToSave);
            log.info("Item added to cart: id={}", item.getId());
        }
        return cartMapper.toCartItemDto(item);
    }

    @Transactional
    public CartItemDto changeItemQuantity(Long itemId, Integer quantity, Long buyerId) {
        log.info("Updating cart item quantity: item_id={}, quantity={}, buyer_id={}", itemId, quantity, buyerId);
        CartItem item = findCartItemByIdWithAllDetails(itemId);
        userService.checkUserActivity(item.getCart().getBuyer());
        checkCartItemOwnerShip(item, buyerId);

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        log.info("Cart item quantity was updated");
        return cartMapper.toCartItemDto(item);
    }

    @Transactional
    public void deleteItem(Long itemId, Long buyerId) {
        log.info("Deleting cart item: cartItemId={}, buyer_id={}", itemId, buyerId);
        CartItem item = findCartItemByIdWithAllDetails(itemId);
        checkCartItemOwnerShip(item, buyerId);

        cartItemRepository.deleteById(itemId);
        log.info("Cart item was deleted");
    }

    public @NonNull Cart findCartByBuyerId(Long buyerId) {
        log.debug("Finding cart by buyer_id={}", buyerId);
        return cartRepository.findByBuyerId(buyerId).orElseThrow(() -> {
            log.warn("Cart with buyer_id {} not found in database", buyerId);
            return new EntityNotFoundException("Not found cart for current user");
        });
    }

    public @NonNull Cart findCartByBuyerIdWithAllDetails(Long buyerId) {
        log.debug("Finding cart by buyer_id={} with items and products", buyerId);
        return cartRepository.findByBuyerIdWithAllDetails(buyerId).orElseThrow(() -> {
            log.warn("Cart with buyer_id {} not found in database", buyerId);
            return new EntityNotFoundException("Not found cart for current user. " +
                    "The cart will be created when product is added to the cart");
        });
    }

    private @NonNull CartItem findCartItemByIdWithAllDetails(Long id) {
        log.debug("Finding cart item with id={}", id);
        return cartItemRepository.findByCartItemIdWithAllDetails(id).orElseThrow(() -> {
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

    public void checkCartIsEmpty(Cart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
    }
}
