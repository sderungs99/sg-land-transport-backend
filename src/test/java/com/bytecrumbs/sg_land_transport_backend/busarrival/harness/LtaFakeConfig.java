package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers.BusArrivalProtocolDriver;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers.HttpBusArrivalProtocolDriver;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers.LtaFake;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers.WireMockLtaFake;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Stands up the LTA Datamall fake (WireMock) for acceptance tests and points the SUT at it
 * via {@code lta.base-url}. Part of the harness, not a test case — knowing this is WireMock
 * is allowed here; test cases must not.
 */
@TestConfiguration(proxyBeanMethods = false)
public class LtaFakeConfig {

    static final String ACCOUNT_KEY = "test-account-key";

    public static final WireMockServer LTA_FAKE =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        LTA_FAKE.start();
    }

    @Bean(destroyMethod = "")
    WireMockServer ltaWireMockServer() {
        return LTA_FAKE;
    }

    @Bean
    LtaFake ltaFake(WireMockServer ltaWireMockServer) {
        return new WireMockLtaFake(ltaWireMockServer);
    }

    @Bean
    BusArrivalProtocolDriver busArrivalProtocolDriver(Environment environment) {
        return new HttpBusArrivalProtocolDriver(environment);
    }

    /** Test clock the DSL pins to its logical "now"; primary so it replaces the production {@code Clock}. */
    @Bean
    @Primary
    MutableClock testClock() {
        return new MutableClock(Instant.EPOCH, ZoneOffset.UTC);
    }

    /** Base URL of the running fake, for pointing the SUT's {@code lta.base-url} at it. */
    public static String baseUrl() {
        return LTA_FAKE.baseUrl();
    }

    public static String accountKey() {
        return ACCOUNT_KEY;
    }
}
