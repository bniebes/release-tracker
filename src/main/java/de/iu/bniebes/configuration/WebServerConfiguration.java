package de.iu.bniebes.configuration;

import de.iu.bniebes.application.EnvironmentAccessor;

public record WebServerConfiguration(int port) {

    public static final String KEY_PORT = "webserver.port";
    public static final int DEFAULT_PORT = 30123;

    public static WebServerConfiguration from(final EnvironmentAccessor environmentAccessor) {
        return new WebServerConfiguration(
                environmentAccessor.getOrDefault(KEY_PORT, DEFAULT_PORT, EnvironmentAccessor::mapToInt));
    }
}
