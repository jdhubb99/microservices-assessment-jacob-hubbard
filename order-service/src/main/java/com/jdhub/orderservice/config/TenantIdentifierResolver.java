package com.jdhub.orderservice.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {
    private static final UUID DEFAULT_TENANT = new UUID(0L, 0L);
    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        UUID tenantId = CONTEXT.get() != null ? CONTEXT.get() : DEFAULT_TENANT;
        log.info("Resolving current tenant identifier: {}", tenantId);
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    public static void setTenantId(UUID tenantId) {
        CONTEXT.set(tenantId);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
