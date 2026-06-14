package com.bytecrumbs.sg_land_transport_backend.busarrival;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Talks to LTA Datamall over real HTTP. Base URL is config-driven ({@code lta.base-url}) so
 * acceptance tests can point it at the WireMock fake; the {@code AccountKey} header LTA requires
 * is sent on every request.
 */
@Component
public class LtaClient {

    private static final String BUS_ARRIVAL_PATH = "/ltaodataservice/v3/BusArrival";

    private final RestClient http;

    public LtaClient(@Value("${lta.base-url}") String baseUrl,
                     @Value("${lta.account-key}") String accountKey) {
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("AccountKey", accountKey)
                .build();
    }

    public LtaBusArrival fetchArrivals(String busStopCode) {
        return http.get()
                .uri(uri -> uri.path(BUS_ARRIVAL_PATH)
                        .queryParam("BusStopCode", busStopCode)
                        .build())
                .retrieve()
                .body(LtaBusArrival.class);
    }
}
