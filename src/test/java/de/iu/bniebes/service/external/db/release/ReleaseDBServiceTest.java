package de.iu.bniebes.service.external.db.release;

import static org.junit.jupiter.api.Assertions.*;

import com.zaxxer.hikari.HikariDataSource;
import de.iu.bniebes.configuration.DBConfiguration;
import de.iu.bniebes.constant.GlobalConstants;
import java.math.BigInteger;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@Slf4j
@EnabledIfSystemProperty(named = "test.condition.pgsql", matches = "true")
class ReleaseDBServiceTest {

    private static final Jdbi jdbi = createTestJdbi();

    private static final String TEST_APPLICATION = "test";
    private static final String TEST_ENVIRONMENT = "test";
    private static final String TEST_VERSION = "1.0.0";
    private static final Instant TEST_TIMESTAMP = Instant.now();

    private final ReleaseDBService releaseDBService = new ReleaseDBService(jdbi);

    @BeforeAll
    public static void init() {
        insertTestRelease();
    }

    @Test
    void release() {
        final var result = assertDoesNotThrow(
                () -> releaseDBService.release(TEST_APPLICATION, TEST_ENVIRONMENT, TEST_VERSION, TEST_TIMESTAMP));
        assertTrue(result.isPresent());
        infoLogResult(result.get());
    }

    @Test
    void releaseById() {
        final var result = assertDoesNotThrow(() -> releaseDBService.releaseById(BigInteger.ONE));
        assertTrue(result.isPresent());
        infoLogResult(result.get());
    }

    @Test
    void releaseId() {
        final var result = assertDoesNotThrow(
                () -> releaseDBService.releaseId(TEST_APPLICATION, TEST_ENVIRONMENT, TEST_VERSION, TEST_TIMESTAMP));
        assertTrue(result.isPresent());
        infoLogResult(result);
    }

    @Test
    void releases() {
        final var result =
                assertDoesNotThrow(() -> releaseDBService.releases(TEST_APPLICATION, TEST_ENVIRONMENT, TEST_VERSION));
        assertFalse(result.isEmpty());
        infoLogResult(result);
    }

    @Test
    void insert() {
        final var app = "test-insert";
        final var env = "test-insert";
        final var ver = "0.0.2";
        final var rts = Instant.now();

        final var result = assertDoesNotThrow(() -> releaseDBService.insert(app, env, ver, rts));
        assertTrue(result.isPresent());
        infoLogResult(result);
    }

    private static Jdbi createTestJdbi() {
        final var hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/release_tracker");
        hikariDataSource.setUsername(DBConfiguration.DEFAULT_DB_JDBC_USER);
        hikariDataSource.setPassword("dev");
        return Jdbi.create(hikariDataSource);
    }

    private static void insertTestRelease() {
        final var insert =
                """
                INSERT INTO releases (application, environment, version, release_timestamp)
                VALUES (:app, :env, :ver, :rts);
                """;
        jdbi.useHandle(handle -> handle.createUpdate(insert)
                .bind("app", TEST_APPLICATION)
                .bind("env", TEST_ENVIRONMENT)
                .bind("ver", TEST_VERSION)
                .bind("rts", TEST_TIMESTAMP)
                .execute());
    }

    private static void infoLogResult(final Object result) {
        log.atInfo()
                .setMessage("Result: {}")
                .addMarker(GlobalConstants.Markers.TEST)
                .addArgument(result)
                .log();
    }
}
