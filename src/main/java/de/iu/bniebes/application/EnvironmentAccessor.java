package de.iu.bniebes.application;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnvironmentAccessor {

    private final Map<String, String> env;

    public static EnvironmentAccessor getEnvFromSystem() {
        return new EnvironmentAccessor(System.getenv());
    }

    public <T> T getRequired(final String key, final Function<String, T> map)
            throws IllegalArgumentException, IllegalStateException {
        if (Objects.isNull(key) || key.isBlank()) throw new IllegalArgumentException();
        final var entry = env.get(key);
        if (Objects.isNull(entry)) {
            throw new IllegalStateException("Required entry for key [%s] is null".formatted(key));
        }
        return map.apply(entry);
    }

    public String getRequired(final String key) {
        return getRequired(key, Function.identity());
    }

    public <T> T getOrDefault(final String key, T defaultValue, final Function<String, T> map)
            throws IllegalArgumentException, IllegalStateException {
        if (Objects.isNull(key) || key.isBlank() || Objects.isNull(defaultValue)) throw new IllegalArgumentException();
        final var entry = env.get(key);
        return Objects.nonNull(entry) ? map.apply(entry) : defaultValue;
    }

    public String getOrDefault(final String key, String defaultValue) {
        return getOrDefault(key, defaultValue, Function.identity());
    }

    public static int mapToInt(final String entry) {
        try {
            return Integer.parseInt(entry);
        } catch (NumberFormatException nfEx) {
            throw new IllegalStateException("Can not convert %s to int".formatted(entry), nfEx);
        }
    }

    public static long mapToLong(final String entry) {
        try {
            return Long.parseLong(entry);
        } catch (NumberFormatException nfEx) {
            throw new IllegalStateException("Can not convert %s to long".formatted(entry), nfEx);
        }
    }
}
