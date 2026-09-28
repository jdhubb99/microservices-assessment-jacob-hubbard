package com.jdhub.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(UUID customerId, String customerEmail, String currency, BigDecimal totalAmount) {
}