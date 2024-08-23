package de.iu.bniebes.service.internal;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InputSanitizationService {

    private final Predicate<String> safeStringPattern =
            Pattern.compile("^[\\w |.;,\\-]+$").asPredicate();
    private final Predicate<String> safeTextPattern =
            Pattern.compile("^[\\w\\s |.:,;!?$%#+*/()\\\\-]+$").asPredicate();

    public Optional<String> safeString(final String input) {
        if (isNullOrBlank(input)) return Optional.empty();
        return safeStringPattern.test(input) ? Optional.of(input) : Optional.empty();
    }

    public Optional<String> safeStringWithinLength(final String input, int maxLength) {
        if (isNullOrBlank(input) || input.length() > maxLength) return Optional.empty();
        return safeString(input);
    }

    public Optional<String> safeText(final String input) {
        if (isNullOrBlank(input)) return Optional.empty();
        return safeTextPattern.test(input) ? Optional.of(input) : Optional.empty();
    }

    public Optional<Instant> offsetDateTime(final String input) {
        if (isNullOrBlank(input)) return Optional.empty();
        try {
            return Optional.of(OffsetDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant());
        } catch (DateTimeParseException dtpEx) {
            return Optional.empty();
        }
    }

    private boolean isNullOrBlank(final String input) {
        return Objects.isNull(input) || input.isBlank();
    }
}
