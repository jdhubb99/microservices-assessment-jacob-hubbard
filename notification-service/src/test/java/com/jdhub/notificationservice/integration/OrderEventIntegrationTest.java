package com.jdhub.notificationservice.integration;

import com.jdhub.notificationservice.dto.OrderEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class OrderEventIntegrationTest extends AbstractIntegrationTest {

    private static final UUID TENANT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Value("${app.kafka.topics.order-events}")
    String topic;

    @Test
    void testRedeliveredEventIsRecordedOnce() {
        UUID orderId = UUID.randomUUID();
        OrderEvent event = anOrderEvent(orderId, "SHIPPED");
        OrderEvent marker = anOrderEvent(orderId, "DELIVERED");

        send(event);
        send(event);
        send(marker);

        await().atMost(Duration.ofSeconds(15)).until(() -> {
            int count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE event_id = ?", Integer.class, marker.eventId());
            return count == 1;
        });
        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE event_id = ?", Integer.class, event.eventId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testEventIsRecordedUnderItsTenant() {
        OrderEvent event = anOrderEvent(UUID.randomUUID(), "SHIPPED");

        send(event);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<UUID> storedTenant = jdbcTemplate.queryForList(
                    "SELECT tenant_id FROM notifications WHERE event_id = ?", UUID.class, event.eventId());
            assertThat(storedTenant).containsExactly(TENANT);
        });
    }

    private void send(OrderEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic, event.orderId().toString(), objectMapper.writeValueAsString(event));
        record.headers().add("tenantId", event.tenantId().toString().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record).join();
    }

    private OrderEvent anOrderEvent(UUID orderId, String status) {
        return new OrderEvent(
                UUID.randomUUID(), "ORDER_STATUS_CHANGED", Instant.now(),
                TENANT, orderId, UUID.randomUUID(),
                "johnSmith@email.com", "PROCESSING", status,
                new BigDecimal("49.99"), "USD", 2L);
    }
}
