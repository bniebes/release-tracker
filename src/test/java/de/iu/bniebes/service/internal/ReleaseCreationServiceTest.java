package de.iu.bniebes.service.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReleaseCreationServiceTest {

    private final ReleaseDBService mockReleaseDBService = mock(ReleaseDBService.class);
    private final ReleaseOptInfoDBService mockReleaseOptInfoDBService = mock(ReleaseOptInfoDBService.class);
    private final ReleaseCreationService releaseCreationService = new ReleaseCreationService(
            mockReleaseDBService, mockReleaseOptInfoDBService, new InputSanitizationService());

    @BeforeEach
    void resetMocks() {
        reset(mockReleaseDBService);
    }

    @Nested
    class CreateTests {

        private static final String TEST_APP = "test-app";
        private static final String TEST_ENV = "test-env";
        private static final String TEST_VER = "test-ver";

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
}
