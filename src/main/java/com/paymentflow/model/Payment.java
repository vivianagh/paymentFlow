package com.paymentflow.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

//inmutable domain entity for a payment
public record Payment(
        UUID id,
        BigDecimal amount,
        String currency,
        String method,
        PaymentStatus status,
        Instant createdAt,
        String failureReason
) {
    public Payment withStatus(PaymentStatus newStatus) {
        return new Payment(id, amount, currency, method, newStatus, createdAt, failureReason);
    }

    public Payment withFailure(String reason) {
        return new Payment(id, amount, currency, method, PaymentStatus.FAILED, createdAt, reason);
    }

    public Payment succeed() {
        return new Payment(id, amount, currency, method, PaymentStatus.SUCCESS, createdAt, null);
    }

}
