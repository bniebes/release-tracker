package de.iu.bniebes.service.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.iu.bniebes.model.OptInfo;
import de.iu.bniebes.model.db.Release;
import de.iu.bniebes.model.parameter.AllParameters;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import de.iu.bniebes.util.TimestampUtils;
import java.math.BigInteger;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReleaseOptInfoServiceTest {

    private static final String TEST_APP = "test-app";
    private static final String TEST_ENV = "test-env";
    private static final String TEST_VER = "test-ver";
    private static final Instant TEST_INSTANT = Instant.now();
    private static final BigInteger TEST_TS = TimestampUtils.zuluEpochMicrosOf(TEST_INSTANT);
    private static final AllParameters TEST_ALL_PARAMETERS = new AllParameters(TEST_APP, TEST_ENV, TEST_VER, TEST_TS);
    private static final BigInteger TEST_ID = BigInteger.ONE;

    private final ReleaseDBService mockReleaseDBService = mock(ReleaseDBService.class);
    private final ReleaseOptInfoDBService mockReleaseOptInfoDBService = mock(ReleaseOptInfoDBService.class);
    private final ReleaseOptInfoService releaseOptInfoService =
            new ReleaseOptInfoService(mockReleaseDBService, mockReleaseOptInfoDBService);

    @BeforeEach
    void resetMocks() {
        reset(mockReleaseDBService, mockReleaseOptInfoDBService);
    }

    @Nested
    public class OptInfoTests {

        @Test
        void optInfo() {
            final var releaseName = "test-release-name";
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.of( new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT, TEST_ID)));
            when(mockReleaseOptInfoDBService.stringValueById(TEST_ID, OptInfo.RELEASE_NAME))
                    .thenReturn(Result.of(releaseName));

            final var result =
                    assertDoesNotThrow(() -> releaseOptInfoService.optInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isPresent());
            assertTrue(result.get().contains(releaseName));
        }

        @Test
        void optInfo_releaseNotFound() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.empty());

            final var result =
                    assertDoesNotThrow(() -> releaseOptInfoService.optInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isEmpty());

            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void optInfo_releaseDBError() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.error());

            final var result =
                    assertDoesNotThrow(() -> releaseOptInfoService.optInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isError());

            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void optInfo_optInfoNotFound() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.of( new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT, TEST_ID)));
            when(mockReleaseOptInfoDBService.stringValueById(TEST_ID, OptInfo.RELEASE_NAME))
                    .thenReturn(Result.empty());

            final var result =
                    assertDoesNotThrow(() -> releaseOptInfoService.optInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isEmpty());
        }

        @Test
        void optInfo_optInfoDBError() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.of( new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT, TEST_ID)));
            when(mockReleaseOptInfoDBService.stringValueById(TEST_ID, OptInfo.RELEASE_NAME))
                    .thenReturn(Result.error());

            final var result =
                    assertDoesNotThrow(() -> releaseOptInfoService.optInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isError());
        }
    }

    @Nested
    public class DeleteOptInfoTests {

        @Test
        void deleteOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.of( new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT, TEST_ID)));
            when(mockReleaseOptInfoDBService.deleteValueById(TEST_ID, OptInfo.RELEASE_NAME))
                    .thenReturn(Result.of(true));

            final var result = assertDoesNotThrow(
                    () -> releaseOptInfoService.deleteOptInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isPresent());
            assertTrue(result.get());
        }

        @Test
        void deleteOptInfo_releaseNotFound() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.empty());

            final var result = assertDoesNotThrow(
                    () -> releaseOptInfoService.deleteOptInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isEmpty());
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void deleteOptInfo_releaseDBError() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.error());

            final var result = assertDoesNotThrow(
                    () -> releaseOptInfoService.deleteOptInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isError());
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void deleteOptInfo_optInfoNotFound() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.of( new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT, TEST_ID)));
            when(mockReleaseOptInfoDBService.deleteValueById(TEST_ID, OptInfo.RELEASE_NAME))
                    .thenReturn(Result.empty());

            final var result = assertDoesNotThrow(
                    () -> releaseOptInfoService.deleteOptInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isEmpty());
        }

        @Test
        void deleteOptInfo_optInfoDBError() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT))
                    .thenReturn(Result.of( new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_INSTANT, TEST_ID)));
            when(mockReleaseOptInfoDBService.deleteValueById(TEST_ID, OptInfo.RELEASE_NAME))
                    .thenReturn(Result.error());

            final var result = assertDoesNotThrow(
                    () -> releaseOptInfoService.deleteOptInfo(TEST_ALL_PARAMETERS, OptInfo.RELEASE_NAME));
            assertTrue(result.isError());
        }
    }
}
