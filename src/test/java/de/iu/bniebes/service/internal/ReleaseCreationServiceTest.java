package de.iu.bniebes.service.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.iu.bniebes.model.db.Release;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import de.iu.bniebes.util.TimestampUtils;
import java.math.BigInteger;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReleaseCreationServiceTest {

    private static final String TEST_APP = "test-app";
    private static final String TEST_ENV = "test-env";
    private static final String TEST_VER = "test-ver";

    private final ReleaseDBService mockReleaseDBService = mock(ReleaseDBService.class);
    private final ReleaseOptInfoDBService mockReleaseOptInfoDBService = mock(ReleaseOptInfoDBService.class);
    private final InputSanitizationService spyInputSanitizationService = spy(new InputSanitizationService());
    private final ReleaseCreationService releaseCreationService =
            new ReleaseCreationService(mockReleaseDBService, mockReleaseOptInfoDBService, spyInputSanitizationService);

    @BeforeEach
    void resetMocks() {
        reset(mockReleaseDBService);
    }

    @Nested
    class CreateTests {

        @Test
        void create() {
            when(mockReleaseDBService.insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.of(BigInteger.ONE));

            final var result = releaseCreationService.create(TEST_APP, TEST_ENV, TEST_VER);
            verify(mockReleaseDBService, times(1)).insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any());
            assertTrue(result.isPresent());
        }

        @Test
        void create_insertFailure() {
            when(mockReleaseDBService.insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.error());

            final var result = releaseCreationService.create(TEST_APP, TEST_ENV, TEST_VER);
            verify(mockReleaseDBService, times(1)).insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any());
            assertTrue(result.isError());
        }
    }

    @Nested
    class CreateOrUpdateTests {

        private static final BigInteger TEST_TIMESTAMP = BigInteger.valueOf(1724704455312088L);
        private static final Instant TEST_TIMESTAMP_INSTANT = TimestampUtils.instantOf(TEST_TIMESTAMP);
        private static final BigInteger TEST_ID = BigInteger.ONE;
        private static final String NO_OPT_INFO = "";
        private static final String EMPTY_OPT_INFO = "{}";

        @Test
        void createOrUpdate_releaseDBError() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.error());

            final var result = assertDoesNotThrow(() ->
                    releaseCreationService.createOrUpdate(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, NO_OPT_INFO));
            assertTrue(result.isError());

            verifyNoInteractions(spyInputSanitizationService);
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void createOrUpdate_creatRelease_NoOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.empty());
            when(mockReleaseDBService.insert(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.of(TEST_ID));

            final var result = assertDoesNotThrow(() ->
                    releaseCreationService.createOrUpdate(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, NO_OPT_INFO));
            assertTrue(result.isPresent());
            assertTrue(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verifyNoInteractions(spyInputSanitizationService);
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void createOrUpdate_creatRelease_EmptyOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.empty());
            when(mockReleaseDBService.insert(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.of(TEST_ID));

            final var result = assertDoesNotThrow(() -> releaseCreationService.createOrUpdate(
                    TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, EMPTY_OPT_INFO));
            assertTrue(result.isPresent());
            assertTrue(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verifyNoInteractions(spyInputSanitizationService);
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void createOrUpdate_createRelease_FullOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.empty());
            when(mockReleaseDBService.insert(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.of(TEST_ID));

            final var result = assertDoesNotThrow(() ->
                    releaseCreationService.createOrUpdate(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, fullOptInfo()));
            assertTrue(result.isPresent());
            assertTrue(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verifyFullOptInfo();
        }

        @Test
        void createOrUpdate_createRelease_PartialOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.empty());
            when(mockReleaseDBService.insert(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(Result.of(TEST_ID));

            final var result = assertDoesNotThrow(() -> releaseCreationService.createOrUpdate(
                    TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, partialOptInfo()));
            assertTrue(result.isPresent());
            assertTrue(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verifyPartialOptInfo();
        }

        @Test
        void createOrUpdate_updateRelease_FullOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(releaseResult());

            final var result = assertDoesNotThrow(() ->
                    releaseCreationService.createOrUpdate(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, fullOptInfo()));
            assertTrue(result.isPresent());
            assertFalse(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verify(mockReleaseDBService, never()).insert(any(), any(), any(), any());
            verifyFullOptInfo();
        }

        @Test
        void createOrUpdate_updateRelease_PartialOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(releaseResult());

            final var result = assertDoesNotThrow(() -> releaseCreationService.createOrUpdate(
                    TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, partialOptInfo()));
            assertTrue(result.isPresent());
            assertFalse(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verify(mockReleaseDBService, never()).insert(any(), any(), any(), any());
            verifyPartialOptInfo();
        }

        @Test
        void createOrUpdate_createOrUpdate_updateRelease_NoOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(releaseResult());

            final var result = assertDoesNotThrow(() ->
                    releaseCreationService.createOrUpdate(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, NO_OPT_INFO));
            assertTrue(result.isPresent());
            assertFalse(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verify(mockReleaseDBService, never()).insert(any(), any(), any(), any());
            verifyNoInteractions(spyInputSanitizationService);
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void createOrUpdate_createOrUpdate_updateRelease_EmptyOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(releaseResult());

            final var result = assertDoesNotThrow(() -> releaseCreationService.createOrUpdate(
                    TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, EMPTY_OPT_INFO));
            assertTrue(result.isPresent());
            assertFalse(result.get().created());
            assertFalse(result.get().jsonResponse().isBlank());

            verify(mockReleaseDBService, never()).insert(any(), any(), any(), any());
            verifyNoInteractions(spyInputSanitizationService);
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        @Test
        void createOrUpdate_createOrUpdate_updateRelease_InvalidJsonOptInfo() {
            when(mockReleaseDBService.release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT))
                    .thenReturn(releaseResult());

            final var invalidJson =
                    """
                    {
                        "": "
                    }
                    """;

            final var result = assertDoesNotThrow(() ->
                    releaseCreationService.createOrUpdate(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP, invalidJson));
            assertTrue(result.isError());

            verify(mockReleaseDBService, never()).insert(any(), any(), any(), any());
            verifyNoInteractions(spyInputSanitizationService);
            verifyNoInteractions(mockReleaseOptInfoDBService);
        }

        private String fullOptInfo() {
            return """
                    {
                        "releaseName": "name",
                        "description": "desc",
                        "changes": "change",
                        "responsibility": "resp",
                        "buildLocation": "loc"
                    }
                    """;
        }

        private void verifyFullOptInfo() {
            verify(spyInputSanitizationService, times(5)).safeString(any());
            verify(mockReleaseOptInfoDBService, times(1)).insertReleaseName(TEST_ID, "name");
            verify(mockReleaseOptInfoDBService, times(1)).insertDescription(TEST_ID, "desc");
            verify(mockReleaseOptInfoDBService, times(1)).insertChanges(TEST_ID, "change");
            verify(mockReleaseOptInfoDBService, times(1)).insertResponsibility(TEST_ID, "resp");
            verify(mockReleaseOptInfoDBService, times(1)).insertBuildLocation(TEST_ID, "loc");
        }

        private String partialOptInfo() {
            return """
                    {
                        "releaseName": "name",
                        "responsibility": "resp"
                    }
                    """;
        }

        private void verifyPartialOptInfo() {
            verify(spyInputSanitizationService, times(2)).safeString(any());
            verify(mockReleaseOptInfoDBService, times(1)).insertReleaseName(TEST_ID, "name");
            verify(mockReleaseOptInfoDBService, never()).insertDescription(TEST_ID, "desc");
            verify(mockReleaseOptInfoDBService, never()).insertChanges(TEST_ID, "change");
            verify(mockReleaseOptInfoDBService, times(1)).insertResponsibility(TEST_ID, "resp");
            verify(mockReleaseOptInfoDBService, never()).insertBuildLocation(TEST_ID, "loc");
        }

        private Result<Release> releaseResult() {
            return Result.of(new Release(TEST_APP, TEST_ENV, TEST_VER, TEST_TIMESTAMP_INSTANT, TEST_ID));
        }
    }
}
