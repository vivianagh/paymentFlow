package com.paymentflow.model;

import java.util.UUID;

/**
 * API response for a payment operation.
 */
public record PaymentResult(
        UUID paymentId,
        PaymentStatus status,
        String message
) {
    public static PaymentResult accepted(UUID id) {
        return new PaymentResult(id, PaymentStatus.PROCESSING, "Payment accepted, processing");
    }

    public static PaymentResult success(UUID id) {
        return new PaymentResult(id, PaymentStatus.SUCCESS, "Payment successful");
    }

    public static PaymentResult failed(UUID id, String reason) {
        return new PaymentResult(id, PaymentStatus.FAILED, reason);
    }
}