package de.iu.bniebes.service.internal;

import static de.iu.bniebes.util.TimestampUtils.zuluEpochNanosOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.model.db.*;
import de.iu.bniebes.model.response.ReleaseResponse;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReleaseAccessServiceTest {

    private static final String TEST_APP = "test-app";
    private static final String TEST_ENV = "test-env";
    private static final String TEST_VER = "test-ver";

    private final ReleaseDBService mockReleaseDBService = mock(ReleaseDBService.class);
    private final ReleaseOptInfoDBService mockReleaseOptInfoDBService = mock(ReleaseOptInfoDBService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final ReleaseAccessService releaseAccessService =
            new ReleaseAccessService(mockReleaseDBService, mockReleaseOptInfoDBService);

    @BeforeEach
    void resetMocks() {
        reset(mockReleaseDBService, mockReleaseOptInfoDBService);
    }

    @Nested
    class GetTests {

        @Test
        void get() {
            final var id = BigInteger.ONE;
            final var timestamp = Instant.now();
            final var zuluEpochNanos = zuluEpochNanosOf(timestamp);

            final var testReleaseName = "test-release-name";
            final var testDescription = "test-description";
            final var testChanges = "test-change-list";
            final var testResponsibility = "test-responsibility";
            final var testBuildLocation = "test-build-location";

            when(mockReleaseDBService.release(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.of(new Release(TEST_APP, TEST_ENV, TEST_VER, timestamp, id)));
            when(mockReleaseOptInfoDBService.releaseNameById(id))
                    .thenReturn(Result.of(new ReleaseName(id, testReleaseName)));
            when(mockReleaseOptInfoDBService.descriptionById(id))
                    .thenReturn(Result.of(new Description(id, testDescription)));
            when(mockReleaseOptInfoDBService.changesById(id)).thenReturn(Result.of(new Changes(id, testChanges)));
            when(mockReleaseOptInfoDBService.responsibilityById(id))
                    .thenReturn(Result.of(new Responsibility(id, testResponsibility)));
            when(mockReleaseOptInfoDBService.buildLocationById(id))
                    .thenReturn(Result.of(new BuildLocation(id, testBuildLocation)));

            final var result = releaseAccessService.get(TEST_APP, TEST_ENV, TEST_VER, zuluEpochNanos);
            assertTrue(result.isPresent());
            System.out.println(result.get());

            final var response = assertDoesNotThrow(() -> mapper.readValue(result.get(), ReleaseResponse.class));
            assertEquals(TEST_APP, response.application());
            assertEquals(TEST_ENV, response.environment());
            assertEquals(TEST_VER, response.version());
            assertEquals(zuluEpochNanos, response.zuluEpochNanos());
            assertEquals(testReleaseName, response.releaseName());
            assertEquals(testDescription, response.description());
            assertEquals(testChanges, response.changes());
            assertEquals(testResponsibility, response.responsibility());
            assertEquals(testBuildLocation, response.buildLocation());
            System.out.println(response);
        }

        @Test
        void get_NoOptionalInfo() {
            final var id = BigInteger.ONE;
            final var timestamp = Instant.now();
            final var zuluEpochNanos = zuluEpochNanosOf(timestamp);

            when(mockReleaseDBService.release(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.of(new Release(TEST_APP, TEST_ENV, TEST_VER, timestamp, id)));
            when(mockReleaseOptInfoDBService.releaseNameById(id)).thenReturn(Result.empty());
            when(mockReleaseOptInfoDBService.descriptionById(id)).thenReturn(Result.empty());
            when(mockReleaseOptInfoDBService.changesById(id)).thenReturn(Result.empty());
            when(mockReleaseOptInfoDBService.responsibilityById(id)).thenReturn(Result.empty());
            when(mockReleaseOptInfoDBService.buildLocationById(id)).thenReturn(Result.empty());

            final var result = releaseAccessService.get(TEST_APP, TEST_ENV, TEST_VER, zuluEpochNanos);
            assertTrue(result.isPresent());
            System.out.println(result.get());

            final var response = assertDoesNotThrow(() -> mapper.readValue(result.get(), ReleaseResponse.class));
            assertEquals(TEST_APP, response.application());
            assertEquals(TEST_ENV, response.environment());
            assertEquals(TEST_VER, response.version());
            assertEquals(zuluEpochNanos, response.zuluEpochNanos());
            assertNull(response.releaseName());
            assertNull(response.description());
            assertNull(response.changes());
            assertNull(response.responsibility());
            assertNull(response.buildLocation());
            System.out.println(response);
        }

        @Test
        void get_NotInDB() {
            when(mockReleaseDBService.release(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.empty());

            final var result = releaseAccessService.get(TEST_APP, TEST_ENV, TEST_VER, BigInteger.valueOf(12345));
            assertTrue(result.isEmpty());
        }

        @Test
        void get_DBError() {
            when(mockReleaseDBService.release(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.error());

            final var result = releaseAccessService.get(TEST_APP, TEST_ENV, TEST_VER, BigInteger.valueOf(12345));
            assertTrue(result.isError());
        }
    }

    @Nested
    class AllTests {

        @Test
        void all() {
            final var fullReleases = Set.of(
                    new FullRelease(
                            BigInteger.ONE,
                            TEST_APP,
                            TEST_ENV,
                            TEST_VER,
                            Instant.now(),
                            "test",
                            "test",
                            "test",
                            "test",
                            "test"),
                    new FullRelease(
                            BigInteger.TWO, TEST_APP, TEST_ENV, TEST_VER, Instant.now(), null, null, null, null, null));
            when(mockReleaseDBService.fullReleases()).thenReturn(Result.of(fullReleases));

            final var result = releaseAccessService.all();
            assertTrue(result.isPresent());
        }

        @Test
        void all_DBEmpty() {
            when(mockReleaseDBService.fullReleases()).thenReturn(Result.empty());

            final var result = releaseAccessService.all();
            assertTrue(result.isEmpty());
        }

        @Test
        void all_DBError() {
            when(mockReleaseDBService.fullReleases()).thenReturn(Result.error());

            final var result = releaseAccessService.all();
            assertTrue(result.isError());
        }
    }
}
