package com.jdhub.notificationservice.integration;

import com.jdhub.notificationservice.config.TestcontainersConfiguration;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Testcontainers
abstract class AbstractIntegrationTest {

    @Resource
    protected KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    protected ObjectMapper objectMapper;
    @Resource
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE notifications");
    }
}
