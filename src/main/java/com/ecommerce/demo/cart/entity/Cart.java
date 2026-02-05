package com.ecommerce.demo.cart.entity;

import com.ecommerce.demo.auth.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_user", columnNames = "user_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // one cart per user
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_user"))
    private AppUser user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
