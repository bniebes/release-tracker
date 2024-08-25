package de.iu.bniebes.util;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimestampUtils {

    private static final BigInteger SECONDS_TO_MICROS_MULTIPLIER = BigInteger.valueOf(1_000_000L);
    private static final BigInteger MICROS_TO_NANOS_MULTIPLIER = BigInteger.valueOf(1000L);

    private TimestampUtils() {}

    public static Instant instantOf(final BigInteger zuluEpochMicros) {
        return Instant.ofEpochSecond(
                zuluEpochMicros.divide(SECONDS_TO_MICROS_MULTIPLIER).longValue(),
                zuluEpochMicros
                        .mod(SECONDS_TO_MICROS_MULTIPLIER)
                        .multiply(MICROS_TO_NANOS_MULTIPLIER)
                        .longValue());
    }

    public static BigInteger zuluEpochMicrosOf(final Instant instant) {
        final var truncatedInstant = instant.truncatedTo(ChronoUnit.MICROS);
        return BigInteger.valueOf(truncatedInstant.getEpochSecond())
                .multiply(SECONDS_TO_MICROS_MULTIPLIER)
                .add(BigInteger.valueOf(truncatedInstant.getNano()).divide(MICROS_TO_NANOS_MULTIPLIER));
    }
}
