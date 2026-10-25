package com.jeff_media.jefflib.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class McVersionTest {

    @ParameterizedTest
    @CsvSource({
            "26.3, 26, 3, 0",
            "26.3.build.5-alpha, 26, 3, 0",
            "26.2.build.7-beta, 26, 2, 0",
            "26.1.2.build.69-stable, 26, 1, 2",
            "26.1.2, 26, 1, 2",
            "26.3-pre-1, 26, 3, 0",
            "26.3-rc-1, 26, 3, 0",
            "1.21.5, 1, 21, 5",
            "1.21.5-R0.1-SNAPSHOT, 1, 21, 5",
            "1.21-R0.1-SNAPSHOT, 1, 21, 0",
            "1.14.4-R0.1-SNAPSHOT, 1, 14, 4"
    })
    void parsesGameVersionsWithoutMistakingBuildNumbersForPatches(
            String input, int major, int minor, int patch) {
        McVersion parsed = McVersion.parse(input);
        assertEquals(major, parsed.getMajor());
        assertEquals(minor, parsed.getMinor());
        assertEquals(patch, parsed.getPatch());
    }

    @Test
    void calendarVersionsStillEnableModernJeffLibFeatures() {
        McVersion version = McVersion.parse("26.3");
        assertTrue(version.isAtLeast(1, 14, 4));
        assertTrue(version.isAtLeast(1, 20, 5));
        assertTrue(version.isAtLeast(26, 2));
        assertFalse(version.isAtLeast(26, 3, 1));
        assertFalse(version.hasVersionInNmsPackageName());
        assertEquals("26.3", version.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "build", "26", "26.build.5", "26.3.1.2", "25w46a"})
    void rejectsUnrecognizedFormatsWithTheOriginalVersionInTheError(String input) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> McVersion.parse(input));
        assertTrue(error.getMessage().contains(input));
    }
}
