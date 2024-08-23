package de.iu.bniebes.service.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class InputSanitizationServiceTest {

    private final InputSanitizationService inputSanitizationService = new InputSanitizationService();

    @Nested
    class SafeStringTests {

        @Test
        void safeString() {
            final var validTestString = "release-tracker";
            final var result = inputSanitizationService.safeString(validTestString);
            assertTrue(result.isPresent());
            assertEquals(validTestString, result.get());
        }

        @Test
        void safeString_AllAllowedCharacters() {
            final var validTestString = "a1Z |.;,-";
            final var result = inputSanitizationService.safeString(validTestString);
            assertTrue(result.isPresent());
            assertEquals(validTestString, result.get());
        }

        @Test
        void safeString_InvalidCharacter() {
            final var result = inputSanitizationService.safeString("<");
            assertTrue(result.isEmpty());
        }

        @Test
        void safeString_NullOrBlank() {
            final var resultNull = inputSanitizationService.safeString(null);
            assertTrue(resultNull.isEmpty());

            final var resultEmpty = inputSanitizationService.safeString("");
            assertTrue(resultEmpty.isEmpty());

            final var resultBlank = inputSanitizationService.safeString(" ");
            assertTrue(resultBlank.isEmpty());
        }
    }

    @Nested
    class SafeStringWithinLengthTests {

        @Test
        void safeStringWithinLength() {
            final var validTestString = "release-tracker";
            final var result =
                    inputSanitizationService.safeStringWithinLength(validTestString, validTestString.length());
            assertTrue(result.isPresent());
            assertEquals(validTestString, result.get());
        }

        @Test
        void safeStringWithinLength_ExceedsLength() {
            final var invalidTestString = "release-tracker-name-too-long";
            final var result = inputSanitizationService.safeStringWithinLength(invalidTestString, 10);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class SafeTextTests {

        @Test
        void safeText_AllAllowedCharacters() {
            final var validText = """
                    This is a test text!
                    
                    The following characters should be allowed: |.:,;!?$%#+*/()-\\
                    A tab \t should also be allowed
                    """;
            final var result = inputSanitizationService.safeText(validText);
            assertTrue(result.isPresent());
            assertEquals(validText, result.get());
        }

        @Test
        void safeText_InvalidCharacters() {
            final var invalidText = """
                    This is a test text!
                    
                    The following characters should be allowed: |.:,;!?$%#+*/()-\\
                    A tab \t should also be allowed
                    Curly Braces are not allowed {}
                    """;
            final var result = inputSanitizationService.safeText(invalidText);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class OffsetDateTimeTests {
        @Test
        void offsetDateTime() {
            final var offsetBerlin = ZoneOffset.of("+02:00");
            final var time = OffsetDateTime.of(2024, 8, 23, 16, 52, 0, 0, offsetBerlin).toInstant();

            final var resultFull = inputSanitizationService.offsetDateTime("2024-08-23T16:52:00.000+02:00");
            assertTrue(resultFull.isPresent());
            assertEquals(time, resultFull.get());

            final var resultShort = inputSanitizationService.offsetDateTime("2024-08-23T16:52+02:00");
            assertTrue(resultShort.isPresent());
            assertEquals(time, resultShort.get());
        }

        @Test
        void offsetDateTime_InvalidTimeString() {
            final var resultMissingOffset = inputSanitizationService.offsetDateTime("2024-08-23T16:52:25.000");
            assertTrue(resultMissingOffset.isEmpty());

            final var resultInvalidOffsetTime = inputSanitizationService.offsetDateTime("2024-08-T16:25+02:00");
            assertTrue(resultInvalidOffsetTime.isEmpty());

            final var resultEmpty = inputSanitizationService.offsetDateTime("");
            assertTrue(resultEmpty.isEmpty());

            final var resultBlank = inputSanitizationService.offsetDateTime(" ");
            assertTrue(resultBlank.isEmpty());
        }
    }
}
