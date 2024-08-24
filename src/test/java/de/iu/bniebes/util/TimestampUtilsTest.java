package de.iu.bniebes.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimestampUtilsTest {

    @Test
    void test() {
        final var instant = Instant.now();
        final var zuluEpochNanos = TimestampUtils.zuluEpochNanosOf(instant);
        final var result = TimestampUtils.instantOf(zuluEpochNanos);
        assertEquals(instant, result);
    }
}
