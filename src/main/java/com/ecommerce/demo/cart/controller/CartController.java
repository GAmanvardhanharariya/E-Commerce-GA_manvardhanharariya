package com.ecommerce.demo.cart.controller;

import com.ecommerce.demo.cart.dto.*;
import com.ecommerce.demo.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    private Long currentUserId(Authentication auth) {
        return Long.parseLong((String) auth.getPrincipal());
    }

    @GetMapping
    public CartResponse getMyCart(Authentication auth) {
        return service.getMyCart(currentUserId(auth));
    }

    @PostMapping("/items")
    public CartResponse addItem(Authentication auth, @Valid @RequestBody AddCartItemRequest req) {
        return service.addItem(currentUserId(auth), req);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(Authentication auth, @PathVariable Long itemId,
                                   @Valid @RequestBody UpdateCartItemRequest req) {
        return service.updateItem(currentUserId(auth), itemId, req);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(Authentication auth, @PathVariable Long itemId) {
        return service.removeItem(currentUserId(auth), itemId);
    }
}
