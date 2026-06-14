package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import com.bytecrumbs.sg_land_transport_backend.busarrival.harness.Params.DslContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParamsTest {

    @Test
    void readsNamedValuesAndDefaultsTheRest() {
        Params params = new Params(new DslContext(), "service: 15", "inMinutes: 3");

        assertThat(params.required("service")).isEqualTo("15");
        assertThat(params.requiredInt("inMinutes")).isEqualTo(3);
        assertThat(params.optional("operator", "SBS")).isEqualTo("SBS");
    }

    @Test
    void failsWhenARequiredValueIsMissing() {
        Params params = new Params(new DslContext(), "service: 15");

        assertThatThrownBy(() -> params.required("stop"))
                .hasMessageContaining("No 'stop' supplied");
    }

    @Test
    void aliasIsStableWithinAScenarioButUniqueAcrossScenarios() {
        DslContext one = new DslContext();
        DslContext two = new DslContext();

        String first = new Params(one, "stop: alpha").alias("stop");
        String firstAgain = new Params(one, "stop: alpha").alias("stop");
        String other = new Params(two, "stop: alpha").alias("stop");

        assertThat(firstAgain).isEqualTo(first);
        assertThat(other).isNotEqualTo(first);
    }

    @Test
    void decodesAnAliasBackToItsFriendlyName() {
        DslContext context = new DslContext();
        String resolved = new Params(context, "stop: alpha").alias("stop");

        assertThat(context.decodeAlias(resolved)).isEqualTo("alpha");
    }
}
