package com.jdhub.notificationservice.service;

import com.jdhub.notificationservice.dto.OrderEvent;
import com.jdhub.notificationservice.entity.Notification;
import com.jdhub.notificationservice.notification.NotificationSender;
import com.jdhub.notificationservice.notification.enums.NotificationType;
import com.jdhub.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;
    @Mock
    NotificationSender notificationSender;
    @InjectMocks
    NotificationService notificationService;

    @Test
    void testDuplicateEventIsSkipped() {
        OrderEvent event = newEvent("ORDER_STATUS_CHANGED", "SHIPPED");
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(true);

        notificationService.handleOrderEvent(event);

        verifyNoInteractions(notificationSender);
        verify(notificationRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONFIRMED", "PROCESSING", "SHIPPED"})
    void testStatusUpdateSendsAndRecordReceipt(String currentStatus) {
        OrderEvent event = newEvent("ORDER_STATUS_CHANGED", currentStatus);
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(false);

        notificationService.handleOrderEvent(event);

        verify(notificationSender).send(eq("bigJim@gmail.com"), contains(currentStatus.toLowerCase()));
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.ORDER_RECEIPT);
        assertThat(saved.getValue().getEventId()).isEqualTo(event.eventId());
    }

    @Test
    void testOrderCreatedSendsReceivedReceipt() {
        OrderEvent event = newEvent("ORDER_CREATED", "PENDING");
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(false);

        notificationService.handleOrderEvent(event);

        verify(notificationSender).send(eq("bigJim@gmail.com"), contains("received"));
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.ORDER_RECEIPT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELIVERED", "CANCELLED"})
    void testTerminalStatusSendsCompletionAcknowledgement(String currentStatus) {
        OrderEvent event = newEvent("ORDER_STATUS_CHANGED", currentStatus);
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(false);

        notificationService.handleOrderEvent(event);

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.COMPLETION_ACKNOWLEDGEMENT);
    }

    private OrderEvent newEvent(String eventType, String currentStatus) {
        return new OrderEvent(
                UUID.randomUUID(), eventType, Instant.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "bigJim@gmail.com", null, currentStatus,
                new BigDecimal("999.99"), "USD", 1L);
    }
}
