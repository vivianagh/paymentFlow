package com.paymentflow.gateway;

import com.paymentflow.model.Payment;
import com.paymentflow.model.PaymentResult;

public interface PaymentGateway {
    PaymentResult charge(Payment payment) throws GatewayException;

    class GatewayException extends Exception {
        public GatewayException(String message) {
            super(message);
        }
    }
}
