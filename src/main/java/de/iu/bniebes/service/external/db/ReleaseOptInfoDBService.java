package de.iu.bniebes.service.external.db;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.db.*;
import de.iu.bniebes.model.result.Result;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
@RequiredArgsConstructor
public class ReleaseOptInfoDBService {

    private static final String TABLE_RELEASE_NAME = "release_names";
    private static final String COLUMN_RELEASE_NAME = "name";
    private static final String TABLE_DESCRIPTION = "descriptions";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String TABLE_CHANGES = "changes";
    private static final String COLUMN_CHANGES = "changes";
    private static final String TABLE_RESPONSIBILITY = "responsibility";
    private static final String COLUMN_RESPONSIBILITY = "responsibility";
    private static final String TABLE_BUILD_LOCATION = "build_location";
    private static final String COLUMN_BUILD_LOCATION = "build_location";

    private final Jdbi jdbi;

    public boolean insertReleaseName(final BigInteger releaseId, final String name) {
        return insertOptInfo(releaseId, TABLE_RELEASE_NAME, COLUMN_RELEASE_NAME, name);
    }

    public Result<ReleaseName> releaseNameById(final BigInteger releaseId) {
        return queryByReleaseId(releaseId, TABLE_RELEASE_NAME, COLUMN_RELEASE_NAME, ReleaseName::new);
    }

    public boolean insertDescription(final BigInteger releaseId, final String description) {
        return insertOptInfo(releaseId, TABLE_DESCRIPTION, COLUMN_DESCRIPTION, description);
    }

    public Result<Description> descriptionById(final BigInteger releaseId) {
        return queryByReleaseId(releaseId, TABLE_DESCRIPTION, COLUMN_DESCRIPTION, Description::new);
    }

    public boolean insertChanges(final BigInteger releaseId, final String changes) {
        return insertOptInfo(releaseId, TABLE_CHANGES, COLUMN_CHANGES, changes);
    }

    public Result<Changes> changesById(final BigInteger releaseId) {
        return queryByReleaseId(releaseId, TABLE_CHANGES, COLUMN_CHANGES, Changes::new);
    }

    public boolean insertResponsibility(final BigInteger releaseId, final String responsibility) {
        return insertOptInfo(releaseId, TABLE_RESPONSIBILITY, COLUMN_RESPONSIBILITY, responsibility);
    }

    public Result<Responsibility> responsibilityById(final BigInteger releaseId) {
        return queryByReleaseId(releaseId, TABLE_RESPONSIBILITY, COLUMN_RESPONSIBILITY, Responsibility::new);
    }

    public boolean insertBuildLocation(final BigInteger releaseId, final String buildLocation) {
        return insertOptInfo(releaseId, TABLE_BUILD_LOCATION, COLUMN_BUILD_LOCATION, buildLocation);
    }

    public Result<BuildLocation> buildLocationById(final BigInteger releaseId) {
        return queryByReleaseId(releaseId, TABLE_BUILD_LOCATION, COLUMN_BUILD_LOCATION, BuildLocation::new);
    }

    private boolean insertOptInfo(
            final BigInteger id, final String table, final String columnName, final String value) {
        try (final var handle = jdbi.open()) {
            final var insertStatement =
                    """
                    INSERT INTO %s(release_id, %s) VALUES (:release_id, :value)
                    ON CONFLICT (release_id) DO UPDATE SET %s = EXCLUDED.%s;
                    """;
            final var result = handle.createUpdate(insertStatement.formatted(table, columnName, columnName, columnName))
                    .bind("release_id", new BigDecimal(id))
                    .bind("value", value)
                    .execute();
            return result == 1;
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not insert into {}")
                    .addArgument(table)
                    .setCause(ex)
                    .log();
            return false;
        }
    }

    private <T> Result<T> queryByReleaseId(
            final BigInteger id,
            final String table,
            final String columnName,
            final BiFunction<BigInteger, String, T> mappingFn) {
        try (final var handle = jdbi.open()) {
            return handle.createQuery("SELECT * FROM %s WHERE release_id = :id;".formatted(table))
                    .bind("id", new BigDecimal(id))
                    .map((rs, ctx) ->
                            mappingFn.apply(rs.getBigDecimal("release_id").toBigInteger(), rs.getString(columnName)))
                    .findOne()
                    .map(Result::of)
                    .orElse(Result.empty());
        } catch (Exception ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.DB)
                    .setMessage("Could not query a release name by release id: {}")
                    .addArgument(id)
                    .log();
            return Result.error();
        }
    }
}
