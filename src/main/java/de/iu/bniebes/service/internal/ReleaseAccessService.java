package de.iu.bniebes.service.internal;

import static de.iu.bniebes.util.TimestampUtils.instantOf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.response.ReleaseResponse;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseAccessService {

    private static final long TIMEOUT = 30;
    private static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

    private final ReleaseDBService releaseDBService;
    private final ReleaseOptInfoDBService releaseOptInfoDBService;

    private final ObjectMapper mapper = new ObjectMapper();

    public Result<String> get(
            final String application, final String environment, final String version, final BigInteger zuluEpochNanos) {

        final var maybeRelease = releaseDBService.release(application, environment, version, instantOf(zuluEpochNanos));
        if (maybeRelease.notPresent()) return maybeRelease.isEmpty() ? Result.empty() : Result.error();
        final var release = maybeRelease.get();
        final var releaseId = release.id();

        try (final var vtx = Executors.newVirtualThreadPerTaskExecutor()) {
            final var futureReleaseName = vtx.submit(() -> releaseOptInfoDBService.releaseNameById(releaseId));
            final var futureDescription = vtx.submit(() -> releaseOptInfoDBService.descriptionById(releaseId));
            final var futureChanges = vtx.submit(() -> releaseOptInfoDBService.changesById(releaseId));
            final var futureResponsibility = vtx.submit(() -> releaseOptInfoDBService.responsibilityById(releaseId));
            final var futureBuildLocation = vtx.submit(() -> releaseOptInfoDBService.buildLocationById(releaseId));

            vtx.shutdown();
            if (!vtx.awaitTermination(TIMEOUT, TIMEOUT_TIME_UNIT)) {
                log.atError()
                        .addMarker(GlobalConstants.Markers.SERVICE)
                        .setMessage("Timeout ({} {}) exceeded fetching optional release information")
                        .addArgument(TIMEOUT)
                        .addArgument(TIMEOUT_TIME_UNIT)
                        .log();
                return Result.error();
            }

            final var releaseResponse = ReleaseResponse.of(
                    release,
                    futureReleaseName.resultNow(),
                    futureDescription.resultNow(),
                    futureChanges.resultNow(),
                    futureResponsibility.resultNow(),
                    futureBuildLocation.resultNow());

            return Result.of(mapper.writeValueAsString(releaseResponse));
        } catch (InterruptedException | JsonProcessingException ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.SERVICE)
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }
}
