package com.jdhub.orderservice.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {
    private static final UUID DEFAULT_TENANT = new UUID(0L, 0L);

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        UUID tenantId = TenantContext.get() != null ? TenantContext.get() : DEFAULT_TENANT;
        log.info("Resolving current tenant identifier: {}", tenantId);
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}
