package de.iu.bniebes;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.application.Configuration;
import de.iu.bniebes.application.EnvironmentAccessor;
import de.iu.bniebes.application.HttpServices;
import de.iu.bniebes.application.Services;
import io.helidon.webserver.WebServer;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReleaseTrackerApplication implements AutoCloseable {

    private final int port;
    private final Services services;
    private final HttpServices httpServices;

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public ReleaseTrackerApplication() {
        final var configuration = new Configuration(EnvironmentAccessor.getEnvFromSystem());
        this.port = configuration.webServerConfiguration.port();
        this.services = new Services(configuration);
        this.httpServices = new HttpServices(services);
    }

    public void run() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(this::onShutdown));
        WebServer.builder()
                .port(port)
                .routing(routing -> routing.get("/", (req, res) -> res.send("Release Tracker"))
                        .get("/health", (req, res) -> res.send("OK"))
                        .register("/v1/release", httpServices.releaseHttpServiceV1)
                        .register("/v1/current", httpServices.currentHttpServiceV1)
                        .register("/v1/util", httpServices.utilHttpServiceV1))
                .build()
                .start();
        shutdownLatch.await();
    }

    public void onShutdown() {
        shutdownLatch.countDown();
    }

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
