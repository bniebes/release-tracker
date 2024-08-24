package de.iu.bniebes.model.result;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void result() {
        final var testValue = "test";
        final var dbResult = Result.of(testValue);
        assertTrue(dbResult.isPresent());
        assertFalse(dbResult.isEmpty());
        assertFalse(dbResult.isError());
        assertEquals(testValue, assertDoesNotThrow(dbResult::get));
    }

    @Test
    void result_Empty() {
        final var dbResult = Result.empty();
        assertFalse(dbResult.isPresent());
        assertTrue(dbResult.isEmpty());
        assertFalse(dbResult.isError());
        assertThrowsExactly(IllegalStateException.class, dbResult::get);
    }

    @Test
    void result_Error() {
        final var dbResult = Result.error();
        assertFalse(dbResult.isPresent());
        assertFalse(dbResult.isEmpty());
        assertTrue(dbResult.isError());
        assertThrowsExactly(IllegalStateException.class, dbResult::get);
    }
}
