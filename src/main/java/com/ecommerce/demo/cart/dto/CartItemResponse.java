package com.ecommerce.demo.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long itemId,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {}
