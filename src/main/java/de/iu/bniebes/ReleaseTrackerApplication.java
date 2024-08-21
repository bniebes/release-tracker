package de.iu.bniebes;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.application.Configuration;
import de.iu.bniebes.application.EnvironmentAccessor;
import de.iu.bniebes.application.Services;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReleaseTrackerApplication implements AutoCloseable {

    private final Services services;

    public ReleaseTrackerApplication() {
        final var configuration = new Configuration(EnvironmentAccessor.getEnvFromSystem());
        this.services = new Services(configuration);
    }

    public void run() {}

    @Override
    public void close() throws Exception {
        services.close();
    }

    public static void main(String[] args) {
        log.atInfo()
                .addMarker(Markers.APPLICATION)
                .setMessage("Start ReleaseTrackerApplication")
                .log();
        try (final var app = new ReleaseTrackerApplication()) {
            app.run();
        } catch (Exception ex) {
            log.atError()
                    .addMarker(Markers.APPLICATION)
                    .setMessage("Unexpected exception")
                    .setCause(ex)
                    .log();
        }
    }
}
