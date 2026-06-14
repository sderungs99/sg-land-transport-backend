package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.Params.DslContext;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.StopArrivalsView.ServiceView;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers.BusArrivalProtocolDriver;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers.LtaFake;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The acceptance DSL: pure domain translation, expressed with named parameters. It owns one logical
 * "now" {@code T} per scenario, pins the SUT clock to {@code T}, and stages the LTA fake to return
 * {@code EstimatedArrival = T + N} so "arriving in N minutes" is exact. Each verb's "Then" asserts
 * here, in domain terms; the DSL knows nothing of HTTP, JSON, or WireMock.
 */
public class BusArrivalDsl {

    private final BusArrivalProtocolDriver sut;
    private final LtaFake lta;
    private final MutableClock clock;
    private final String accountKey;

    private DslContext context;
    private final Map<String, StopArrivalsView> lastArrivals = new HashMap<>();
    private Instant now;

    public BusArrivalDsl(BusArrivalProtocolDriver sut,
                         LtaFake lta,
                         MutableClock clock,
                         @Value("${lta.account-key}") String accountKey) {
        this.sut = sut;
        this.lta = lta;
        this.clock = clock;
        this.accountKey = accountKey;
    }

    /** Start a fresh scenario: clear the fake, the remembered responses and aliases, and pin "now". */
    public void reset() {
        lta.reset();
        context = new DslContext();
        lastArrivals.clear();
        now = Instant.parse("2026-06-14T02:00:00Z");
        clock.setInstant(now);
    }

    /** Given: a bus on {@code service} arriving in {@code inMinutes} at {@code stop}. */
    public void busArriving(String... args) {
        Params params = new Params(context, args);
        String stop = params.alias("stop");
        String service = params.required("service");
        int minutes = params.requiredInt("inMinutes");

        lta.stageBus(stop, service, now.plus(Duration.ofMinutes(minutes)));
    }

    /** When: a Client requests arrivals at {@code stop}. */
    public void clientRequestsArrivals(String... args) {
        Params params = new Params(context, args);
        String stop = params.alias("stop");

        lastArrivals.put(stop, sut.requestArrivals(stop));
    }

    /** Then: the Client sees {@code service} arriving in {@code inMinutes} at {@code stop}. */
    public void confirmServiceArriving(String... args) {
        Params params = new Params(context, args);
        String stop = params.alias("stop");
        String service = params.required("service");
        int expectedMinutes = params.requiredInt("inMinutes");

        ServiceView arriving = serviceAt(stop, service);
        assertThat(arriving.arrivalMinutes())
                .as("arrivals of service %s at stop %s", service, context.decodeAlias(stop))
                .isNotEmpty();
        assertThat(arriving.arrivalMinutes().get(0))
                .as("minutes until next %s at stop %s", service, context.decodeAlias(stop))
                .isEqualTo(expectedMinutes);
    }

    /** Then: the LTA client sent the {@code AccountKey} header upstream for {@code stop}. */
    public void confirmLtaReceivedAccountKey(String... args) {
        Params params = new Params(context, args);
        String stop = params.alias("stop");

        assertThat(lta.receivedRequestWithAccountKey(stop, accountKey))
                .as("LTA received a request for stop %s carrying the AccountKey header",
                        context.decodeAlias(stop))
                .isTrue();
    }

    private ServiceView serviceAt(String stop, String service) {
        StopArrivalsView arrivals = lastArrivals.get(stop);
        assertThat(arrivals)
                .as("a Client requested arrivals at stop %s", context.decodeAlias(stop))
                .isNotNull();
        Optional<ServiceView> match = arrivals.services().stream()
                .filter(s -> s.serviceNo().equals(service))
                .findFirst();
        assertThat(match)
                .as("service %s present at stop %s", service, context.decodeAlias(stop))
                .isPresent();
        return match.get();
    }
}
