package de.iu.bniebes.application;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EnvironmentAccessorTest {

    private static final String KEY_STRING = "test-string-value";
    private static final String VALUE_STRING = "test";

    private static final String KEY_INT = "test-int-value";
    private static final int VALUE_INT = 12345;

    private static final String KEY_LONG = "test-long-value";
    private static final long VALUE_LONG = 12345L;

    private static final Map<String, String> ENV_MAP = Map.ofEntries(
            Map.entry(KEY_STRING, VALUE_STRING),
            Map.entry(KEY_INT, String.valueOf(VALUE_INT)),
            Map.entry(KEY_LONG, String.valueOf(VALUE_LONG)));

    private final EnvironmentAccessor environmentAccessor = new EnvironmentAccessor(ENV_MAP);

    @Nested
    class GetRequiredTests {

        @Test
        void getRequiredString() {
            final var result = environmentAccessor.getRequired(KEY_STRING);
            assertEquals(VALUE_STRING, result);
        }

        @Test
        void getRequired_Int() {
            final var result = environmentAccessor.getRequired(KEY_STRING);
            assertEquals(VALUE_STRING, result);
        }

        @Test
        void getRequiredInt_BadValue() {
            final var result = environmentAccessor.getRequired(KEY_STRING);
            assertEquals(VALUE_STRING, result);
        }

        @Test
        void getRequiredLong() {
            final var result = environmentAccessor.getRequired(KEY_LONG, EnvironmentAccessor::mapToLong);
            assertEquals(VALUE_LONG, result);
        }

        @Test
        void getRequiredLong_BadValue() {
            assertThrows(
                    IllegalStateException.class,
                    () -> environmentAccessor.getRequired(KEY_STRING, EnvironmentAccessor::mapToLong));
        }

        @Test
        void getRequired_ValueNotSet() {
            assertThrows(IllegalStateException.class, () -> environmentAccessor.getRequired("throw"));
        }

        @Test
        void getRequired_BadArguments() {
            assertThrows(IllegalArgumentException.class, () -> environmentAccessor.getRequired(null));
            assertThrows(IllegalArgumentException.class, () -> environmentAccessor.getRequired(""));
        }
    }

    @Nested
    class getOrDefaultTests {

        private static final String DEFAULT_KEY_STRING = "default";
        private static final String DEFAULT_VALUE_STRING = "default";

        @Test
        void getOrDefault_ValueFromEnv() {
            final var result = environmentAccessor.getOrDefault(KEY_STRING, DEFAULT_VALUE_STRING);
            assertEquals(VALUE_STRING, result);
        }

        @Test
        void getOrDefault_DefaultValue() {
            final var result = environmentAccessor.getOrDefault(DEFAULT_KEY_STRING, DEFAULT_VALUE_STRING);
            assertEquals(DEFAULT_VALUE_STRING, result);
        }

        @Test
        void getOrDefault_ValueFromEnv_Int() {
            final var result =
                    environmentAccessor.getOrDefault(KEY_INT, Integer.MAX_VALUE, EnvironmentAccessor::mapToInt);
            assertEquals(VALUE_INT, result);
        }

        @Test
        void getOrDefault_DefaultValue_Int() {
            final var result =
                    environmentAccessor.getOrDefault("default-int", Integer.MAX_VALUE, EnvironmentAccessor::mapToInt);
            assertEquals(Integer.MAX_VALUE, result);
        }
    }

    @Nested
    class MapToIntTests {

        @Test
        void mapToInt() {
            final var result = EnvironmentAccessor.mapToInt(String.valueOf(Integer.MAX_VALUE));
            assertEquals(Integer.MAX_VALUE, result);
        }

        @Test
        void mapToInt_BadValue() {
            assertThrows(IllegalStateException.class, () -> EnvironmentAccessor.mapToInt("test"));
        }
    }

    @Nested
    class MapToLongTests {

        @Test
        void mapToLong() {
            final var result = EnvironmentAccessor.mapToLong(String.valueOf(Long.MAX_VALUE));
            assertEquals(Long.MAX_VALUE, result);
        }

        @Test
        void mapToLong_BadValue() {
            assertThrows(IllegalStateException.class, () -> EnvironmentAccessor.mapToLong("test"));
        }
    }

    @Nested
    class LoadSecretTests {

        private final Map<String, String> secretEnvMap = new HashMap<>();

        private final EnvironmentAccessor secretEnvironmentAccessor = new EnvironmentAccessor(secretEnvMap);

        @Test
        void loadSecret_WithKey() throws IOException {
            final var envKey = "test.secret.key";
            final var secret = "test";
            final var testFile = Files.createTempFile("test_secret", ".tmp");
            Files.writeString(testFile, secret);
            secretEnvMap.put(envKey, testFile.toString());

            final var result = assertDoesNotThrow(() -> secretEnvironmentAccessor.loadSecret(envKey, "unused"));
            assertNotNull(result);
            assertEquals(secret, result);

            assertTrue(Files.deleteIfExists(testFile));
        }

        @Test
        void loadSecret_DefaultValue() throws IOException {
            final var secret = "test";
            final var testFile = Files.createTempFile("test_secret", ".tmp");
            Files.writeString(testFile, secret);

            final var result =
                    assertDoesNotThrow(() -> secretEnvironmentAccessor.loadSecret("unused", testFile.toString()));
            assertNotNull(result);
            assertEquals(secret, result);

            assertTrue(Files.deleteIfExists(testFile));
        }

        @Test
        void loadSecret_BadPath() {
            assertThrows(
                    IllegalStateException.class, () -> secretEnvironmentAccessor.loadSecret("unused", "/tmp/test"));
        }
    }
}
