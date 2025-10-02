package com.paymentflow.service.learning;

class WorkerThread extends Thread {
    private final ManualPaymentQueue<Runnable> queue;

    WorkerThread(ManualPaymentQueue<Runnable> queue, String name) {
        super(name);
        this.queue = queue;
        setDaemon(true);
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                Runnable task = queue.take();
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }
}
