package com.jdhub.notificationservice;

import com.jdhub.notificationservice.config.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
