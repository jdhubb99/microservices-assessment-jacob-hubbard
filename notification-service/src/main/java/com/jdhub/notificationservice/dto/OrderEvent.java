package com.jdhub.notificationservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID tenantId,
        UUID orderId,
        UUID customerId,
        String customerEmail,
        String previousStatus,
        String currentStatus,
        BigDecimal totalAmount,
        String currency,
        Long orderVersion
) {
}
