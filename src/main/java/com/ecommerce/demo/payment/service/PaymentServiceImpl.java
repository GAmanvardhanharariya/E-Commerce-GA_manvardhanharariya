package com.ecommerce.demo.payment.service;

import com.ecommerce.demo.order.entity.Order;
import com.ecommerce.demo.order.entity.OrderStatus;
import com.ecommerce.demo.order.repository.OrderRepository;
import com.ecommerce.demo.payment.config.StripeConfig;
import com.ecommerce.demo.payment.dto.PaymentConfirmRequest;
import com.ecommerce.demo.payment.dto.PaymentIntentResponse;
import com.ecommerce.demo.payment.entity.Payment;
import com.ecommerce.demo.payment.entity.PaymentStatus;
import com.ecommerce.demo.payment.repository.PaymentRepository;
import com.ecommerce.demo.product.entity.Product;
import com.ecommerce.demo.product.repository.ProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final StripeConfig stripeConfig;
    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final ProductRepository productRepo;

    public PaymentServiceImpl(
            StripeConfig stripeConfig,
            OrderRepository orderRepo,
            PaymentRepository paymentRepo,
            ProductRepository productRepo
    ) {
        this.stripeConfig = stripeConfig;
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.productRepo = productRepo;
    }

    private RequestOptions stripeOptions() {
        return RequestOptions.builder()
                .setApiKey(stripeConfig.getSecretKey())
                .build();
    }

    @Override
    public PaymentIntentResponse createIntent(Long userId, Long orderId) {

        Order order = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not pending payment");
        }

        // Prevent duplicate intents for same order
        paymentRepo.findByOrderId(orderId).ifPresent(p -> {
            throw new IllegalStateException("Payment already initiated for this order");
        });

        long amountPaise = order.getGrandTotal().multiply(java.math.BigDecimal.valueOf(100)).longValueExact();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountPaise)
                    .setCurrency("inr")
                    // metadata helps reconciliation even without webhooks
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .putMetadata("userId", String.valueOf(userId))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params, stripeOptions());

            Payment payment = Payment.builder()
                    .stripePaymentIntentId(intent.getId())
                    .status(PaymentStatus.CREATED)
                    .amount(amountPaise)
                    .currency("inr")
                    .order(order)
                    .build();

            paymentRepo.save(payment);

            return new PaymentIntentResponse(intent.getId(), intent.getClientSecret());

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error creating payment intent", e);
        }
    }

    @Override
    public void confirm(Long userId, PaymentConfirmRequest req) {

        Order order = orderRepo.findByIdAndUserId(req.orderId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + req.orderId()));

        Payment payment = paymentRepo.findByStripePaymentIntentId(req.paymentIntentId())
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for intent: " + req.paymentIntentId()));

        // Ensure this intent belongs to this order
        if (!payment.getOrder().getId().equals(order.getId())) {
            throw new IllegalArgumentException("PaymentIntent does not belong to this order");
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return; // idempotent
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(req.paymentIntentId(), stripeOptions());

            // strongest check without webhook
            if (!"succeeded".equals(intent.getStatus())) {
                payment.setStatus(PaymentStatus.FAILED);
                throw new IllegalStateException("Payment not successful. Status=" + intent.getStatus());
            }

            // optional: verify amount matches
            if (intent.getAmount() == null || intent.getAmount() != payment.getAmount()) {
                throw new IllegalStateException("Amount mismatch for payment intent");
            }

            // Reduce stock NOW (payment succeeded)
            List<Long> productIds = order.getItems().stream()
                    .map(oi -> oi.getProduct().getId())
                    .distinct()
                    .toList();

            // Lock products to avoid race when multiple payments complete concurrently
            List<Product> locked = productRepo.findAllByIdInForUpdate(productIds);
            Map<Long, Product> byId = locked.stream().collect(Collectors.toMap(Product::getId, p -> p));

            for (var oi : order.getItems()) {
                Product p = byId.get(oi.getProduct().getId());
                if (p == null) throw new EntityNotFoundException("Product missing: " + oi.getProduct().getId());

                int qty = oi.getQuantity();
                if (p.getStock() < qty) {
                    throw new IllegalStateException("Insufficient stock during payment confirm for: " + p.getName());
                }
                p.setStock(p.getStock() - qty);
            }

            payment.setStatus(PaymentStatus.SUCCEEDED);
            order.setStatus(OrderStatus.CONFIRMED);

            // Transaction commit will flush product stock + order + payment
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error confirming payment", e);
        }
    }
}
