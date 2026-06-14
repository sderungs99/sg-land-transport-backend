package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Base for Bus Arrival acceptance tests. Holds all of the harness wiring — the real-HTTP topology,
 * the LTA Datamall fake, and pointing the SUT's {@code lta.base-url} at it — so test cases only
 * speak domain language through the {@link BusArrivalDsl}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({LtaFakeConfig.class, BusArrivalDsl.class})
public abstract class AcceptanceTest {

    @Autowired
    protected BusArrivalDsl dsl;

    @BeforeEach
    void freshScenario() {
        dsl.reset();
    }

    @DynamicPropertySource
    static void ltaProperties(DynamicPropertyRegistry registry) {
        registry.add("lta.base-url", LtaFakeConfig::baseUrl);
        registry.add("lta.account-key", LtaFakeConfig::accountKey);
    }
}
