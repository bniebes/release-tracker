package de.iu.bniebes.service.external.db.release;

import static org.junit.jupiter.api.Assertions.*;

import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import de.iu.bniebes.service.external.db.helper.DBTestHelper;
import java.math.BigInteger;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@Slf4j
@EnabledIfSystemProperty(named = "test.condition.pgsql", matches = "true")
class ReleaseOptInfoDBServiceTest {

    private static final Jdbi JDBI = DBTestHelper.createTestJdbi();
    private static final ReleaseDBService RELEASE_DB_SERVICE = new ReleaseDBService(JDBI);

    private static final String TEST_APP = "test";
    private static final String TEST_ENV = "test";
    private static final String TEST_VER = "0.0.3";
    private static final Instant TEST_TIMESTAMP = Instant.now();
    private static final BigInteger TEST_ID = RELEASE_DB_SERVICE
            .insert(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP)
            .get();
    private static final BigInteger TEST_ID_UPDATE = RELEASE_DB_SERVICE
            .insert(TEST_APP, TEST_ENV, TEST_VER, Instant.now())
            .get();

    private final ReleaseOptInfoDBService releaseOptInfoDBService = new ReleaseOptInfoDBService(JDBI);

    @Test
    void valueNotPresent() {
        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.releaseNameById(BigInteger.ZERO));
        assertTrue(queryResult.isEmpty());
    }

    @Test
    void releaseNameTest() {
        final var value = "release-name";

        final var insertResult = assertDoesNotThrow(() -> releaseOptInfoDBService.insertReleaseName(TEST_ID, value));
        DBTestHelper.infoLogResult(insertResult);
        assertTrue(insertResult);

        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.releaseNameById(TEST_ID));
        DBTestHelper.infoLogResult(queryResult);
        assertTrue(queryResult.isPresent());
        assertEquals(TEST_ID, queryResult.get().id());
        assertEquals(value, queryResult.get().name());
    }

    @Test
    void updateReleaseName() {
        final var value = "release-name-before-update";
        final var updateValue = "release-name-after-update";

        final var insertResult =
                assertDoesNotThrow(() -> releaseOptInfoDBService.insertReleaseName(TEST_ID_UPDATE, value));
        assertTrue(insertResult);

        final var insertResultUpdate =
                assertDoesNotThrow(() -> releaseOptInfoDBService.insertReleaseName(TEST_ID_UPDATE, updateValue));
        assertTrue(insertResultUpdate);

        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.releaseNameById(TEST_ID_UPDATE));
        assertTrue(queryResult.isPresent());
        assertEquals(TEST_ID_UPDATE, queryResult.get().id());
        assertEquals(updateValue, queryResult.get().name());
    }

    @Test
    void descriptionTest() {
        final var value = "description";

        final var insertResult = assertDoesNotThrow(() -> releaseOptInfoDBService.insertDescription(TEST_ID, value));
        DBTestHelper.infoLogResult(insertResult);
        assertTrue(insertResult);

        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.descriptionById(TEST_ID));
        DBTestHelper.infoLogResult(queryResult);
        assertTrue(queryResult.isPresent());
        assertEquals(TEST_ID, queryResult.get().id());
        assertEquals(value, queryResult.get().description());
    }

    @Test
    void changesTest() {
        final var value = "changes";

        final var insertResult = assertDoesNotThrow(() -> releaseOptInfoDBService.insertChanges(TEST_ID, value));
        DBTestHelper.infoLogResult(insertResult);
        assertTrue(insertResult);

        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.changesById(TEST_ID));
        DBTestHelper.infoLogResult(queryResult);
        assertTrue(queryResult.isPresent());
        assertEquals(TEST_ID, queryResult.get().id());
        assertEquals(value, queryResult.get().changes());
    }

    @Test
    void responsibilityTest() {
        final var value = "responsibility";

        final var insertResult = assertDoesNotThrow(() -> releaseOptInfoDBService.insertResponsibility(TEST_ID, value));
        DBTestHelper.infoLogResult(insertResult);
        assertTrue(insertResult);

        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.responsibilityById(TEST_ID));
        DBTestHelper.infoLogResult(queryResult);
        assertTrue(queryResult.isPresent());
        assertEquals(TEST_ID, queryResult.get().id());
        assertEquals(value, queryResult.get().name());
    }

    @Test
    void buildLocationTest() {
        final var value = "build-location";

        final var insertResult = assertDoesNotThrow(() -> releaseOptInfoDBService.insertBuildLocation(TEST_ID, value));
        DBTestHelper.infoLogResult(insertResult);
        assertTrue(insertResult);

        final var queryResult = assertDoesNotThrow(() -> releaseOptInfoDBService.buildLocationById(TEST_ID));
        DBTestHelper.infoLogResult(queryResult);
        assertTrue(queryResult.isPresent());
        assertEquals(TEST_ID, queryResult.get().id());
        assertEquals(value, queryResult.get().name());
    }
}
