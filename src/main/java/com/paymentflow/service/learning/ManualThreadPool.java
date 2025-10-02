package com.paymentflow.service.learning;

import java.util.ArrayList;
import java.util.List;

class ManualThreadPool {
    private final ManualPaymentQueue<Runnable> queue;
    private final List<WorkerThread> workers = new ArrayList<>();

    ManualThreadPool(int workerCount, int queueCapacity) {
        this.queue = new ManualPaymentQueue<>(queueCapacity);
        for (int i = 0; i < workerCount; i++) {
            WorkerThread w = new WorkerThread(queue, "pay-worker-" + i);
            w.start();
            workers.add(w);
        }
    }

    public void execute(Runnable task) throws InterruptedException {
        queue.put(task);
    }

    public void shutdownNow() {
        workers.forEach(Thread::interrupt);
    }

    public int queueSize() {return queue.size();}
}
