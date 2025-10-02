package com.paymentflow.service.learning;

import com.paymentflow.gateway.PaymentGateway;
import com.paymentflow.model.Payment;
import com.paymentflow.model.PaymentRequest;
import com.paymentflow.model.PaymentResult;
import com.paymentflow.model.PaymentStatus;
import com.paymentflow.repository.PaymentRepository;
import com.paymentflow.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Learning mode: accept fast (202) and process asynchronously in a manual thread pool.
 */
@Service("learningPaymentService")
public class LearningPaymentService implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentGateway stripeGateway;
    private final PaymentGateway payPalGateway;

    // 4 workers, queue capacity 100
    private final ManualThreadPool pool = new ManualThreadPool(4,100);

    public LearningPaymentService(PaymentRepository repository,
                                  PaymentGateway stripeGateway,
                                  PaymentGateway payPalGateway) {
        this.repository = Objects.requireNonNull(repository);
        this.stripeGateway = stripeGateway;
        this.payPalGateway = payPalGateway;
    }

    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        var payment = new Payment(
                UUID.randomUUID(),
                request.amount() != null ? request.amount() : BigDecimal.ZERO,
                request.currency(),
                request.method(),
                PaymentStatus.PROCESSING,
                Instant.now(),
                null
        );
        repository.save(payment);

        try {
            pool.execute(() -> processAsync(payment));
            return PaymentResult.accepted(payment.id());

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            repository.save(payment.withFailure("QUEUE_INTERRUPTED"));
            return PaymentResult.failed(payment.id(), "System interrupted");
        }
    }

    private void processAsync(Payment payment) {
        try {
            var gw = selectGateway(payment.method());
            gw.charge(payment);
            repository.save(payment.succeed());
        } catch (PaymentGateway.GatewayException ge) {
            repository.save(payment.withFailure(ge.getMessage()));
        } catch (Throwable t) {
            repository.save(payment.withFailure("UNEXPECTED: " + t.getClass().getSimpleName()));
        }
    }

    @Override
    public PaymentResult getStatus(UUID paymentId) {
        return repository.findById(paymentId)
                .map(p -> new PaymentResult(p.id(), p.status(), p.failureReason()))
                .orElseGet(() -> new PaymentResult(paymentId, PaymentStatus.FAILED, "Not found"));
    }

    private PaymentGateway selectGateway(String method) {
        String m = method == null ? "" : method.toLowerCase();
        return switch (m) {
            case "stripe" -> stripeGateway;
            case "paypal" -> payPalGateway;
            default -> stripeGateway; // default for Sprint 1
        };
    }
}
