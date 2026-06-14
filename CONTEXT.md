# SG Land Transport Backend

A backend service that sits in front of Singapore's LTA Datamall APIs. It is a **caching/aggregating proxy**: it pulls data from LTA Datamall, persists it, serves its own clients from that persisted copy, and refreshes the copy over time.

## Language

**LTA Datamall**:
The upstream, third-party source of Singapore land-transport data that this service consumes. Owned and operated by Singapore's Land Transport Authority.
_Avoid_: "the API" (ambiguous — we expose an API too), "upstream" used bare.

**Caching/aggregating proxy**:
What this service is. It does not pass LTA responses straight through; it stores them, may combine multiple LTA endpoints, and serves clients from its own store rather than calling LTA on every request.
_Avoid_: "pass-through proxy", "gateway".

**Client**:
A consumer of *this* service's API — notably the front-end that the same team owns. Distinct from this service's own role as a consumer of LTA Datamall.
_Avoid_: "user", "consumer" used bare (ambiguous — we are also a consumer, of LTA).
