package com.jdhub.orderservice.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TenantIntegrationTest extends AbstractIntegrationTest {

    static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";
    private static final String SUSPENDED_TENANT = "33333333-3333-3333-3333-333333333333";
    private static final String CREATE_ORDER_JSON = """
            {
               "customerId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
               "customerEmail": "email@email.com",
               "currency": "USD",
               "totalAmount": 10.99
            }
            """;
    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Test
    void testTenantCanReadItsOwnOrder() throws Exception {
        String orderId = createOrder(TENANT_A);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header(TENANT_HEADER, TENANT_A))
                .andExpect(status().isOk());
    }

    @Test
    void testTenantCannotReadAnotherTenantsOrder() throws Exception {
        String orderId = createOrder(TENANT_A);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header(TENANT_HEADER, TENANT_B))
                .andExpect(status().isNotFound());
    }

    @Test
    void testTenantCannotUpdateAnotherTenantOrder() throws Exception{
        String tenantAOrderId = createOrder(TENANT_A);
        String tenantBOrderId = createOrder(TENANT_B);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", tenantAOrderId)
                        .header(TENANT_HEADER, TENANT_B)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                    { "status": "CONFIRMED" }
                                 """
                        ))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/orders/{id}/status", tenantBOrderId)
                        .header(TENANT_HEADER, TENANT_B)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                    { "status": "CONFIRMED" }
                                 """
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void testTenantCanCancelItsOwnOrder() throws Exception {
        String orderId = createOrder(TENANT_B);
        mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                        .header(TENANT_HEADER, TENANT_B)
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
                            .header(TENANT_HEADER, TENANT_B)
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content("""
                                        { "reason": "not mine" }
                                     """
                            ))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/v1/orders/{id}", orderId).header(TENANT_HEADER, TENANT_A))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testTenantCanGetItsOwnOrders() throws Exception {
        String tenantAOrderId1 = createOrder(TENANT_A);
        String tenantAOrderId2 = createOrder(TENANT_A);

        mockMvc.perform(get("/api/v1/orders")
                        .header(TENANT_HEADER, TENANT_A))
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
                        .header(TENANT_HEADER, TENANT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.orders[1].id").value(tenantAOrderId1))
                .andExpect(jsonPath("$.orders[0].id").value(tenantAOrderId2));

        mockMvc.perform(get("/api/v1/orders")
                        .header(TENANT_HEADER, TENANT_B))
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
                        .header(TENANT_HEADER, TENANT_A)
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
                        .header(TENANT_HEADER, headerValue))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid tenant header"));
    }

    @Test
    void testUnknownTenantIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders").header(TENANT_HEADER, UUID.randomUUID().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant access denied"));
    }

    @Test
    void testSuspendedTenantIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders").header(TENANT_HEADER, SUSPENDED_TENANT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant access denied"));
    }

    @Test
    void testSuspendedTenantCannotCreateOrders() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header(TENANT_HEADER, SUSPENDED_TENANT)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant access denied"));
    }

    @Test
    void testStatusEndpointRejectsCancellation() throws Exception {
        String orderId = createOrder(TENANT_A);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                        .header("X-Tenant-ID", TENANT_A)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                    { "status": "CANCELLED" }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/orders/{id}", orderId).header("X-Tenant-ID", TENANT_A))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    private String createOrder(String tenantId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/orders")
                        .header(TENANT_HEADER, tenantId)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.id");
    }
}
