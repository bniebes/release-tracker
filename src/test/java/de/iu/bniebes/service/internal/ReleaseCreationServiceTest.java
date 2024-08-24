package de.iu.bniebes.service.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReleaseCreationServiceTest {

    private final ReleaseDBService releaseDBService = mock(ReleaseDBService.class);
    private final ReleaseCreationService releaseCreationService = new ReleaseCreationService(releaseDBService);

    @BeforeEach
    void resetMocks() {
        reset(releaseDBService);
    }

    @Nested
    class CreateTests {

        private static final String TEST_APP = "test-app";
        private static final String TEST_ENV = "test-env";
        private static final String TEST_VER = "test-ver";

        @Test
        void create() {
            when(releaseDBService.insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.of(BigInteger.ONE));

            final var result = releaseCreationService.create(TEST_APP, TEST_ENV, TEST_VER);
            verify(releaseDBService, times(1)).insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any());
            assertTrue(result.isPresent());
        }

        @Test
        void create_insertFailure() {
            when(releaseDBService.insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any()))
                    .thenReturn(Result.error());

            final var result = releaseCreationService.create(TEST_APP, TEST_ENV, TEST_VER);
            verify(releaseDBService, times(1)).insert(eq(TEST_APP), eq(TEST_ENV), eq(TEST_VER), any());
            assertTrue(result.isError());
        }
    }
}
