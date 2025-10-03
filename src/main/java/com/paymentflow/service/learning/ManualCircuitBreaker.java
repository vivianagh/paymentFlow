package com.paymentflow.service.learning;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Educational Circuit Breaker using volatile for visibility and synchronized for atomic transitions.
 * States: CLOSED → OPEN → HALF_OPEN
 */
public class ManualCircuitBreaker {

    public enum State {CLOSED, OPEN, HALF_OPEN, HALF_CLOSED}

    private volatile State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile long lastFailureTimeMillis  = 0;

    private final int failureThreshold;
    private final long openTimeoutMillis ;
    private final int successThreshold;

    public ManualCircuitBreaker(int failureThreshold, long openTimeoutMillis, int successThreshold) {
        this.failureThreshold = failureThreshold;
        this.openTimeoutMillis = openTimeoutMillis;
        this.successThreshold = successThreshold;
    }

    // Check if a call is allowed at this moment
    public synchronized boolean allowRequest() {
        if (state == State.OPEN) {
            long now = System.currentTimeMillis();
            if (now - lastFailureTimeMillis >= openTimeoutMillis) {
                state = State.HALF_OPEN;
                successCount.set(0);
                return true; //allow probe
            }
            return false; //still open
        }
        return true; // closed or half_open
    }

    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            if (successCount.incrementAndGet() >= successThreshold) {
                state = State.CLOSED;
                failureCount.set(0);
            }
        } else if (state == State.CLOSED) {
            failureCount.set(0);
        }
    }

    public synchronized void recordFailure() {
        lastFailureTimeMillis = System.currentTimeMillis();
        if (state == State.HALF_OPEN) {
            state = State.OPEN;
            return;
        }
        if (failureCount.incrementAndGet() >= failureThreshold) {
            state = State.OPEN;
        }
    }

    public State getState() { return state; }

}
