package com.jdhub.notificationservice.notification.enums;

import java.util.Set;

public enum NotificationType {
    ORDER_RECEIPT,
    COMPLETION_ACKNOWLEDGEMENT;

    private static final Set<String> TERMINAL_STATUSES = Set.of("DELIVERED", "CANCELLED");

    public static NotificationType forStatus(String currentStatus) {
        return TERMINAL_STATUSES.contains(currentStatus) ? COMPLETION_ACKNOWLEDGEMENT : ORDER_RECEIPT;
    }
}
