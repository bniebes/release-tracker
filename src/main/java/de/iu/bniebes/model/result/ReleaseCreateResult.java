package de.iu.bniebes.model.result;

import static de.iu.bniebes.constant.GlobalConstants.Conversion.SECONDS_TO_NANOS_MULTIPLIER;

import java.math.BigInteger;
import java.time.Instant;

public record ReleaseCreateResult(String application, String environment, String version, BigInteger zuluEpochNanos) {

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
