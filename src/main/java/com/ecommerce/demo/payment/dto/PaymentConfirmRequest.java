package com.ecommerce.demo.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
        @NotNull Long orderId,
        @NotBlank String paymentIntentId
) {}
