package com.jdhub.orderservice.service;

import com.jdhub.orderservice.dto.CreateOrderRequest;
import com.jdhub.orderservice.dto.OrderResponse;
import com.jdhub.orderservice.entity.Order;
import com.jdhub.orderservice.entity.enums.OrderStatus;
import com.jdhub.orderservice.exception.OrderNotFoundException;
import com.jdhub.orderservice.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
       this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest payload) {
        Order newOrder = Order.create(payload.customerId(), payload.customerEmail(), payload.currency(), payload.totalAmount());
        orderRepository.saveAndFlush(newOrder);
        // TODO: add event publishing

        return OrderResponse.from(newOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order existingOrder = findOrderOrThrow(orderId);
        return OrderResponse.from(existingOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order existingOrder = findOrderOrThrow(orderId);
        existingOrder.transitionTo(newStatus);
        orderRepository.saveAndFlush(existingOrder);

        // TODO: add event publishing
        return OrderResponse.from(existingOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String cancellationReason) {
        Order existingOrder = findOrderOrThrow(orderId);
        existingOrder.cancel(cancellationReason);
        orderRepository.saveAndFlush(existingOrder);
        // TODO: add event publishing

        return OrderResponse.from(existingOrder);
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
