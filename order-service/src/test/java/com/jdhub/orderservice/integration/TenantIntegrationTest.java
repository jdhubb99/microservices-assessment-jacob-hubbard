package com.jdhub.orderservice.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TenantIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";
    private static final String SUSPENDED_TENANT = "33333333-3333-3333-3333-333333333333";
    public static final String CREATE_ORDER_JSON = """
            {
               "customerId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
               "customerEmail": "email@email.com",
               "currency": "USD",
               "totalAmount": 10.99
            }
            """;

    @Test
    void testTenantCanReadItsOwnOrder() throws Exception {
        String orderId = createOrder(TENANT_A);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header("X-Tenant-ID", TENANT_A))
                .andExpect(status().isOk());
    }

    @Test
    void testTenantCannotReadAnotherTenantsOrder() throws Exception {
        String orderId = createOrder(TENANT_A);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header("X-Tenant-ID", TENANT_B))
                .andExpect(status().isNotFound());
    }

    @Test
    void testTenantCanCancelItsOwnOrder() throws Exception {
        String orderId = createOrder(TENANT_B);
        mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                        .header("X-Tenant-ID", TENANT_B)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                    { "reason": "not mine" }
                                 """
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testTenantCannotCancelAnotherTenantsOrder() throws Exception {
            String orderId = createOrder(TENANT_A);

            mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                            .header("X-Tenant-ID", TENANT_B)
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content("""
                                        { "reason": "not mine" }
                                     """
                            ))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/v1/orders/{id}", orderId).header("X-Tenant-ID", TENANT_A))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testTenantCanGetItsOwnOrders() throws Exception {
        String tenantAOrderId1 = createOrder(TENANT_A);
        String tenantAOrderId2 = createOrder(TENANT_A);

        mockMvc.perform(get("/api/v1/orders")
                        .header("X-Tenant-ID", TENANT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.totalOrders").value(2));
    }

    @Test
    void testTenantCannotGetOtherTenantsOrders() throws Exception {
        String tenantAOrderId1 = createOrder(TENANT_A);
        String tenantAOrderId2 = createOrder(TENANT_A);
        String tenantBOrderId1 = createOrder(TENANT_B);

        mockMvc.perform(get("/api/v1/orders")
                        .header("X-Tenant-ID", TENANT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.orders[1].id").value(tenantAOrderId1))
                .andExpect(jsonPath("$.orders[0].id").value(tenantAOrderId2));

        mockMvc.perform(get("/api/v1/orders")
                        .header("X-Tenant-ID", TENANT_B))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1))
                .andExpect(jsonPath("$.totalOrders").value(1))
                .andExpect(jsonPath("$.orders[0].id").value(tenantBOrderId1));
    }

    @Test
    void testTenantIdCannotBeSpoofedInRequestBody() throws Exception {
        String body = """
             {
                "tenantId": "22222222-2222-2222-2222-222222222222"
                "customerId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "customerEmail": "email@email.com",
                "currency": "USD",
                "totalAmount": 10.99
             }
             """;
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-ID", TENANT_A)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(body))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testMissingTenantHeaderIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid tenant header"));
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "this-is-a-string", "54321" })
    void testIncorrectTenantHeaderIsRejected(String headerValue) throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("X-Tenant-ID", headerValue))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid tenant header"));
    }

    @Test
    void testUnknownTenantIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders").header("X-Tenant-ID", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant access denied"));
    }

    @Test
    void testSuspendedTenantIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders").header("X-Tenant-ID", SUSPENDED_TENANT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant access denied"));
    }

    @Test
    void testSuspendedTenantCannotCreateOrders() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-ID", SUSPENDED_TENANT)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant access denied"));
    }

    private String createOrder(String tenantId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.id");
    }
}
