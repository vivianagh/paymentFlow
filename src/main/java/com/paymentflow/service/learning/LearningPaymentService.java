package com.paymentflow.service.learning;

import com.paymentflow.gateway.PaymentGateway;
import com.paymentflow.model.Payment;
import com.paymentflow.model.PaymentRequest;
import com.paymentflow.model.PaymentResult;
import com.paymentflow.model.PaymentStatus;
import com.paymentflow.repository.PaymentRepository;
import com.paymentflow.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

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

    private final ManualCircuitBreaker breaker = new ManualCircuitBreaker(
            3,
            100,
            2
    );
    private final ManualRateLimiter limiter = new ManualRateLimiter(20);
    private final ManualCache<String, UUID> idemCache = new ManualCache<>();


    public LearningPaymentService(PaymentRepository repository,
                                  @Qualifier("stripeGateway") PaymentGateway stripeGateway,
                                  @Qualifier("payPalGateway")  PaymentGateway payPalGateway) {
        this.repository = Objects.requireNonNull(repository);
        this.stripeGateway = stripeGateway;
        this.payPalGateway = payPalGateway;
    }

    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        // Idempotency
        UUID pid = request.idempotencyKey() == null ? UUID.randomUUID()
                : idemCache.remember(request.idempotencyKey(), key -> UUID.randomUUID());

        var payment = new Payment(
                pid,
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

        // Circuit breaker fast-fail
        if (!breaker.allowRequest()) {
            repository.save(payment.withFailure("CIRCUIT_OPEN"));
            return;
        }
        boolean acquired = false;

        try {
            //Rate limit (concurrency)
            acquired = limiter.acquire(200); //wait up to 200ms
            if (acquired) {
                repository.save(payment.withFailure("RATE_LIMIT"));
                breaker.recordFailure(); // because we refused to call downstream
                return;
            }
            var gw = selectGateway(payment.method());
            // Retry only for transient gateway exceptions
            PaymentResult result = ManualRetry.execute(3, 100, () -> gw.charge(payment));
            repository.save(payment.succeed());
            breaker.recordSuccess();
        } catch (PaymentGateway.GatewayException ge) {
            breaker.recordFailure();
            repository.save(payment.withFailure(ge.getMessage()));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            breaker.recordFailure();
            repository.save(payment.withFailure("INTERRUPTED"));
        } catch (Exception e) {
            breaker.recordFailure();
            repository.save(payment.withFailure("UNEXPECTED: " + e.getClass().getSimpleName()));
        } finally {
            if (acquired) limiter.release();
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
