package de.iu.bniebes.model.result;

import java.math.BigInteger;
import java.time.Instant;

public record ReleaseCreateResult(String application, String environment, String version, BigInteger zuluEpochNanos) {

    private static final BigInteger SECONDS_TO_NANOS_MULTIPLIER = BigInteger.valueOf(1_000_000_000L);

    public static ReleaseCreateResult of(
            final String application, final String environment, final String version, final Instant timestamp) {
        return new ReleaseCreateResult(
                application,
                environment,
                version,
                BigInteger.valueOf(timestamp.getEpochSecond())
                        .multiply(SECONDS_TO_NANOS_MULTIPLIER)
                        .add(BigInteger.valueOf(timestamp.getNano())));
    }
}
