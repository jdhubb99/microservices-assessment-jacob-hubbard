package com.jdhub.orderservice.exception;

public class InvalidTenantHeaderException extends TenantException {
    public InvalidTenantHeaderException(String message) {
        super(message);
    }
}
