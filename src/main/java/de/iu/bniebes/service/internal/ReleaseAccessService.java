package de.iu.bniebes.service.internal;

import static de.iu.bniebes.constant.GlobalConstants.*;
import static de.iu.bniebes.util.TimestampUtils.instantOf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.model.db.FullRelease;
import de.iu.bniebes.model.response.ReleaseResponse;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseAccessService {

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
            if (!vtx.awaitTermination(Timeouts.VTX, Timeouts.VTX_UNIT)) {
                log.atError()
                        .addMarker(Markers.SERVICE)
                        .setMessage("Timeout exceeded fetching optional release information")
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
            log.atError().addMarker(Markers.SERVICE).setCause(ex).log();
            return Result.error();
        }
    }

    public Result<String> all() {
        final var maybeFullReleases = releaseDBService.fullReleases();
        if (maybeFullReleases.isEmpty()) return Result.empty();
        if (maybeFullReleases.isError()) return Result.error();

        return toJson(maybeFullReleases.get());
    }

    public Result<String> allByApplication(final String application) {
        final var maybeFullReleases = releaseDBService.fullReleasesByApplication(application);
        if (maybeFullReleases.isEmpty()) return Result.empty();
        if (maybeFullReleases.isError()) return Result.error();

        return toJson(maybeFullReleases.get());
    }

    public Result<String> allByApplicationAndEnvironment(final String application, final String environment) {
        final var maybeFullReleases =
                releaseDBService.fullReleasesByApplicationAndEnvironment(application, environment);
        if (maybeFullReleases.isEmpty()) return Result.empty();
        if (maybeFullReleases.isError()) return Result.error();

        return toJson(maybeFullReleases.get());
    }

    public Result<String> currentByApplication(final String application) {
        final var maybeFullRelease = releaseDBService.currentReleaseByApplication(application);
        if (maybeFullRelease.isEmpty()) return Result.empty();
        if (maybeFullRelease.isError()) return Result.error();

        return toJson(maybeFullRelease.get());
    }

    public Result<String> currentByApplicationAndEnvironment(final String application, final String environment) {
        final var maybeFullRelease =
                releaseDBService.currentReleaseByApplicationAndEnvironment(application, environment);
        if (maybeFullRelease.isEmpty()) return Result.empty();
        if (maybeFullRelease.isError()) return Result.error();

        return toJson(maybeFullRelease.get());
    }

    private Result<String> toJson(final Set<FullRelease> fullReleases) {
        if (fullReleases.isEmpty()) return Result.empty();
        try {
            final var releaseResponses =
                    fullReleases.stream().map(ReleaseResponse::of).collect(Collectors.toSet());
            return Result.of(mapper.writeValueAsString(releaseResponses));
        } catch (JsonProcessingException jpEx) {
            log.atError().addMarker(Markers.SERVICE).setCause(jpEx).log();
            return Result.error();
        }
    }

    private Result<String> toJson(final FullRelease fullRelease) {
        try {
            return Result.of(mapper.writeValueAsString(ReleaseResponse.of(fullRelease)));
        } catch (JsonProcessingException jpEx) {
            log.atError().addMarker(Markers.SERVICE).setCause(jpEx).log();
            return Result.error();
        }
    }
}
