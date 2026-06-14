package com.bytecrumbs.sg_land_transport_backend.busarrival.harness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.fail;

/**
 * Named, optionally-defaulted test parameters (Dave Farley's ATDD pattern). Tests pass
 * {@code "key: value"} tokens so they state only what's salient and stay stable as the model grows.
 *
 * <p>{@link #alias} resolves a friendly token to a process-unique value via {@link DslContext}, so
 * scenarios are isolated (parallel-safe) while reading in domain terms; {@link DslContext#decodeAlias}
 * maps it back for readable failure messages.
 */
public final class Params {

    private final DslContext context;
    private final String[] args;

    public Params(DslContext context, String... args) {
        this.context = context;
        this.args = args;
    }

    /** A required parameter resolved to its process-unique alias (for isolated test data). */
    public String alias(String name) {
        return context.alias(required(name));
    }

    /** A required parameter taken verbatim (for real domain values like a bus service number). */
    public String required(String name) {
        String value = valueOf(name);
        if (value == null) {
            fail("No '" + name + "' supplied");
        }
        return value;
    }

    public int requiredInt(String name) {
        return Integer.parseInt(required(name));
    }

    public String optional(String name, String defaultValue) {
        String value = valueOf(name);
        return value == null ? defaultValue : value;
    }

    private String valueOf(String name) {
        String prefix = name + ":";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    /**
     * Per-scenario aliasing: a friendly name resolves to a stable value within one scenario and a
     * distinct value across scenarios (a process-global sequence), so test data never collides.
     */
    public static final class DslContext {

        private static final Map<String, Integer> globalSequence = new HashMap<>();
        private final Map<String, String> aliases = new HashMap<>();

        public synchronized String alias(String name) {
            return aliases.computeIfAbsent(name, n -> n + nextGlobalSequence(n));
        }

        public String decodeAlias(String value) {
            return aliases.entrySet().stream()
                    .filter(e -> e.getValue().equals(value))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(value);
        }

        private static synchronized int nextGlobalSequence(String name) {
            int next = globalSequence.getOrDefault(name, 0) + 1;
            globalSequence.put(name, next);
            return next;
        }
    }
}
