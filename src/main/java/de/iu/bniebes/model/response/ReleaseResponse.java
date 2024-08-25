package de.iu.bniebes.model.response;

import static de.iu.bniebes.util.TimestampUtils.zuluEpochNanosOf;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.iu.bniebes.model.db.*;
import de.iu.bniebes.model.result.Result;
import java.math.BigInteger;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ReleaseResponse(
        String application,
        String environment,
        String version,
        BigInteger zuluEpochNanos,
        String releaseName,
        String description,
        String changes,
        String responsibility,
        String buildLocation) {

    public static ReleaseResponse of(
            final Release release,
            final Result<ReleaseName> releaseNameResult,
            final Result<Description> descriptionResult,
            final Result<Changes> changesResult,
            final Result<Responsibility> responsibilityResult,
            final Result<BuildLocation> buildLocationResult) {

        return new ReleaseResponse(
                release.application(),
                release.environment(),
                release.version(),
                zuluEpochNanosOf(release.releaseTimestamp()),
                releaseNameResult.isPresent() ? releaseNameResult.get().name() : "",
                descriptionResult.isPresent() ? descriptionResult.get().description() : "",
                changesResult.isPresent() ? changesResult.get().changes() : "",
                responsibilityResult.isPresent() ? responsibilityResult.get().name() : "",
                buildLocationResult.isPresent() ? buildLocationResult.get().name() : "");
    }

    public static ReleaseResponse of(final FullRelease fullRelease) {
        return new ReleaseResponse(
                fullRelease.application(),
                fullRelease.environment(),
                fullRelease.version(),
                zuluEpochNanosOf(fullRelease.releaseTimestamp()),
                valueOrBlank(fullRelease.releaseName()),
                valueOrBlank(fullRelease.description()),
                valueOrBlank(fullRelease.changes()),
                valueOrBlank(fullRelease.responsibility()),
                valueOrBlank(fullRelease.buildLocation()));
    }

    private static String valueOrBlank(final String value) {
        return Objects.nonNull(value) ? value : "";
    }
}
