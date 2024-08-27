package de.iu.bniebes.service.internal;

import static de.iu.bniebes.util.TimestampUtils.zuluEpochMicrosOf;
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
            final var zuluEpochNanos = zuluEpochMicrosOf(timestamp);

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
            assertEquals(zuluEpochNanos, response.zuluEpochMicros());
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
            final var zuluEpochNanos = zuluEpochMicrosOf(timestamp);

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
            assertEquals(zuluEpochNanos, response.zuluEpochMicros());
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
            when(mockReleaseDBService.fullReleases()).thenReturn(Result.of(testFullReleases()));

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

    @Nested
    class AllByApplicationTests {

        @Test
        void allByApplication() {
            when(mockReleaseDBService.fullReleasesByApplication(TEST_APP)).thenReturn(Result.of(testFullReleases()));

            final var result = releaseAccessService.allByApplication(TEST_APP);
            assertTrue(result.isPresent());
        }

        @Test
        void allByApplication_DBEmpty() {
            when(mockReleaseDBService.fullReleasesByApplication(TEST_APP)).thenReturn(Result.empty());

            final var result = releaseAccessService.allByApplication(TEST_APP);
            assertTrue(result.isEmpty());
        }

        @Test
        void allByApplication_DBError() {
            when(mockReleaseDBService.fullReleasesByApplication(TEST_APP)).thenReturn(Result.error());

            final var result = releaseAccessService.allByApplication(TEST_APP);
            assertTrue(result.isError());
        }
    }

    @Nested
    class AllByApplicationAndEnvironmentTests {

        @Test
        void allByApplication() {
            when(mockReleaseDBService.fullReleasesByApplicationAndEnvironment(TEST_APP, TEST_ENV))
                    .thenReturn(Result.of(testFullReleases()));

            final var result = releaseAccessService.allByApplicationAndEnvironment(TEST_APP, TEST_ENV);
            assertTrue(result.isPresent());
        }

        @Test
        void allByApplication_DBEmpty() {
            when(mockReleaseDBService.fullReleasesByApplicationAndEnvironment(TEST_APP, TEST_ENV))
                    .thenReturn(Result.empty());

            final var result = releaseAccessService.allByApplicationAndEnvironment(TEST_APP, TEST_ENV);
            assertTrue(result.isEmpty());
        }

        @Test
        void allByApplication_DBError() {
            when(mockReleaseDBService.fullReleasesByApplicationAndEnvironment(TEST_APP, TEST_ENV))
                    .thenReturn(Result.error());

            final var result = releaseAccessService.allByApplicationAndEnvironment(TEST_APP, TEST_ENV);
            assertTrue(result.isError());
        }
    }

    @Nested
    public class CurrentByApplicationTests {

        @Test
        void currentByApplication() {
            when(mockReleaseDBService.currentReleaseByApplication(TEST_APP)).thenReturn(Result.of(testFullRelease()));

            final var result = releaseAccessService.currentByApplication(TEST_APP);
            assertTrue(result.isPresent());
        }

        @Test
        void currentByApplication_DBEmpty() {
            when(mockReleaseDBService.currentReleaseByApplication(TEST_APP)).thenReturn(Result.error());

            final var result = releaseAccessService.currentByApplication(TEST_APP);
            assertTrue(result.isError());
        }

        @Test
        void currentByApplication_DBError() {
            when(mockReleaseDBService.currentReleaseByApplication(TEST_APP)).thenReturn(Result.error());

            final var result = releaseAccessService.currentByApplication(TEST_APP);
            assertTrue(result.isError());
        }
    }

    @Nested
    public class CurrentByApplicationAndEnvironmentTests {

        @Test
        void currentByApplicationAndEnvironment() {
            when(mockReleaseDBService.currentReleaseByApplicationAndEnvironment(TEST_APP, TEST_ENV))
                    .thenReturn(Result.of(testFullRelease()));

            final var result = releaseAccessService.currentByApplicationAndEnvironment(TEST_APP, TEST_ENV);
            assertTrue(result.isPresent());
        }

        @Test
        void currentByApplicationAndEnvironment_DBEmpty() {
            when(mockReleaseDBService.currentReleaseByApplicationAndEnvironment(TEST_APP, TEST_ENV))
                    .thenReturn(Result.error());

            final var result = releaseAccessService.currentByApplicationAndEnvironment(TEST_APP, TEST_ENV);
            assertTrue(result.isError());
        }

        @Test
        void currentByApplicationAndEnvironment_DBError() {
            when(mockReleaseDBService.currentReleaseByApplicationAndEnvironment(TEST_APP, TEST_ENV))
                    .thenReturn(Result.error());

            final var result = releaseAccessService.currentByApplicationAndEnvironment(TEST_APP, TEST_ENV);
            assertTrue(result.isError());
        }
    }

    private static FullRelease testFullRelease() {
        return new FullRelease(
                BigInteger.ONE, TEST_APP, TEST_ENV, TEST_VER, Instant.now(), "test", "test", "test", "test", "test");
    }

    private static Set<FullRelease> testFullReleases() {
        return Set.of(
                testFullRelease(),
                new FullRelease(
                        BigInteger.TWO, TEST_APP, TEST_ENV, TEST_VER, Instant.now(), null, null, null, null, null));
    }
}
