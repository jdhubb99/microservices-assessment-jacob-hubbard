package com.jdhub.orderservice.integration;

import com.jdhub.orderservice.config.TestcontainersConfiguration;
import com.jdhub.orderservice.messaging.OrderEventPublisher;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.reset;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.kafka.admin.auto-create=false")
@AutoConfigureMockMvc
@Testcontainers
abstract class AbstractIntegrationTest {
    @MockitoBean
    OrderEventPublisher orderEventPublisher;

    @Resource
    MockMvc mockMvc;

    @Resource
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetState() {
        // resets DB without hibernate since tenant context might not be set
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        reset(orderEventPublisher);
    }
}
