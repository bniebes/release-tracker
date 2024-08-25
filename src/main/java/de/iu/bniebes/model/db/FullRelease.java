package de.iu.bniebes.model.db;

import java.math.BigInteger;
import java.time.Instant;

public record FullRelease(
        BigInteger id,
        String application,
        String environment,
        String version,
        Instant releaseTimestamp,
        String releaseName,
        String description,
        String changes,
        String responsibility,
        String buildLocation) {}
