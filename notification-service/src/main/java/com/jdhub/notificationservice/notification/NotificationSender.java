package com.jdhub.notificationservice.notification;

import com.jdhub.notificationservice.notification.enums.NotificationChannel;

public interface NotificationSender {
    NotificationChannel channel();
    void send(String recipient, String message);
}
