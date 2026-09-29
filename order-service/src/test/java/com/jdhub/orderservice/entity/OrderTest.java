package com.jdhub.orderservice.entity;

import com.jdhub.orderservice.entity.enums.OrderStatus;
import com.jdhub.orderservice.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

    @Test
    void testNewOrderStatusIsPending() {
        assertThat(newOrder().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testCannotCancelShippedOrder() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.CONFIRMED);
        order.transitionTo(OrderStatus.PROCESSING);
        order.transitionTo(OrderStatus.SHIPPED);
        assertThatThrownBy(() -> order.cancel("Cannot cancel"))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void testCanCancelOrderBeforeShipment() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.CONFIRMED);

        order.cancel("Random reason");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo("Random reason");
    }

    @Test
    void testInvalidStatusTransitionThrows() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.SHIPPED))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    private Order newOrder() {
        return Order.create(UUID.randomUUID(), "test@test.com", "USD", new BigDecimal("100.99"));
    }
}
