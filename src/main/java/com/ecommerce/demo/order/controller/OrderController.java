package com.ecommerce.demo.order.controller;

import com.ecommerce.demo.order.dto.*;
import com.ecommerce.demo.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    private Long currentUserId(Authentication auth) {
        return Long.parseLong((String) auth.getPrincipal());
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(Authentication auth, @Valid @RequestBody CheckoutRequest req) {
        return service.checkout(currentUserId(auth), req);
    }

    @GetMapping
    public List<OrderSummaryResponse> list(Authentication auth) {
        return service.listMyOrders(currentUserId(auth));
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(Authentication auth, @PathVariable Long orderId) {
        return service.getMyOrder(currentUserId(auth), orderId);
    }
}
