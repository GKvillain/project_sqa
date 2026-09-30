package org.apache.commons.lang3;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link RandomStringUtils}.
 */
public class RandomStringUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_instanceCreation_isNotNull() {
        RandomStringUtils instance = new RandomStringUtils();
        assertNotNull(instance);
    }

    // Tests random string generation with count 0
    @Test
    public void testRandom_zeroCount_returnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
        assertEquals("", RandomStringUtils.random(0, "abc"));
        assertEquals("", RandomStringUtils.random(0, new char[]{'a', 'b'}));
    }

    // Tests random string generation with negative count
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCount_throwsIllegalArgumentException() {
        RandomStringUtils.random(-1);
    }

    // Tests random(int) general behavior
    @Test
    public void testRandom_default_returnsStringOfCorrectLength() {
        String result = RandomStringUtils.random(10);
        assertEquals(10, result.length());
    }

    // Tests randomAscii(int) character range [32, 126]
    @Test
    public void testRandomAscii_validCount_returnsAsciiPrintableCharacters() {
        String result = RandomStringUtils.randomAscii(50);
        assertEquals(50, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 32 && c <= 126);
        }
    }

    // Tests randomAlphabetic(int) characters
    @Test
    public void testRandomAlphabetic_validCount_returnsOnlyLetters() {
        String result = RandomStringUtils.randomAlphabetic(50);
        assertEquals(50, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    // Tests randomAlphanumeric(int) characters
    @Test
    public void testRandomAlphanumeric_validCount_returnsOnlyLettersOrDigits() {
        String result = RandomStringUtils.randomAlphanumeric(50);
        assertEquals(50, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    // Tests randomNumeric(int) characters
    @Test
    public void testRandomNumeric_validCount_returnsOnlyDigits() {
        String result = RandomStringUtils.randomNumeric(50);
        assertEquals(50, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    // Tests random(int, boolean, boolean) with letters and numbers true
    @Test
    public void testRandom_lettersAndNumbersFlags_returnsMatchingCharacters() {
        String result = RandomStringUtils.random(30, true, true);
        assertEquals(30, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    // Tests random(int, int, int, boolean, boolean) custom range
    @Test
    public void testRandom_customRange_returnsCharactersWithinRange() {
        String result = RandomStringUtils.random(20, 'a', 'f' + 1, false, false);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 'a' && c <= 'f');
        }
    }

    // Tests random(int, String) with valid string
    @Test
    public void testRandom_withStringChars_returnsCharactersFromString() {
        String set = "abc123";
        String result = RandomStringUtils.random(30, set);
        assertEquals(30, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(set.indexOf(c) != -1);
        }
    }

    // Tests random(int, String) with null string
    @Test
    public void testRandom_nullString_usesAllCharacters() {
        String result = RandomStringUtils.random(10, (String) null);
        assertEquals(10, result.length());
    }

    // Tests random(int, char...) with custom char array
    @Test
    public void testRandom_withCharArray_returnsCharactersFromArray() {
        char[] set = new char[]{'x', 'y', 'z'};
        String result = RandomStringUtils.random(30, set);
        assertEquals(30, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'x' || c == 'y' || c == 'z');
        }
    }

    // Tests random(int, char...) with null array
    @Test
    public void testRandom_nullCharArray_usesAllCharacters() {
        String result = RandomStringUtils.random(10, (char[]) null);
        assertEquals(10, result.length());
    }

    // Tests seeded random predictability
    @Test
    public void testRandom_withSeededRandom_returnsDeterministicResult() {
        Random rand1 = new Random(12345L);
        Random rand2 = new Random(12345L);
        String result1 = RandomStringUtils.random(20, 0, 0, true, true, null, rand1);
        String result2 = RandomStringUtils.random(20, 0, 0, true, true, null, rand2);
        assertEquals(result1, result2);
    }

    // Tests Lang-12 bug: random with custom char array and start/end set to 0
    @Test
    public void testRandom_customCharArrayWithZeroStartEnd_doesNotThrowException() {
        char[] chars = new char[]{'a', 'b', 'c'};
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars, new Random(100L));
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    // Tests Lang-12 bug: random with empty char array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyCharArray_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, new char[0]);
    }

    // Tests surrogate pairs generation branch
    @Test
    public void testRandom_surrogatePairCharacters_generatesCorrectLength() {
        // Surrogate range 55296 to 57343
        String result = RandomStringUtils.random(4, 55296, 56192, false, false, null, new Random(42L));
        assertEquals(4, result.length());
    }
}