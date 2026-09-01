package ua.com.ostrog.photo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Placeholder unit test. This is only a scaffold to prove the test
 * infrastructure works - it does not exercise any real application logic yet.
 */
class ExtractorTest {

    @Test
    @DisplayName("test harness is wired up")
    void sanityCheck() {
        assertEquals(4, 2 + 2);
        assertTrue(Extractor.class.getName().endsWith("Extractor"));
    }
}
