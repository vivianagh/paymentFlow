package com.paymentflow.service;

import com.paymentflow.model.PaymentRequest;
import com.paymentflow.model.PaymentResult;

import java.util.UUID;

public interface PaymentService {
    PaymentResult createPayment(PaymentRequest request);

    PaymentResult getStatus(UUID paymentId);
}
