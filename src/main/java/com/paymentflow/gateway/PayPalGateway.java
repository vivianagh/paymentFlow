package com.paymentflow.gateway;

import com.paymentflow.model.Payment;
import com.paymentflow.model.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component("payPalGateway")
public class PayPalGateway implements PaymentGateway {

    private static void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(minMs, maxMs + 1));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean randomFail(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    @Override
    public PaymentResult charge(Payment payment) throws GatewayException {
        simulateLatency(80, 250);
        if (randomFail(0.15)) throw new GatewayException("PayPal network glitch");
        return PaymentResult.success(payment.id());
    }
}
