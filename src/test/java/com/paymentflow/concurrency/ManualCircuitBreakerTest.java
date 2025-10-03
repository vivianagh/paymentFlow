package com.paymentflow.concurrency;

import com.paymentflow.service.learning.ManualCircuitBreaker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManualCircuitBreakerTest {

    @Test
    void openAfterFailureAndHalfOpenAfterTimeout() throws InterruptedException {
        var cb = new ManualCircuitBreaker(2, 200, 1);

        assertTrue(cb.allowRequest());
        cb.recordFailure();
        assertEquals(ManualCircuitBreaker.State.CLOSED, cb.getState());

        assertTrue(cb.allowRequest());
        cb.recordFailure();
        assertEquals(ManualCircuitBreaker.State.OPEN, cb.getState());

        assertFalse(cb.allowRequest()); // still open

        Thread.sleep(220);
        assertTrue(cb.allowRequest()); // moves to HALF_OPEN
        cb.recordSuccess();            // successThreshold=1
        assertEquals(ManualCircuitBreaker.State.CLOSED, cb.getState());
    }
}
