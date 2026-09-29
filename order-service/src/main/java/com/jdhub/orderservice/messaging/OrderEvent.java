package com.jdhub.orderservice.messaging;

import com.jdhub.orderservice.entity.Order;
import com.jdhub.orderservice.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderEvent(
        UUID eventId,
        OrderEventType eventType,
        Instant occurredAt,
        UUID tenantId,
        UUID orderId,
        UUID customerId,
        String customerEmail,
        OrderStatus previousStatus,
        OrderStatus currentStatus,
        BigDecimal totalAmount,
        String currency,
        Long orderVersion
) {
    public static OrderEvent created(Order order) {
        return createOrderEventWith(OrderEventType.ORDER_CREATED, order, null);
    }

    public static OrderEvent statusChanged(Order order, OrderStatus previousStatus) {
        return createOrderEventWith(OrderEventType.ORDER_STATUS_CHANGED, order, previousStatus);
    }

    private static OrderEvent createOrderEventWith(OrderEventType type, Order order, OrderStatus previousStatus) {
        return new OrderEvent(
                UUID.randomUUID(),
                type,
                Instant.now(),
                order.getTenantId(),
                order.getId(),
                order.getCustomerId(),
                order.getCustomerEmail(),
                previousStatus,
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getVersion()
        );
    }
}
