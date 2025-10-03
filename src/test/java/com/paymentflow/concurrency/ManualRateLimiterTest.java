package com.paymentflow.concurrency;

import com.paymentflow.service.learning.ManualRateLimiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManualRateLimiterTest {

    @Test
    void blocksWhenNoPermits() throws Exception {
        var rl = new ManualRateLimiter(1);
        assertTrue(rl.acquire(0));
        long start = System.currentTimeMillis();
        assertFalse(rl.acquire(100)); // no release yet, should time out
        long took = System.currentTimeMillis() - start;
        assertTrue(took >= 90);
        rl.release();
    }
}
