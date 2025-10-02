package com.paymentflow.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


/**
 * Request payload for creating a payment.
 */
public record PaymentRequest(
        @NotNull @Min(1) BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String method,          // "stripe" or "paypal" (for now)
        String idempotencyKey             // optional for Sprint 1
) {
}
