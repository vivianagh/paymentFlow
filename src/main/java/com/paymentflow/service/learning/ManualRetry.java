package com.paymentflow.service.learning;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class ManualRetry {

    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }

    public static <T> T execute(int maxAttempts, long initialBackoffMs,
                                SupplierWithException<T> action) throws Exception {
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be >= 1");
        long backoff = Math.max(1, initialBackoffMs);
        Exception last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            } catch (Exception e) {
                last = e;
                if (attempt == maxAttempts) break;
                long jitter = ThreadLocalRandom.current().nextLong(Math.max(1, backoff / 2), backoff + 1);
                try { Thread.sleep(jitter); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw ie; }
                backoff = Math.min(backoff * 2, 2_000L);
            }
        }
        throw last;
    }

}
