package com.ecommerce.demo.order.dto;

import com.ecommerce.demo.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal grandTotal,
        Instant createdAt
) {}
