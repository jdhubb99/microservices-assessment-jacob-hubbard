package com.jdhub.orderservice.entity;

import com.jdhub.orderservice.entity.enums.OrderStatus;
import com.jdhub.orderservice.exception.InvalidStatusTransitionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @TenantId
    private UUID tenantId;
    private UUID customerId;
    private String customerEmail;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String currency;
    private BigDecimal totalAmount;
    private String cancellationReason;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    private Order(UUID customerId, String customerEmail, String currency, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.status = OrderStatus.PENDING; // status starts as PENDING
        this.currency = currency;
        this.totalAmount = totalAmount;
    }

    public static Order create(UUID customerId, String customerEmail, String currency, BigDecimal totalAmount) {
        Objects.requireNonNull(customerId, "customerId is required");
        Objects.requireNonNull(customerEmail, "customerEmail is required");
        Objects.requireNonNull(currency, "current is required");
        Objects.requireNonNull(totalAmount, "total_amount is required");

        return new Order(customerId, customerEmail, currency, totalAmount);
    }

    public void transitionTo(OrderStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new InvalidStatusTransitionException(this.id, this.status, nextStatus);
        }
        this.status = nextStatus;
    }

    public void cancel(String reason) {
        if (!this.status.isCancellable()) {
            throw new InvalidStatusTransitionException(this.id, this.status, OrderStatus.CANCELLED);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
    }
}
