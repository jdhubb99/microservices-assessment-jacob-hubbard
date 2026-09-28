package com.jdhub.orderservice.dto;

import com.jdhub.orderservice.entity.Order;
import com.jdhub.orderservice.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String customerEmail,
        OrderStatus status,
        String currency,
        BigDecimal totalAmount,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getCancellationReason(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
