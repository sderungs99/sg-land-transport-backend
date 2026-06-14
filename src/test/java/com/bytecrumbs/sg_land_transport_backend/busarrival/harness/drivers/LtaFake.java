package com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers;

import java.time.Instant;

/**
 * Programs and inspects the LTA Datamall test double. The interface hides the fake technology
 * (WireMock today) so the DSL only expresses intent: stage a bus, check what reached upstream.
 */
public interface LtaFake {

    void reset();

    void stageBus(String busStopCode, String serviceNo, Instant estimatedArrival);

    boolean receivedRequestWithAccountKey(String busStopCode, String expectedAccountKey);
}
