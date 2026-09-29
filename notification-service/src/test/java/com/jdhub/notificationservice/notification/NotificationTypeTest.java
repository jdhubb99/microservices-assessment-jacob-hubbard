package com.jdhub.notificationservice.notification;

import com.jdhub.notificationservice.notification.enums.NotificationType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTypeTest {

    @ParameterizedTest
    @CsvSource({
            "PENDING,ORDER_RECEIPT",
            "CONFIRMED,ORDER_RECEIPT",
            "PROCESSING,ORDER_RECEIPT",
            "SHIPPED,ORDER_RECEIPT",
            "DELIVERED,COMPLETION_ACKNOWLEDGEMENT",
            "CANCELLED,COMPLETION_ACKNOWLEDGEMENT"
    })
    void mapsOrderStatusToNotificationType(String status, NotificationType expected) {
        assertThat(NotificationType.forStatus(status)).isEqualTo(expected);
    }
}