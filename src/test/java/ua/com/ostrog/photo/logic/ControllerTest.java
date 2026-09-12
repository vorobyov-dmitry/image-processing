package ua.com.ostrog.photo.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Placeholder unit test. This is only a scaffold to prove the test
 * infrastructure works - it does not exercise any real application logic yet.
 */
class ControllerTest {

    @Test
    @DisplayName("test harness is wired up")
    void sanityCheck() {
        assertEquals(4, 2 + 2);
        assertNotNull(new Controller());
    }
}
