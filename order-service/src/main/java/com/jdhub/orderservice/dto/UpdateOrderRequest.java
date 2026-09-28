package com.jdhub.orderservice.dto;

import com.jdhub.orderservice.entity.enums.OrderStatus;

public record UpdateOrderRequest(OrderStatus status) {
}
