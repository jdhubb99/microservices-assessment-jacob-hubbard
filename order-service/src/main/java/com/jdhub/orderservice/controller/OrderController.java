package com.jdhub.orderservice.controller;

import com.jdhub.orderservice.dto.CancelOrderRequest;
import com.jdhub.orderservice.dto.CreateOrderRequest;
import com.jdhub.orderservice.dto.OrderResponse;
import com.jdhub.orderservice.dto.UpdateOrderRequest;
import com.jdhub.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
            OrderResponse newOrder = orderService.createOrder(request);
            URI location = URI.create("/api/v1/orders/" + newOrder.id());
            return ResponseEntity.created(location).body(newOrder);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable UUID orderId, @RequestBody UpdateOrderRequest request) {
        return orderService.updateOrderStatus(orderId, request.status());
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable UUID orderId, @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request != null ? request.reason() : null;
        return orderService.cancelOrder(orderId, reason);
    }
}
