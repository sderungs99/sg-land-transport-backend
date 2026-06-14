package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A {@link Clock} whose "now" the acceptance DSL can fix to a logical time {@code T}.
 * In the in-JVM-real-HTTP topology the SUT and the DSL share this one instance, so the
 * DSL can pin the SUT's clock to the same {@code T} it uses to compute the fake's
 * {@code EstimatedArrival}, making "arriving in N minutes" exact.
 */
public final class MutableClock extends Clock {

    private final ZoneId zone;
    private volatile Instant instant;

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public Instant instant() {
        return instant;
    }

    @Override
    public long millis() {
        return instant.toEpochMilli();
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone == null ? ZoneOffset.UTC : zone);
    }
}
