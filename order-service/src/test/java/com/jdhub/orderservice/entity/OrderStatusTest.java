package com.jdhub.orderservice.entity;

import com.jdhub.orderservice.entity.enums.OrderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest
    @CsvSource({
            "PENDING,CONFIRMED,true",
            "PENDING,CANCELLED,true",
            "CONFIRMED,PROCESSING,true",
            "PROCESSING,CANCELLED,true",
            "SHIPPED,DELIVERED,true",
            "PENDING,SHIPPED,false",
            "SHIPPED,CANCELLED,false",
            "DELIVERED,CANCELLED,false",
            "CANCELLED,PENDING,false"
    })
    void testTransitionRules(OrderStatus from, OrderStatus to, boolean ruleAllowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(ruleAllowed);
    }
}
