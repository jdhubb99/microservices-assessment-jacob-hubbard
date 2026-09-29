package com.jdhub.notificationservice.service;

import com.jdhub.notificationservice.dto.OrderEvent;
import com.jdhub.notificationservice.entity.Notification;
import com.jdhub.notificationservice.notification.enums.NotificationType;
import com.jdhub.notificationservice.notification.NotificationSender;
import com.jdhub.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationService {
    private static final String ORDER_CREATED = "ORDER_CREATED";
    private static final String CANCELLED = "CANCELLED";

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    public NotificationService(NotificationRepository notificationRepository, NotificationSender notificationSender) {
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
    }

    @Transactional
    public void handleOrderEvent(OrderEvent event) {
        if (notificationRepository.existsByEventId(event.eventId())) {
            log.info("Event {} already processed, skipping", event.eventId());
            return;
        }

        NotificationType type = NotificationType.forStatus(event.currentStatus());
        String message = buildMessage(type, event);

        notificationSender.send(event.customerEmail(), message);
        notificationRepository.save(Notification.create(event, type, message));
    }

    // builds the notification message based on the NotificationType and OrderEvent's
    private String buildMessage(NotificationType notificationType, OrderEvent orderEvent) {
        return switch (notificationType) {
            case ORDER_RECEIPT -> ORDER_CREATED.equals(orderEvent.eventType())
                    ? "We've received your order %s for %s %s.".formatted(orderEvent.orderId(), orderEvent.totalAmount(), orderEvent.currency())
                    : "Your order %s is now %s.".formatted(orderEvent.orderId(), orderEvent.currentStatus().toLowerCase());
            case COMPLETION_ACKNOWLEDGEMENT -> CANCELLED.equals(orderEvent.currentStatus())
                    ? "Your order %s has been cancelled.".formatted(orderEvent.orderId())
                    : "Your order %s has been delivered. Thanks for your purchase!".formatted(orderEvent.orderId());
        };
    }
}