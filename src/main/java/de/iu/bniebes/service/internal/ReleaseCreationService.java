package de.iu.bniebes.service.internal;

import static de.iu.bniebes.constant.GlobalConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.model.request.ReleaseOptionalInformation;
import de.iu.bniebes.model.response.ReleaseCreateResponse;
import de.iu.bniebes.model.result.CreateOrUpdateResult;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import de.iu.bniebes.util.TimestampUtils;
import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseCreationService {

    private record CreatedRelease(BigInteger id, String json) {}

    private final ReleaseDBService releaseDBService;
    private final ReleaseOptInfoDBService releaseOptInfoDBService;
    private final InputSanitizationService inputSanitizationService;

    private final ObjectMapper mapper = new ObjectMapper();

    public Result<String> create(final String app, final String env, final String version) {
        final var createdRelease = createRelease(app, env, version, Instant.now());
        if (createdRelease.notPresent()) return Result.error();

        return Result.of(createdRelease.get().json());
    }

    public Result<CreateOrUpdateResult> createOrUpdate(
            final String app, final String env, final String ver, final BigInteger zeu, final String optionalInfo) {
        final var zeuInstant = TimestampUtils.instantOf(zeu);
        final var maybeRelease = releaseDBService.release(app, env, ver, zeuInstant);
        if (maybeRelease.isError()) return Result.error();

        var created = false;
        var json = "{}";

        final BigInteger id;
        if (maybeRelease.isEmpty()) {
            final var maybeCreatedRelease = createRelease(app, env, ver, zeuInstant);
            if (maybeCreatedRelease.notPresent()) return Result.error();

            created = true;
            json = maybeCreatedRelease.get().json();
            id = maybeCreatedRelease.get().id();
        } else {
            id = maybeRelease.get().id();
        }

        if (optionalInfo.isBlank()) return Result.of(new CreateOrUpdateResult(json, created));

        try (final var vtx = Executors.newVirtualThreadPerTaskExecutor()) {
            final var optionalInformation = mapper.readValue(optionalInfo, ReleaseOptionalInformation.class);
            if (optionalInformation.allNull()) return Result.of(new CreateOrUpdateResult(json, created));

            optionalInformation
                    .releaseNameOrEmpty()
                    .flatMap(inputSanitizationService::safeString)
                    .map(name -> vtx.submit(() -> releaseOptInfoDBService.insertReleaseName(id, name)));
            optionalInformation
                    .descriptionOrEmpty()
                    .flatMap(inputSanitizationService::safeString)
                    .map(desc -> vtx.submit(() -> releaseOptInfoDBService.insertDescription(id, desc)));
            optionalInformation
                    .changesOrEmpty()
                    .flatMap(inputSanitizationService::safeString)
                    .map(changes -> vtx.submit(() -> releaseOptInfoDBService.insertChanges(id, changes)));
            optionalInformation
                    .responsibilityOrEmpty()
                    .flatMap(inputSanitizationService::safeString)
                    .map(resp -> vtx.submit(() -> releaseOptInfoDBService.insertResponsibility(id, resp)));
            optionalInformation
                    .buildLocationOrEmpty()
                    .flatMap(inputSanitizationService::safeString)
                    .map(bl -> vtx.submit(() -> releaseOptInfoDBService.insertBuildLocation(id, bl)));

            vtx.shutdown();
            if (!vtx.awaitTermination(Timeouts.VTX, Timeouts.VTX_UNIT)) {
                log.atError()
                        .setMessage("Timeout exceeded while creating optional information")
                        .log();
                return Result.error();
            }

            return Result.of(new CreateOrUpdateResult(json, created));
        } catch (JsonProcessingException | InterruptedException ex) {
            log.atError().addMarker(Markers.SERVICE).setCause(ex).log();
            return Result.error();
        }
    }

    private Result<CreatedRelease> createRelease(
            final String app, final String env, final String ver, final Instant instant) {
        try {
            final var maybeId = releaseDBService.insert(app, env, ver, instant);
            if (maybeId.notPresent()) return Result.error();

            return Result.of(new CreatedRelease(
                    maybeId.get(), mapper.writeValueAsString(ReleaseCreateResponse.of(app, env, ver, instant))));
        } catch (DateTimeException | JsonProcessingException ex) {
            log.atError().addMarker(Markers.SERVICE).setCause(ex).log();
            return Result.error();
        }
    }
}
