package com.jdhub.orderservice.exception;

import com.jdhub.orderservice.entity.enums.OrderStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InvalidStatusTransitionException extends RuntimeException {

    private final UUID orderId;
    private final OrderStatus from;
    private final OrderStatus to;

    public InvalidStatusTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super("Cannot transition order %s from %s to %s".formatted(orderId, from, to));
        this.orderId = orderId;
        this.from = from;
        this.to = to;
    }
}
