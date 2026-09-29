package com.jdhub.orderservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(@NotNull UUID customerId, @NotBlank @Email @Size(max = 320) String customerEmail, @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "must be a 3-letter code; ex. USD") String currency, @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal totalAmount) {
}