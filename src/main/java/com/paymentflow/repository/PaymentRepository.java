package com.paymentflow.repository;

import com.paymentflow.model.Payment;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository abstraction (in-memory for Sprint 1).
 */
public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);
}
