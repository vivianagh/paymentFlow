package com.paymentflow.concurrency;

import com.paymentflow.service.learning.ManualRetry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManualRetryTest {

    @Test
    void retriesAndEventuallySucceeds() throws Exception {
        final int[] counter = {0};
        String out = ManualRetry.execute(3, 50, () -> {
            if (++counter[0] < 3) throw new RuntimeException("transient");
            return "OK";
        });
        assertEquals("OK", out);
    }
}
