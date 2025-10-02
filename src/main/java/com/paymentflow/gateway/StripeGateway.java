package com.paymentflow.gateway;

import com.paymentflow.model.Payment;
import com.paymentflow.model.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component("stripeGateway")
public class StripeGateway implements PaymentGateway {

    private static void simulateLatency(int minsMs, int maxMS) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(minsMs, maxMS + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean randomFail(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    @Override
    public PaymentResult charge(Payment payment) throws GatewayException {
        simulateLatency(50, 200);
        if (randomFail(0.25)) throw new GatewayException("Stripe temporary failure");
        return PaymentResult.success(payment.id());

    }
}
