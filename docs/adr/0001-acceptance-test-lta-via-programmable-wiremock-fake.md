# Test against LTA Datamall via a programmable WireMock fake at the HTTP boundary

Our working model is Acceptance Test Driven Development (Dave Farley): tests exercise the deployable as a black box through its real external interfaces, separated from the system by a four-layer model (test case → DSL → protocol driver → SUT). Because this service's whole job is talking to LTA Datamall over HTTP, we replace LTA at the **HTTP boundary** with a **runtime-programmable WireMock fake** that the acceptance DSL configures per scenario. We deliberately do *not* mock at a client interface (Mockito) or with Spring's `MockRestServiceServer`, because both are in-JVM and white-box and cannot serve a black-box deployable — and the real HTTP/deserialization path is the single most important thing to test in a proxy.

## Decisions

- **Seam: the HTTP boundary.** The app makes a real HTTP call; WireMock answers it. Selected via a `lta.base-url` property.
- **Topology: in-JVM-real-HTTP now, out-of-process later.** Start with `@SpringBootTest(RANDOM_PORT)` + WireMock on a port (fast feedback). Keep the seam config-driven and the DSL clean so we can promote to a fully out-of-process harness (built artifact + fake as separate containers via Testcontainers) without rewriting test cases.
- **Fake hidden behind the DSL.** Only the DSL knows it's WireMock. If WireMock's stub model fights us when we add stateful caching/pagination, we can swap in a hand-rolled Spring Boot fake behind the same DSL.
- **Time is deterministic via an injected `Clock`.** Production depends on a `java.time.Clock` bean, not `Instant.now()`. The DSL owns a single logical "now" `T`, fixes the SUT clock to `T`, and tells the fake to return `EstimatedArrival = T + N`. So "arriving in N minutes" is exact and repeatable, and edge cases (already departed, not running) are stageable.
- **Fixture provenance: golden capture + offline fidelity test.** Capture one real LTA response per endpoint (AccountKey stripped), commit it, and write a narrow test that deserializes the golden file into our DTOs. This catches the bug acceptance tests structurally can't: our model of LTA's shape being wrong. Acceptance stubs themselves stay synthetic and DSL-programmed (templated times). Refreshing goldens needs a real AccountKey + live call, so it's a manual, not-in-CI harness; CI stays offline and deterministic.
- **Isolation: reset-per-test serial now, DSL ready for parallel.** `WireMock.reset()` per test, suite runs serially. The DSL allocates a unique `BusStopCode` per scenario so test data is already collision-free; we flip on parallelism (and drop the reset) later without rewriting tests, compatible with the single shared fake in the out-of-process topology.
- **Sad paths modelled in slice one:** upstream 5xx/timeout → `502`/`504`; empty `Services` → `200` empty list (not an error); empty-string `EstimatedArrival` → that bus omitted/flagged; plus one test asserting the `AccountKey` header is actually sent upstream. Deferred: `429` backoff, malformed JSON. `401`/bad-key treated as a startup/config concern, not a per-request acceptance test.

## Considered and rejected

- **`MockRestServiceServer` / Mockito client mocks** — in-JVM, white-box; can't serve a black-box deployable and skip the real HTTP/deserialization path. May still be used later for narrow unit tests of pure transformation logic, but not as the acceptance seam.
- **Pact / Spring Cloud Contract against LTA** — consumer-driven contracts need a provider we control to verify them; LTA is a fixed third party. (Pact *is* the intended tool for the future boundary between this backend as provider and our own front-end as consumer — a different boundary, a different strategy.)
- **Hand-rolled fake from day one** — more code to own than slice one justifies; deferred unless WireMock's stub model becomes a fight under stateful caching/pagination.
- **Fully out-of-process from day one** — container orchestration cost without payback yet; deferred behind a config-driven seam.

## Scope note

Slice one is **Bus Arrival**, built as a fetch-on-demand transformer (no cache) — so it does not yet exercise the caching/aggregating machinery that defines this service's eventual shape. The caching/statefulness decisions (stateful fake scenarios, pagination, staleness/refresh) are deferred to a later slice on a reference-data endpoint and are not covered by this ADR.
