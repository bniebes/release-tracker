package de.iu.bniebes.model.db;

import java.math.BigInteger;
import java.time.Instant;

public record Release(
        String application, String environment, String version, Instant releaseTimestamp, BigInteger id) {}
