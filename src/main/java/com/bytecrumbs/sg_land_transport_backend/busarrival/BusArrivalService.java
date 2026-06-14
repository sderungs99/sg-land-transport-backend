package com.bytecrumbs.sg_land_transport_backend.busarrival;

import com.bytecrumbs.sg_land_transport_backend.busarrival.BusStopArrivals.Arrival;
import com.bytecrumbs.sg_land_transport_backend.busarrival.BusStopArrivals.ServiceArrivals;
import com.bytecrumbs.sg_land_transport_backend.busarrival.LtaBusArrival.LtaNextBus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Fetches arrivals from LTA Datamall on demand and maps each service's first {@code NextBus}
 * into "arriving in N minutes", computed deterministically against the injected {@link Clock}.
 */
@Service
public class BusArrivalService {

    private final LtaClient lta;
    private final Clock clock;

    public BusArrivalService(LtaClient lta, Clock clock) {
        this.lta = lta;
        this.clock = clock;
    }

    public BusStopArrivals arrivalsAt(String busStopCode) {
        Instant now = clock.instant();
        List<ServiceArrivals> services = lta.fetchArrivals(busStopCode).services().stream()
                .map(service -> new ServiceArrivals(
                        service.serviceNo(),
                        List.of(new Arrival(minutesUntil(service.nextBus(), now)))))
                .toList();
        return new BusStopArrivals(busStopCode, services);
    }

    private long minutesUntil(LtaNextBus nextBus, Instant now) {
        Instant estimatedArrival = OffsetDateTime.parse(nextBus.estimatedArrival()).toInstant();
        return Duration.between(now, estimatedArrival).toMinutes();
    }
}
