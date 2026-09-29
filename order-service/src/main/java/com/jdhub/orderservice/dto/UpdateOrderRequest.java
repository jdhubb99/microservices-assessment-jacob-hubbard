package com.jdhub.orderservice.dto;

import com.jdhub.orderservice.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderRequest(@NotNull OrderStatus status) {
}
