package com.jdhub.orderservice.exception;

public class TenantException extends RuntimeException {
    public TenantException(String message) {
        super(message);
    }
}
