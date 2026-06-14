package com.bytecrumbs.sg_land_transport_backend.busarrival;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Client-facing Bus Arrival endpoint. */
@RestController
public class BusArrivalController {

    private final BusArrivalService service;

    public BusArrivalController(BusArrivalService service) {
        this.service = service;
    }

    @GetMapping("/bus-stops/{busStopCode}/arrivals")
    public BusStopArrivals arrivals(@PathVariable String busStopCode) {
        return service.arrivalsAt(busStopCode);
    }
}
