package com.paymentflow.service.production;

import com.paymentflow.model.PaymentRequest;
import com.paymentflow.model.PaymentResult;
import com.paymentflow.model.PaymentStatus;
import com.paymentflow.repository.PaymentRepository;
import com.paymentflow.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("productionPaymentService")
public class ProductionPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;

    public ProductionPaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        return new PaymentResult(
                UUID.randomUUID(),
                PaymentStatus.PROCESSING,
                "Production mode placeholder. Will be implemented in Sprint 4."
        );
    }

    @Override
    public PaymentResult getStatus(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(p -> new PaymentResult(p.id(), p.status(), p.failureReason()))
                .orElse(new PaymentResult(paymentId, PaymentStatus.FAILED, "Not Found"));
    }
}
