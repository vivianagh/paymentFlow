package com.paymentflow.service.learning;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// Simple concurrency limiter using Semaphore
public class ManualRateLimiter {
    private final Semaphore semaphore;

    public ManualRateLimiter(int maxConcurrent) {
        if (maxConcurrent <= 0) {
            throw new IllegalArgumentException("maxConcurrent must be greater than 0");
        }
        this.semaphore = new Semaphore(maxConcurrent);
    }

    public boolean acquire(long maxWaitMillis) throws InterruptedException {
        if (maxWaitMillis <= 0) {
            return semaphore.tryAcquire();
        }
        return semaphore.tryAcquire(maxWaitMillis, TimeUnit.MILLISECONDS);
    }

    public void release() {
        semaphore.release();
    }

    public int availablePermits() {
        return semaphore.availablePermits();
    }
}
