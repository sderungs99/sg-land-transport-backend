package com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers;

import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.StopArrivalsView;

/**
 * How the harness drives the System Under Test for Bus Arrival. The interface is the swappable
 * seam: today an in-JVM real-HTTP driver, tomorrow an out-of-process one (Testcontainers) — the
 * DSL and test cases never change (ADR-0001).
 */
public interface BusArrivalProtocolDriver {

    StopArrivalsView requestArrivals(String busStopCode);
}
