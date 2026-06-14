package com.bytecrumbs.sg_land_transport_backend.busarrival;

import java.util.List;

/** The Client-facing Bus Arrival response: per-service arrivals at a bus stop. */
public record BusStopArrivals(String busStopCode, List<ServiceArrivals> services) {

    public record ServiceArrivals(String serviceNo, List<Arrival> arrivals) {
    }

    public record Arrival(long minutes) {
    }
}
