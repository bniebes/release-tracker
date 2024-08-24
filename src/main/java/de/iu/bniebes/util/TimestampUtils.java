package de.iu.bniebes.util;

import java.math.BigInteger;
import java.time.Instant;

public class TimestampUtils {

    private static final BigInteger SECONDS_TO_NANOS_MULTIPLIER = BigInteger.valueOf(1_000_000_000L);

    private TimestampUtils() {}

    public static Instant instantOf(final BigInteger zuluEpochNanos) {
        return Instant.ofEpochSecond(
                zuluEpochNanos.divide(SECONDS_TO_NANOS_MULTIPLIER).longValue(),
                zuluEpochNanos.mod(SECONDS_TO_NANOS_MULTIPLIER).longValue());
    }

    public static BigInteger zuluEpochNanosOf(final Instant instant) {
        return BigInteger.valueOf(instant.getEpochSecond())
                .multiply(SECONDS_TO_NANOS_MULTIPLIER)
                .add(BigInteger.valueOf(instant.getNano()));
    }
}
