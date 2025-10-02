package com.paymentflow.controller;


import com.paymentflow.model.PaymentRequest;
import com.paymentflow.model.PaymentResult;
import com.paymentflow.service.PaymentOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentOrchestrator orchestrator;

    public PaymentController(PaymentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public ResponseEntity<PaymentResult> create(@Valid @RequestBody PaymentRequest paymentRequest) {
        var res = orchestrator.create(paymentRequest);
        return ResponseEntity.accepted().body(res);
    }

    @GetMapping("/id")
    public PaymentResult status(@PathVariable UUID id) {
        return orchestrator.status(id);
    }
}
