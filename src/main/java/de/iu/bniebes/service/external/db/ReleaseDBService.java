package de.iu.bniebes.service.external.db;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.db.FullRelease;
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

    public Result<Set<FullRelease>> fullReleases() {
        final var query =
                """
                SELECT
                    id, application, environment, version, release_timestamp,
                    rn.name, d.description, c.changes, r.responsibility, bl.build_location
                FROM releases
                    LEFT JOIN release_names rn on releases.id = rn.release_id
                    LEFT JOIN descriptions d on releases.id = d.release_id
                    LEFT JOIN changes c on releases.id = c.release_id
                    LEFT JOIN responsibility r on c.release_id = r.release_id
                    LEFT JOIN build_location bl on releases.id = bl.release_id
                """;
        try (final var handle = jdbi.open()) {
            final var fullReleases =
                    handle.createQuery(query).map(this::toFullRelease).collectIntoSet();
            return fullReleases.isEmpty() ? Result.empty() : Result.of(fullReleases);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query fullReleases")
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<Set<FullRelease>> fullReleasesByApplication(final String application) {
        final var query =
                """
                SELECT
                    id, application, environment, version, release_timestamp,
                    rn.name, d.description, c.changes, r.responsibility, bl.build_location
                FROM releases
                    LEFT JOIN release_names rn on releases.id = rn.release_id
                    LEFT JOIN descriptions d on releases.id = d.release_id
                    LEFT JOIN changes c on releases.id = c.release_id
                    LEFT JOIN responsibility r on c.release_id = r.release_id
                    LEFT JOIN build_location bl on releases.id = bl.release_id
                WHERE application = :app;
                """;
        try (final var handle = jdbi.open()) {
            final var fullReleases = handle.createQuery(query)
                    .bind("app", application)
                    .map(this::toFullRelease)
                    .collectIntoSet();
            return fullReleases.isEmpty() ? Result.empty() : Result.of(fullReleases);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query fullReleases")
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<Set<FullRelease>> fullReleasesByApplicationAndEnvironment(
            final String application, final String environment) {
        final var query =
                """
                SELECT
                    id, application, environment, version, release_timestamp,
                    rn.name, d.description, c.changes, r.responsibility, bl.build_location
                FROM releases
                    LEFT JOIN release_names rn on releases.id = rn.release_id
                    LEFT JOIN descriptions d on releases.id = d.release_id
                    LEFT JOIN changes c on releases.id = c.release_id
                    LEFT JOIN responsibility r on c.release_id = r.release_id
                    LEFT JOIN build_location bl on releases.id = bl.release_id
                WHERE application = :app AND environment = :env;
                """;
        try (final var handle = jdbi.open()) {
            final var fullReleases = handle.createQuery(query)
                    .bind("app", application)
                    .bind("env", environment)
                    .map(this::toFullRelease)
                    .collectIntoSet();
            return fullReleases.isEmpty() ? Result.empty() : Result.of(fullReleases);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query fullReleases")
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<FullRelease> currentReleaseByApplication(final String application) {
        final var query =
                """
                SELECT
                    id, application, environment, version, release_timestamp,
                    rn.name, d.description, c.changes, r.responsibility, bl.build_location
                FROM releases
                         LEFT JOIN release_names rn on releases.id = rn.release_id
                         LEFT JOIN descriptions d on releases.id = d.release_id
                         LEFT JOIN changes c on releases.id = c.release_id
                         LEFT JOIN responsibility r on c.release_id = r.release_id
                         LEFT JOIN build_location bl on releases.id = bl.release_id
                WHERE application = :app
                ORDER BY release_timestamp DESC
                LIMIT 1;
                """;
        try (final var handle = jdbi.open()) {
            final var maybeFullRelease = handle.createQuery(query)
                    .bind("app", application)
                    .map(this::toFullRelease)
                    .findOne();
            return maybeFullRelease.map(Result::of).orElseGet(Result::empty);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query current release by app[{}]")
                    .addArgument(application)
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }

    public Result<FullRelease> currentReleaseByApplicationAndEnvironment(
            final String application, final String environment) {
        final var query =
                """
                SELECT
                    id, application, environment, version, release_timestamp,
                    rn.name, d.description, c.changes, r.responsibility, bl.build_location
                FROM releases
                         LEFT JOIN release_names rn on releases.id = rn.release_id
                         LEFT JOIN descriptions d on releases.id = d.release_id
                         LEFT JOIN changes c on releases.id = c.release_id
                         LEFT JOIN responsibility r on c.release_id = r.release_id
                         LEFT JOIN build_location bl on releases.id = bl.release_id
                WHERE application = :app AND environment = :env
                ORDER BY release_timestamp DESC
                LIMIT 1;
                """;
        try (final var handle = jdbi.open()) {
            final var maybeFullRelease = handle.createQuery(query)
                    .bind("app", application)
                    .bind("env", environment)
                    .map(this::toFullRelease)
                    .findOne();
            return maybeFullRelease.map(Result::of).orElseGet(Result::empty);
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query current release by app[{}] and env[{}]")
                    .addArgument(application)
                    .addArgument(environment)
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

    private FullRelease toFullRelease(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new FullRelease(
                rs.getBigDecimal("id").toBigInteger(),
                rs.getString("application"),
                rs.getString("environment"),
                rs.getString("version"),
                rs.getTimestamp("release_timestamp").toInstant(),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("changes"),
                rs.getString("responsibility"),
                rs.getString("build_location"));
    }
}
