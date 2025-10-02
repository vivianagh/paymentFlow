package com.paymentflow.service.learning;

import java.util.ArrayDeque;
import java.util.Queue;

// Not for production use, it's for learning thread coordination.
public class ManualPaymentQueue<T> {

    private final Queue<T> queue = new ArrayDeque<>();
    private final int capacity;

    public ManualPaymentQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        this.capacity = capacity;
    }

    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            wait(); //wait until there's space
        }
        queue.add(item);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T result = queue.remove();
        notifyAll();
        return result;
    }

    public synchronized int size() {
        return queue.size();
    }
}
