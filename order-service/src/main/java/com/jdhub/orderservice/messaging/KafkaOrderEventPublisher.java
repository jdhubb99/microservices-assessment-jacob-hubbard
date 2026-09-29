package com.jdhub.orderservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaOrderEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final ObjectMapper objectMapper;

    public KafkaOrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                              @Value("${app.kafka.topics.order-events}") String topic,
                              ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<SendResult<String, String>> sendWithCallback(OrderEvent event) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, event.orderId().toString(), toJson(event));

        record.headers()
                .add("tenantId", event.tenantId().toString().getBytes(StandardCharsets.UTF_8))
                .add("eventId", event.eventId().toString().getBytes(StandardCharsets.UTF_8))
                .add("eventType", event.eventType().name().getBytes(StandardCharsets.UTF_8));

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event sent successfully: {} to partition {}", event.orderId(),
                        result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish {} {} for order {}",
                        event.eventType(), event.eventId(), event.orderId(), ex);
            }
        });
        return future;
    }

    private String toJson(OrderEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize %s event %s for order %s"
                            .formatted(event.eventType(), event.eventId(), event.orderId()), ex);
        }
    }
}
