package com.jdhub.orderservice.config;

import com.jdhub.orderservice.entity.Tenant;
import com.jdhub.orderservice.exception.InvalidTenantHeaderException;
import com.jdhub.orderservice.exception.TenantAccessDeniedException;
import com.jdhub.orderservice.exception.TenantException;
import com.jdhub.orderservice.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;
    private final HandlerExceptionResolver exceptionResolver;

    public TenantFilter(TenantRepository tenantRepository, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.tenantRepository = tenantRepository;
        this.exceptionResolver = exceptionResolver;
    }

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        UUID tenantId;
        try {
            tenantId = resolveTenantHeader(request);
        } catch (TenantException ex) {
            exceptionResolver.resolveException(request, response, null, ex);
            return;
        }

        try {
            TenantContext.set(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private UUID resolveTenantHeader(HttpServletRequest request) {
        UUID tenantId = parseTenantId(request.getHeader(TENANT_HEADER)).orElseThrow(
                () -> new InvalidTenantHeaderException("Missing or invalid tenant identifier"));

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            log.info("Rejected request for unknown tenant {}", tenantId);
            return new TenantAccessDeniedException("Tenant is not permitted to access this resource");
        });

        if (!tenant.isActive()) {
            log.info("Rejected request for inactive tenant {} with status {}", tenantId, tenant.getStatus());
            throw new TenantAccessDeniedException("Tenant is not permitted to access this resource");
        }
        return tenantId;
    }

    private Optional<UUID> parseTenantId(String header) {
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(header.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
