package com.paymentflow.service;

import com.paymentflow.model.PaymentRequest;
import com.paymentflow.model.PaymentResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentOrchestrator {

    private final PaymentService learningPaymentService;
    private final PaymentService productionPaymentService;
    private final String paymentMode;

    public PaymentOrchestrator(
            @Qualifier("learningPaymentService")PaymentService learningPaymentService,
            @Qualifier("productionPaymentService")PaymentService productionPaymentService,
            @Value("${payment.model:learning}")String paymentMode
    ) {
        this.learningPaymentService = learningPaymentService;
        this.productionPaymentService = productionPaymentService;
        this.paymentMode = paymentMode;
    }


    public PaymentResult create(PaymentRequest paymentRequest) {
        return isProduction() ? productionPaymentService.createPayment(paymentRequest)
                : learningPaymentService.createPayment(paymentRequest);
    }

    public PaymentResult status(UUID id) {
        return isProduction() ? productionPaymentService.getStatus(id)
                : learningPaymentService.getStatus(id);
    }

    private boolean isProduction() {
        return "production".equalsIgnoreCase(paymentMode);
    }
}
