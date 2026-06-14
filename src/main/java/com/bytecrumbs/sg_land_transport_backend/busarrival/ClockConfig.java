package com.bytecrumbs.sg_land_transport_backend.busarrival;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Production depends on an injected {@link Clock} (never {@code Instant.now()}) so acceptance
 * tests can pin "now" to a deterministic logical time.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
