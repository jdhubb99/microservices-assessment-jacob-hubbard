package com.jdhub.notificationservice.notification;

import com.jdhub.notificationservice.notification.enums.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingNotificationSender implements NotificationSender {
    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(String recipient, String message) {
        log.info("Simulated {} to {}: {}", channel(), recipient, message);
    }
}
