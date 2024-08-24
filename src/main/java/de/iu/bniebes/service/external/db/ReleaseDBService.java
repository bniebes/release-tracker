package de.iu.bniebes.service.external.db;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.db.Release;
import de.iu.bniebes.model.result.Result;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
@RequiredArgsConstructor
public class ReleaseDBService {

    private final Jdbi jdbi;

    public Result<Release> release(
            final String application, final String environment, final String version, final Instant releaseTimestamp) {
        try (final var handle = jdbi.open()) {
            final var query =
                    """
                    SELECT *
                    FROM releases
                    WHERE application = :app AND environment = :env AND version = :ver AND release_timestamp = :rts;
                    """;
            return handle.createQuery(query)
                    .bindMap(releaseBinds(application, environment, version, releaseTimestamp))
                    .map(this::toRelease)
                    .findOne()
                    .map(Result::of)
                    .orElseGet(Result::empty);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query a release")
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<Release> releaseById(final BigInteger id) {
        try (final var handle = jdbi.open()) {
            return handle.createQuery("SELECT * FROM releases WHERE id = :id;")
                    .bind("id", new BigDecimal(id))
                    .map(this::toRelease)
                    .findOne()
                    .map(Result::of)
                    .orElse(Result.empty());
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query a release by id {}")
                    .addArgument(id)
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<BigInteger> releaseId(
            final String application, final String environment, final String version, final Instant releaseTimestamp) {
        try (final var handle = jdbi.open()) {
            final var query =
                    """
                    SELECT id
                    FROM releases
                    WHERE application = :app AND environment = :env AND version = :ver AND release_timestamp = :rts;
                    """;
            return handle.createQuery(query)
                    .bindMap(releaseBinds(application, environment, version, releaseTimestamp))
                    .map((rs, ctx) -> rs.getBigDecimal("id").toBigInteger())
                    .findOne()
                    .map(Result::of)
                    .orElse(Result.empty());
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query a release id by [{}, {}, {}, {}]")
                    .addArgument(application)
                    .addArgument(environment)
                    .addArgument(version)
                    .addArgument(releaseTimestamp)
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<Set<Release>> releases(final String application, final String environment, final String version) {
        try (final var handle = jdbi.open()) {
            final var query =
                    """
                    SELECT *
                    FROM releases
                    WHERE application = :app AND environment = :env AND version = :ver;
                    """;
            final var result = handle.createQuery(query)
                    .bind("app", application)
                    .bind("env", environment)
                    .bind("ver", version)
                    .map(this::toRelease)
                    .collectIntoSet();
            return result.isEmpty() ? Result.empty() : Result.of(result);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query a releases by [{}, {}, {}]")
                    .addArgument(application)
                    .addArgument(environment)
                    .addArgument(version)
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<BigInteger> insert(
            final String application, final String environment, final String version, final Instant releaseTimestamp) {
        try (final var handle = jdbi.open()) {
            final var updateStatement =
                    """
                    INSERT INTO releases(application, environment, version, release_timestamp)
                    VALUES (:app, :env, :ver, :rts);
                    """;
            return handle.createUpdate(updateStatement)
                    .bindMap(releaseBinds(application, environment, version, releaseTimestamp))
                    .executeAndReturnGeneratedKeys("id")
                    .map((rs, ctx) -> rs.getBigDecimal("id").toBigInteger())
                    .findOne()
                    .map(Result::of)
                    .orElse(Result.empty());
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not insert release")
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    private Map<String, ?> releaseBinds(
            final String application, final String environment, final String version, final Instant releaseTimestamp) {
        return Map.ofEntries(
                Map.entry("app", application),
                Map.entry("env", environment),
                Map.entry("ver", version),
                Map.entry("rts", releaseTimestamp));
    }

    private Release toRelease(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new Release(
                rs.getString("application"),
                rs.getString("environment"),
                rs.getString("version"),
                rs.getTimestamp("release_timestamp").toInstant(),
                rs.getBigDecimal("id").toBigInteger());
    }
}
