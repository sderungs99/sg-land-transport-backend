package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import java.util.List;

/**
 * Protocol-neutral view of a Client Bus Arrival response. Protocol drivers translate the wire
 * format (JSON over HTTP) into this; the DSL asserts against it in domain terms, so the DSL never
 * sees JSON paths or status codes.
 */
public record StopArrivalsView(String busStopCode, List<ServiceView> services) {

    public record ServiceView(String serviceNo, List<Integer> arrivalMinutes) {
    }
}
