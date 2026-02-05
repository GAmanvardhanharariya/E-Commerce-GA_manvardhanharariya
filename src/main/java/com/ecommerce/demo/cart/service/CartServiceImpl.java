package com.ecommerce.demo.cart.service;

import com.ecommerce.demo.auth.entity.AppUser;
import com.ecommerce.demo.auth.repository.UserRepository;
import com.ecommerce.demo.cart.dto.*;
import com.ecommerce.demo.cart.entity.Cart;
import com.ecommerce.demo.cart.entity.CartItem;
import com.ecommerce.demo.cart.repository.CartItemRepository;
import com.ecommerce.demo.cart.repository.CartRepository;
import com.ecommerce.demo.product.entity.Product;
import com.ecommerce.demo.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    public CartServiceImpl(
            CartRepository cartRepo,
            CartItemRepository itemRepo,
            UserRepository userRepo,
            ProductRepository productRepo
    ) {
        this.cartRepo = cartRepo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(Long userId) {
        return cartRepo.findByUserId(userId)
                .map(this::toResponse)
                .orElse(new CartResponse(null, java.util.List.of(), java.math.BigDecimal.ZERO));
    }


    @Override
    public CartResponse addItem(Long userId, AddCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepo.findById(req.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.productId()));

        // If already in cart, just increase qty
        CartItem item = itemRepo.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(req.quantity())
                    .build();
            itemRepo.save(item);

            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + req.quantity());
        }

        return toResponse(getOrCreateCart(userId));
    }

    @Override
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));

        // Prevent modifying someone else's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Not allowed to modify this cart item");
        }

        item.setQuantity(req.quantity());
        return toResponse(getOrCreateCart(userId));
    }

    @Override
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Not allowed to modify this cart item");
        }

        itemRepo.delete(item);
        return toResponse(getOrCreateCart(userId));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            AppUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

            Cart cart = Cart.builder().user(user).build();
            return cartRepo.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(ci -> {
            BigDecimal unit = ci.getProduct().getPrice();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartItemResponse(
                    ci.getId(),
                    ci.getProduct().getId(),
                    ci.getProduct().getName(),
                    unit,
                    ci.getQuantity(),
                    line
            );
        }).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, total);
    }
}
