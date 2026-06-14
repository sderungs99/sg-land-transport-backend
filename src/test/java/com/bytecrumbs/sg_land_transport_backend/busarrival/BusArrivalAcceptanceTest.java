package com.bytecrumbs.sg_land_transport_backend.busarrival;

import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.AcceptanceTest;
import org.junit.jupiter.api.Test;

/**
 * Acceptance test for the Bus Arrival walking skeleton. Speaks only domain language through the DSL;
 * the harness (see {@link AcceptanceTest}) is the only place that knows the LTA Datamall fake is WireMock.
 */
class BusArrivalAcceptanceTest extends AcceptanceTest {

    @Test
    void clientSeesABusArrivingInThreeMinutes() {
        dsl.busArriving("stop: alpha", "service: 15", "inMinutes: 3");

        dsl.clientRequestsArrivals("stop: alpha");

        dsl.confirmServiceArriving("stop: alpha", "service: 15", "inMinutes: 3");
    }

    @Test
    void eachStopReportsItsOwnArrivalTime() {
        dsl.busArriving("stop: alpha", "service: 15", "inMinutes: 3");
        dsl.busArriving("stop: beta", "service: 99", "inMinutes: 7");

        dsl.clientRequestsArrivals("stop: alpha");
        dsl.clientRequestsArrivals("stop: beta");

        dsl.confirmServiceArriving("stop: alpha", "service: 15", "inMinutes: 3");
        dsl.confirmServiceArriving("stop: beta", "service: 99", "inMinutes: 7");
    }

    @Test
    void theLtaClientSendsTheAccountKeyHeaderUpstream() {
        dsl.busArriving("stop: alpha", "service: 15", "inMinutes: 3");

        dsl.clientRequestsArrivals("stop: alpha");

        dsl.confirmLtaReceivedAccountKey("stop: alpha");
    }
}
