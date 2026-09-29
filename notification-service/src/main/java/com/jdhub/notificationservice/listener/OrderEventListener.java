package com.jdhub.notificationservice.listener;

import com.jdhub.notificationservice.config.TenantContext;
import com.jdhub.notificationservice.dto.OrderEvent;
import com.jdhub.notificationservice.exception.InvalidEventException;
import com.jdhub.notificationservice.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class OrderEventListener {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events}")
    public void processOrderEvent(ConsumerRecord<String, String> record) {
        UUID tenantId = requireUuidHeader(record, "tenantId");

        TenantContext.set(tenantId);
        try {
            OrderEvent event = parse(record.value());
            if (!tenantId.equals(event.tenantId())) {
                throw new InvalidEventException("Tenant header does not match event payload");
            }
            notificationService.handleOrderEvent(event);
        } finally {
            TenantContext.clear();
        }
    }

    private UUID requireUuidHeader(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        if (header == null) {
            throw new InvalidEventException("Missing required header: " + name);
        }
        try {
            return UUID.fromString(new String(header.value(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ex) {
            throw new InvalidEventException("Malformed header: " + name, ex);
        }
    }

    private OrderEvent parse(String json) {
        try {
            return objectMapper.readValue(json, OrderEvent.class);
        } catch (JacksonException ex) {
            throw new InvalidEventException("Unparseable event payload", ex);
        }
    }
}
