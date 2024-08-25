package de.iu.bniebes.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class TimestampUtilsTest {

    @Test
    void test() {
        final var instant = Instant.now();
        final var zuluEpochMicros = TimestampUtils.zuluEpochMicrosOf(instant);
        final var result = TimestampUtils.instantOf(zuluEpochMicros);
        assertEquals(instant.truncatedTo(ChronoUnit.MICROS), result);
    }
}
