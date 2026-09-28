package com.jdhub.orderservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Optional<UUID> tenantId = parseTenantId(request.getHeader(TENANT_HEADER));
        if (tenantId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid tenant identifier");
            return;
        }

        try {
            TenantIdentifierResolver.setTenantId(tenantId.get());
            filterChain.doFilter(request, response);
        } finally {
            TenantIdentifierResolver.clear();
        }
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
