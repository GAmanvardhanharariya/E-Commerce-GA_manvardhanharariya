package com.ecommerce.demo.order.service;

import com.ecommerce.demo.cart.entity.Cart;
import com.ecommerce.demo.cart.entity.CartItem;
import com.ecommerce.demo.cart.repository.CartRepository;
import com.ecommerce.demo.order.dto.*;
import com.ecommerce.demo.order.entity.Order;
import com.ecommerce.demo.order.entity.OrderItem;
import com.ecommerce.demo.order.entity.OrderStatus;
import com.ecommerce.demo.order.repository.OrderRepository;
import com.ecommerce.demo.product.entity.Product;
import com.ecommerce.demo.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public OrderServiceImpl(CartRepository cartRepo, ProductRepository productRepo, OrderRepository orderRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    @Override
    public OrderResponse checkout(Long userId, CheckoutRequest req) {

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // lock products
        List<Long> productIds = cart.getItems().stream()
                .map(ci -> ci.getProduct().getId())
                .distinct()
                .toList();

        List<Product> lockedProducts = productRepo.findAllByIdInForUpdate(productIds);

        // Build quick lookup
        java.util.Map<Long, Product> productById = lockedProducts.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        // validate + compute totals
        BigDecimal grandTotal = BigDecimal.ZERO;

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(cart.getUser())
                .status(OrderStatus.PENDING)
                .shippingName(req.shippingName())
                .shippingPhone(req.shippingPhone())
                .shippingAddressLine(req.addressLine())
                .shippingCity(req.city())
                .shippingPincode(req.pincode())
                .grandTotal(BigDecimal.ZERO) // set later
                .build();

        for (CartItem ci : cart.getItems()) {
            Product p = productById.get(ci.getProduct().getId());
            if (p == null) throw new EntityNotFoundException("Product not found: " + ci.getProduct().getId());

            int qty = ci.getQuantity();
            if (qty <= 0) throw new IllegalArgumentException("Invalid quantity for product " + p.getId());

            if (p.getStock() < qty) {
                throw new IllegalArgumentException("Insufficient stock for: " + p.getName());
            }

            BigDecimal unit = p.getPrice(); // snapshot current price
            BigDecimal line = unit.multiply(BigDecimal.valueOf(qty));
            grandTotal = grandTotal.add(line);

            // decrement stock
            p.setStock(p.getStock() - qty);

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productNameSnapshot(p.getName())
                    .unitPriceSnapshot(unit)
                    .quantity(qty)
                    .lineTotal(line)
                    .build();

            order.getItems().add(oi);
        }

        order.setGrandTotal(grandTotal);

        // Save order (cascade saves items)
        Order saved = orderRepo.save(order);

        // Clear cart (orphanRemoval will delete items)
        cart.getItems().clear();

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> listMyOrders(Long userId) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getGrandTotal(), o.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        Order o = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        return toResponse(o);
    }

    private String generateOrderNumber() {
        // v1: good enough, unique, sortable-ish
        return "ORD-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private OrderResponse toResponse(Order o) {
        var items = o.getItems().stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getProductNameSnapshot(),
                        oi.getUnitPriceSnapshot(),
                        oi.getQuantity(),
                        oi.getLineTotal()
                )).toList();

        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getStatus(),
                items,
                o.getGrandTotal(),
                o.getCreatedAt()
        );
    }
}
