package com.bytecrumbs.sg_land_transport_backend.busarrival.harness.drivers;

import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.StopArrivalsView;
import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.StopArrivalsView.ServiceView;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives the SUT in the in-JVM real-HTTP topology: a real HTTP request to the embedded server on
 * its random port, translating the JSON response into a {@link StopArrivalsView}. All knowledge of
 * the wire format (URL, status, JSON paths) lives here, not in the DSL.
 */
public class HttpBusArrivalProtocolDriver implements BusArrivalProtocolDriver {

    private final Environment environment;

    public HttpBusArrivalProtocolDriver(Environment environment) {
        this.environment = environment;
    }

    @Override
    public StopArrivalsView requestArrivals(String busStopCode) {
        ResponseEntity<String> response = sutClient().get()
                .uri("/bus-stops/{stop}/arrivals", busStopCode)
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return parse(response.getBody());
    }

    private StopArrivalsView parse(String body) {
        DocumentContext json = JsonPath.parse(body);
        List<String> serviceNos = json.read("$.services[*].serviceNo");
        List<ServiceView> services = new ArrayList<>();
        for (int i = 0; i < serviceNos.size(); i++) {
            List<Integer> minutes = json.read("$.services[" + i + "].arrivals[*].minutes");
            services.add(new ServiceView(serviceNos.get(i), minutes));
        }
        return new StopArrivalsView(json.read("$.busStopCode"), services);
    }

    private RestClient sutClient() {
        String port = environment.getProperty("local.server.port");
        return RestClient.create("http://localhost:" + port);
    }
}
