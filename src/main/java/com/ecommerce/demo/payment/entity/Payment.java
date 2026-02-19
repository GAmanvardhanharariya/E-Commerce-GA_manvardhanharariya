package com.ecommerce.demo.payment.entity;

import com.ecommerce.demo.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_pi", columnList = "stripePaymentIntentId", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String stripePaymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false)
    private Long amount; // in paise (smallest unit)

    @Column(nullable = false, length = 10)
    private String currency;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_payment_order"))
    private Order order;
}
