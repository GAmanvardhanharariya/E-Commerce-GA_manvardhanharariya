package com.ecommerce.demo.payment.dto;

public record PaymentIntentResponse(
        String paymentIntentId,
        String clientSecret
) {}
