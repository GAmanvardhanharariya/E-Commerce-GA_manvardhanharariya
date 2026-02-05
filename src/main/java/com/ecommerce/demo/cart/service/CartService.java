package com.ecommerce.demo.cart.service;

import com.ecommerce.demo.cart.dto.*;

public interface CartService {
    CartResponse getMyCart(Long userId);
    CartResponse addItem(Long userId, AddCartItemRequest req);
    CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest req);
    CartResponse removeItem(Long userId, Long itemId);
}
