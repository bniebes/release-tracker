package de.iu.bniebes.model.response;

import static de.iu.bniebes.util.TimestampUtils.zuluEpochNanosOf;

import java.math.BigInteger;
import java.time.Instant;

public record ReleaseCreateResponse(String application, String environment, String version, BigInteger zuluEpochNanos) {

    public static ReleaseCreateResponse of(
            final String application, final String environment, final String version, final Instant timestamp) {
        return new ReleaseCreateResponse(application, environment, version, zuluEpochNanosOf(timestamp));
    }
}
