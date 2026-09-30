package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Random;

public class RandomStringUtilsTest {

    // Test count = 0 returns empty string
    @Test
    public void testRandom_countZero_returnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.random(0, true, true));
        assertEquals("", RandomStringUtils.randomAscii(0));
    }

    // Test negative count throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCount_throwsIllegalArgumentException() {
        RandomStringUtils.random(-1);
    }

    // Test randomNumeric returns only digits
    @Test
    public void testRandomNumeric_positiveCount_returnsNumericString() {
        String result = RandomStringUtils.randomNumeric(10);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    // Test randomAlphabetic returns only letters
    @Test
    public void testRandomAlphabetic_positiveCount_returnsAlphabeticString() {
        String result = RandomStringUtils.randomAlphabetic(5);
        assertEquals(5, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    // Test randomAlphanumeric returns only letters or digits
    @Test
    public void testRandomAlphanumeric_positiveCount_returnsAlphanumericString() {
        String result = RandomStringUtils.randomAlphanumeric(8);
        assertEquals(8, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    // Test randomAscii returns characters in range 32-126
    @Test
    public void testRandomAscii_positiveCount_returnsAsciiString() {
        String result = RandomStringUtils.randomAscii(3);
        assertEquals(3, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 32 && c <= 126);
        }
    }

    // Test random with empty char array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_charArrayEmpty_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, new char[0]);
    }

    // Test random with null char array should not throw
    @Test
    public void testRandom_charArrayNull_returnsRandomString() {
        String result = RandomStringUtils.random(5, (char[]) null);
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    // Test random with null string should not throw
    @Test
    public void testRandom_stringNull_returnsRandomString() {
        String result = RandomStringUtils.random(5, (String) null);
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    // Test random with empty string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_stringEmpty_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, "");
    }

    // Test low surrogate with count=1 triggers infinite loop (defect 12b)
    @Test(timeout = 1000)
    public void testRandom_count1AndLowSurrogate_detectsInfiniteLoop() {
        char[] chars = new char[] {(char)56320};
        String result = RandomStringUtils.random(1, chars);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test high surrogate with count=1 triggers infinite loop
    @Test(timeout = 1000)
    public void testRandom_count1AndHighSurrogate_detectsInfiniteLoop() {
        char[] chars = new char[] {(char)55296};
        String result = RandomStringUtils.random(1, chars);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test private high surrogate with count=1 triggers infinite loop
    @Test(timeout = 1000)
    public void testRandom_count1AndPrivateHighSurrogate_detectsInfiniteLoop() {
        char[] chars = new char[] {(char)56192};
        String result = RandomStringUtils.random(1, chars);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test letters=true and low surrogate -> condition rejects, triggers infinite loop
    @Test(timeout = 1000)
    public void testRandom_lettersTrueAndLowSurrogate_detectsInfiniteLoop() {
        char[] chars = new char[] {(char)56320};
        Random random = new Random();
        String result = RandomStringUtils.random(1, 0, 1, true, false, chars, random);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test numbers=true and high surrogate -> condition rejects, triggers infinite loop
    @Test(timeout = 1000)
    public void testRandom_numbersTrueAndHighSurrogate_detectsInfiniteLoop() {
        char[] chars = new char[] {(char)55296};
        Random random = new Random();
        String result = RandomStringUtils.random(1, 0, 1, false, true, chars, random);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test surrogate pair when count=2 (normal handling, no infinite loop)
    @Test
    public void testRandom_count2WithLowSurrogate_returnsCorrectLength() {
        char[] chars = new char[] {(char)56320};
        String result = RandomStringUtils.random(2, chars);
        assertNotNull(result);
        assertEquals(2, result.length());
    }

    // Test start/end zero with letters true sets start=' ', end='z'+1
    @Test
    public void testRandom_startEndZeroLettersTrue_setsStartSpaceEndZ() {
        String result = RandomStringUtils.random(10, 0, 0, true, false);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= ' ' && c <= 'z');
        }
    }

    // Test random with letters=false, numbers=false allows any characters
    @Test
    public void testRandom_lettersFalseNumbersFalse_returnsAnyChars() {
        String result = RandomStringUtils.random(10, false, false);
        assertEquals(10, result.length());
        // no specific range check, just length
    }
}