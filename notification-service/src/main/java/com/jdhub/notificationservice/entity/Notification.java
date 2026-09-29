package com.jdhub.notificationservice.entity;

import com.jdhub.notificationservice.dto.OrderEvent;
import com.jdhub.notificationservice.notification.enums.NotificationChannel;
import com.jdhub.notificationservice.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @TenantId
    private UUID tenantId;
    private UUID eventId;
    private UUID orderId;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    private String recipient;
    private String message;
    private String orderStatus;
    private Instant sentAt;

    private Notification(OrderEvent event, NotificationType type, String message) {
        this.eventId = event.eventId();
        this.orderId = event.orderId();
        this.type = type;
        this.channel = NotificationChannel.EMAIL;
        this.recipient = event.customerEmail();
        this.message = message;
        this.orderStatus = event.currentStatus();
        this.sentAt = Instant.now();
    }

    public static Notification create(OrderEvent event, NotificationType type, String message) {
        return new Notification(event, type, message);
    }
}
