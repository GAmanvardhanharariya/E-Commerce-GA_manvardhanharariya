package com.ecommerce.demo.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Boolean active,
        CategoryMiniResponse category,
        Instant createdAt,
        Instant updatedAt
) {}
