package com.ecommerce.demo.payment.service;

import com.ecommerce.demo.payment.dto.PaymentConfirmRequest;
import com.ecommerce.demo.payment.dto.PaymentIntentResponse;

public interface PaymentService {
    PaymentIntentResponse createIntent(Long userId, Long orderId);
    void confirm(Long userId, PaymentConfirmRequest req);
}
