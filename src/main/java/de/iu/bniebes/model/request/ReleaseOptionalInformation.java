package de.iu.bniebes.model.request;

import java.util.Objects;
import java.util.Optional;

public record ReleaseOptionalInformation(
        String releaseName, String description, String changes, String responsibility, String buildLocation) {

    public boolean allNull() {
        return Objects.isNull(releaseName)
                && Objects.isNull(description)
                && Objects.isNull(changes)
                && Objects.isNull(responsibility)
                && Objects.isNull(buildLocation);
    }

    public Optional<String> releaseNameOrEmpty() {
        return Optional.ofNullable(releaseName);
    }

    public Optional<String> descriptionOrEmpty() {
        return Optional.ofNullable(description);
    }

    public Optional<String> changesOrEmpty() {
        return Optional.ofNullable(changes);
    }

    public Optional<String> responsibilityOrEmpty() {
        return Optional.ofNullable(responsibility);
    }

    public Optional<String> buildLocationOrEmpty() {
        return Optional.ofNullable(buildLocation);
    }
}
