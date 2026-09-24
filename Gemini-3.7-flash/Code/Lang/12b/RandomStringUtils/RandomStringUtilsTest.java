package org.apache.commons.lang3;

import org.junit.Test;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link RandomStringUtils}.
 */
public class RandomStringUtilsTest {

    // Tests public constructor instantiation
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        RandomStringUtils rsu = new RandomStringUtils();
        assertNotNull(rsu);
    }

    // Tests random string with count 0 returns empty string
    @Test
    public void testRandom_zeroCount_returnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
    }

    // Tests negative count throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCount_throwsException() {
        RandomStringUtils.random(-1);
    }

    // Tests randomAscii generates printable ASCII characters
    @Test
    public void testRandomAscii_validCount_returnsAsciiCharacters() {
        int count = 50;
        String result = RandomStringUtils.randomAscii(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 32 && c <= 126);
        }
    }

    // Tests randomAlphabetic generates only letters
    @Test
    public void testRandomAlphabetic_validCount_returnsOnlyLetters() {
        int count = 50;
        String result = RandomStringUtils.randomAlphabetic(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    // Tests randomAlphanumeric generates only letters and digits
    @Test
    public void testRandomAlphanumeric_validCount_returnsOnlyAlphaNumeric() {
        int count = 50;
        String result = RandomStringUtils.randomAlphanumeric(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    // Tests randomNumeric generates only digits
    @Test
    public void testRandomNumeric_validCount_returnsOnlyDigits() {
        int count = 50;
        String result = RandomStringUtils.randomNumeric(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    // Tests random with specific char set as String
    @Test
    public void testRandom_withCharString_containsOnlySpecifiedChars() {
        String set = "abc";
        String result = RandomStringUtils.random(20, set);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(set.indexOf(c) != -1);
        }
    }

    // Tests random with null char set String falls back to all characters
    @Test
    public void testRandom_nullCharString_returnsRandomString() {
        String result = RandomStringUtils.random(10, (String) null);
        assertEquals(10, result.length());
    }

    // Tests random with char array
    @Test
    public void testRandom_withCharArray_containsOnlySpecifiedChars() {
        char[] chars = new char[]{'a', 'b', 'c'};
        String result = RandomStringUtils.random(20, chars);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    // Tests random with null char array falls back to all characters
    @Test
    public void testRandom_nullCharArray_returnsRandomString() {
        String result = RandomStringUtils.random(10, (char[]) null);
        assertEquals(10, result.length());
    }

    // Tests random with seeded Random instance for deterministic output
    @Test
    public void testRandom_seededRandom_producesConsistentResults() {
        char[] chars = new char[]{'a', 'b', 'c', 'd', 'e'};
        Random rand1 = new Random(12345L);
        Random rand2 = new Random(12345L);
        String result1 = RandomStringUtils.random(10, 0, 5, false, false, chars, rand1);
        String result2 = RandomStringUtils.random(10, 0, 5, false, false, chars, rand2);
        assertEquals(result1, result2);
    }

    // Tests random with explicit start/end range and letters flag
    @Test
    public void testRandom_customRangeWithLetters_generatesMatchingChars() {
        String result = RandomStringUtils.random(30, 'a', 'z' + 1, true, false);
        assertEquals(30, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 'a' && c <= 'z');
        }
    }

    // Tests random when start and end are 0 with custom char array (Defects4J Lang-12 bug trigger)
    @Test
    public void testRandom_zeroStartAndEndWithCharArray_generatesExpectedString() {
        char[] chars = new char[]{'a', 'b', 'c'};
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars, new Random());
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    // Tests empty char array input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyCharArray_throwsIllegalArgumentException() {
        RandomStringUtils.random(10, new char[0]);
    }

    // Tests empty char array with full parameters throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyCharArrayFullParams_throwsIllegalArgumentException() {
        RandomStringUtils.random(10, 0, 0, false, false, new char[0], new Random());
    }
}