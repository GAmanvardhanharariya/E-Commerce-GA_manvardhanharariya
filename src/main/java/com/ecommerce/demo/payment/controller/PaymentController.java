package com.ecommerce.demo.payment.controller;

import com.ecommerce.demo.payment.dto.PaymentConfirmRequest;
import com.ecommerce.demo.payment.dto.PaymentIntentResponse;
import com.ecommerce.demo.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    private Long currentUserId(Authentication auth) {
        return Long.parseLong((String) auth.getPrincipal());
    }

    @PostMapping("/intent/{orderId}")
    public PaymentIntentResponse createIntent(Authentication auth, @PathVariable Long orderId) {
        return service.createIntent(currentUserId(auth), orderId);
    }

    @PostMapping("/confirm")
    public void confirm(Authentication auth, @Valid @RequestBody PaymentConfirmRequest req) {
        service.confirm(currentUserId(auth), req);
    }
}
