package com.jdhub.orderservice.exception;

public class TenantAccessDeniedException extends TenantException {
    public TenantAccessDeniedException(String message) {
        super(message);
    }
}
