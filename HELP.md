# Payment Processor – Concurrent Payments (Learning + Production)

A clean payment processing service built with **Java 21** and **Spring Boot 3**.  
The project shows **two paths** under one stable API:

- **Learning mode**: manual concurrency & resilience to master the fundamentals.
- **Production mode**: industry-grade tools (ThreadPoolExecutor / Virtual Threads, Resilience4j, Micrometer).

> ✅ Current scope: **Sprint 1 + Sprint 2** (base project + manual producer–consumer with worker threads).

---

## ✨ Why this project

I want to demonstrate both **foundations** (how threads and coordination actually work) and **production practices** (how we do it in real systems). The REST API stays the same while the internal implementation switches via a **feature flag**.

---

## 🧠 What’s implemented so far

### Sprint 1 – Base & clean layering
- Domain models using **Java records** (immutable).
- REST **controller** + **orchestrator** with feature flag `payment.mode=learning|production`.
- Fake **gateways** (Stripe/PayPal) with latency & random failures.
- In-memory **repository** (ConcurrentHashMap) behind a clear interface.

### Sprint 2 – Manual threading (producer–consumer)
- **ManualPaymentQueue** using `synchronized` / `wait` / `notifyAll`.
- **WorkerThread** consumers + **ManualThreadPool** (fixed workers, bounded queue).
- **LearningPaymentService** now enqueues tasks and returns **202 Accepted** immediately.

> In learning mode I implemented a classic producer–consumer queue with `synchronized`/`wait`/`notify`. The REST thread accepts the request, enqueues a task, and returns `202` immediately. A small pool of worker threads pulls tasks and calls the gateway. The queue is **bounded** to provide **backpressure**: if traffic grows faster than we can process, producers block instead of allocating unbounded memory. I carefully used `while (waitCondition) wait()` to avoid **spurious wakeups** and **restored the interrupt flag** when needed. In production I replace all this with **ThreadPoolExecutor** (or **virtual threads**) and **BlockingQueue**, plus **Resilience4j** for circuit breaker / rate limiting.

---

## 🏗 Architecture (current)

```
HTTP (PaymentController)
        │
        ▼
 PaymentOrchestrator  ──(feature flag)──► LearningPaymentService
        │                                   ├─ ManualThreadPool (workers)
        │                                   └─ ManualPaymentQueue (bounded)
        └────────────────────────────────► ProductionPaymentService (placeholder)
                                              (ThreadPoolExecutor / VTs + Resilience4j in Sprint 4)
        ▼
   PaymentGateway (Stripe, PayPal)  ← latency + random failures
        ▼
   PaymentRepository (in-memory for now)
```

---

## 🚀 How to run

```bash
# Java 21 & Maven
mvn spring-boot:run
```

Config (`src/main/resources/application.yml`):

```yaml
payment:
  mode: learning   # change to "production" in Sprint 4
```

---

## 🔌 Endpoints

- **POST** `/api/payments`  
  Request body:
  ```json
  {"amount": 100, "currency": "USD", "method": "stripe"}
  ```
  Response: `202 Accepted` with `{ "paymentId": "...", "status": "PROCESSING" }`

- **GET** `/api/payments/{id}`  
  Returns the current status: `PROCESSING | SUCCESS | FAILED`

**cURL example**
```bash
curl -X POST http://localhost:8080/api/payments   -H "Content-Type: application/json"   -d '{"amount":100, "currency":"USD", "method":"paypal"}'
```

---

## 🔍 Concurrency notes (learning mode)

- **Monitor pattern**: guarded blocks with `while (empty) wait();` and `notifyAll()` to wake producers/consumers safely.
- **Bounded queue** (capacity): provides **backpressure**; producers **block** when full.
- **Graceful shutdown**: workers honor interrupts (we restore the interrupt flag when caught).

---

## 🧪 Testing (added now / planned next)

- Queue smoke test: producer/consumer under contention (Sprint 2).
- Integration tests: POST 202 then status evolves to SUCCESS/FAILED (next step).
- Benchmarks & metrics planned for Sprints 4–5.

---

## 🛣 Roadmap

- **Sprint 3**: Manual resilience — Circuit Breaker (volatile + synchronized), RateLimiter (Semaphore), Retry (exponential backoff).
- **Sprint 4**: Production mode — ThreadPoolExecutor / Virtual Threads, Resilience4j, Micrometer.
- **Sprint 5**: Full test suite, docs, benchmarks, CI.

---