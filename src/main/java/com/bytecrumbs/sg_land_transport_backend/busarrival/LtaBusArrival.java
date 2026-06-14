package com.bytecrumbs.sg_land_transport_backend.busarrival;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Our model of the LTA Datamall Bus Arrival response shape (only the fields this slice maps).
 * Field names mirror LTA's PascalCase JSON via {@link JsonProperty}.
 */
public record LtaBusArrival(@JsonProperty("Services") List<LtaService> services) {

    public List<LtaService> services() {
        return services == null ? List.of() : services;
    }

    public record LtaService(@JsonProperty("ServiceNo") String serviceNo,
                             @JsonProperty("NextBus") LtaNextBus nextBus) {
    }

    public record LtaNextBus(@JsonProperty("EstimatedArrival") String estimatedArrival) {
    }
}
