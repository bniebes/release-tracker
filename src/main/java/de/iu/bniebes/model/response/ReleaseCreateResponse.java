package de.iu.bniebes.model.response;

import static de.iu.bniebes.util.TimestampUtils.zuluEpochMicrosOf;

import java.math.BigInteger;
import java.time.Instant;

public record ReleaseCreateResponse(
        String application, String environment, String version, BigInteger zuluEpochMicros) {

    public static ReleaseCreateResponse of(
            final String application, final String environment, final String version, final Instant timestamp) {
        return new ReleaseCreateResponse(application, environment, version, zuluEpochMicrosOf(timestamp));
    }
}
