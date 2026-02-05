package com.ecommerce.demo.order.service;

import com.ecommerce.demo.order.dto.*;

import java.util.List;

public interface OrderService {
    OrderResponse checkout(Long userId, CheckoutRequest req);
    List<OrderSummaryResponse> listMyOrders(Long userId);
    OrderResponse getMyOrder(Long userId, Long orderId);
}
