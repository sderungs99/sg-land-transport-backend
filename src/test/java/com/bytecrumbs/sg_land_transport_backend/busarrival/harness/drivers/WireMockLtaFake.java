package com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * The one class that knows the LTA Datamall fake is WireMock: it owns the wire format (endpoint
 * path, PascalCase JSON, the {@code AccountKey} header) and the stubbing/recording API.
 */
public class WireMockLtaFake implements LtaFake {

    private static final String BUS_ARRIVAL_PATH = "/ltaodataservice/v3/BusArrival";

    private final WireMockServer server;

    public WireMockLtaFake(WireMockServer server) {
        this.server = server;
    }

    @Override
    public void reset() {
        server.resetAll();
    }

    @Override
    public void stageBus(String busStopCode, String serviceNo, Instant estimatedArrival) {
        String eta = OffsetDateTime.ofInstant(estimatedArrival, ZoneOffset.ofHours(8))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String body = """
                {"BusStopCode":"%s","Services":[{"ServiceNo":"%s","NextBus":{"EstimatedArrival":"%s"}}]}"""
                .formatted(busStopCode, serviceNo, eta);
        server.stubFor(get(urlPathEqualTo(BUS_ARRIVAL_PATH))
                .withQueryParam("BusStopCode", equalTo(busStopCode))
                .willReturn(okJson(body)));
    }

    @Override
    public boolean receivedRequestWithAccountKey(String busStopCode, String expectedAccountKey) {
        return !server.findAll(getRequestedFor(urlPathEqualTo(BUS_ARRIVAL_PATH))
                .withQueryParam("BusStopCode", equalTo(busStopCode))
                .withHeader("AccountKey", equalTo(expectedAccountKey))).isEmpty();
    }
}
