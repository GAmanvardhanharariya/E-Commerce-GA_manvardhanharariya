package com.ecommerce.demo.order.dto;

import com.ecommerce.demo.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal grandTotal,
        Instant createdAt
) {}
