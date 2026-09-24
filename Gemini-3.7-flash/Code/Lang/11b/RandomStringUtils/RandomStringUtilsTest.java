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

    // Tests default constructor
    @Test
    public void testConstructor_publicInstanceCreation_instantiated() {
        RandomStringUtils instance = new RandomStringUtils();
        assertNotNull(instance);
    }

    // Tests count equals zero returns empty string
    @Test
    public void testRandom_countZero_returnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
        assertEquals("", RandomStringUtils.random(0, "abc"));
        assertEquals("", RandomStringUtils.random(0, new char[]{'a', 'b'}));
        assertEquals("", RandomStringUtils.random(0, 0, 0, false, false, (char[]) null, new Random()));
    }

    // Tests negative count throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCount_throwsIllegalArgumentException() {
        RandomStringUtils.random(-1);
    }

    // Tests negative count with custom random throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCountWithRandom_throwsIllegalArgumentException() {
        RandomStringUtils.random(-5, 0, 0, false, false, null, new Random());
    }

    // Tests empty char array throws IllegalArgumentException (LANG-807 / Defects4J Lang-11)
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyCharArray_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, new char[0]);
    }

    // Tests empty string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyString_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, "");
    }

    // Tests random ASCII string generation
    @Test
    public void testRandomAscii_positiveCount_returnsAsciiCharacters() {
        int count = 50;
        String result = RandomStringUtils.randomAscii(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 32 && c <= 126);
        }
    }

    // Tests random alphabetic string generation
    @Test
    public void testRandomAlphabetic_positiveCount_returnsLettersOnly() {
        int count = 50;
        String result = RandomStringUtils.randomAlphabetic(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    // Tests random numeric string generation
    @Test
    public void testRandomNumeric_positiveCount_returnsDigitsOnly() {
        int count = 50;
        String result = RandomStringUtils.randomNumeric(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    // Tests random alphanumeric string generation
    @Test
    public void testRandomAlphanumeric_positiveCount_returnsLettersAndDigits() {
        int count = 50;
        String result = RandomStringUtils.randomAlphanumeric(count);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    // Tests random with null string chooses from all characters
    @Test
    public void testRandom_nullString_returnsRandomString() {
        String result = RandomStringUtils.random(10, (String) null);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    // Tests random with null char array chooses from all characters
    @Test
    public void testRandom_nullCharArray_returnsRandomString() {
        String result = RandomStringUtils.random(10, (char[]) null);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    // Tests random with specific character set
    @Test
    public void testRandom_specificChars_returnsOnlySpecifiedChars() {
        char[] set = new char[]{'a', 'b', 'c'};
        String result = RandomStringUtils.random(30, set);
        assertEquals(30, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    // Tests random with specific string character set
    @Test
    public void testRandom_specificStringChars_returnsOnlySpecifiedChars() {
        String set = "XYZ123";
        String result = RandomStringUtils.random(20, set);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(set.indexOf(c) >= 0);
        }
    }

    // Tests reproducible generation with seeded Random
    @Test
    public void testRandom_seededRandom_returnsReproducibleSequence() {
        Random random1 = new Random(12345L);
        Random random2 = new Random(12345L);
        String str1 = RandomStringUtils.random(20, 0, 0, true, true, null, random1);
        String str2 = RandomStringUtils.random(20, 0, 0, true, true, null, random2);
        assertEquals(str1, str2);
    }

    // Tests random with letters and numbers both false
    @Test
    public void testRandom_lettersAndNumbersFalse_generatesCharacters() {
        String result = RandomStringUtils.random(10, false, false);
        assertEquals(10, result.length());
    }

    // Tests random with start and end range
    @Test
    public void testRandom_startAndEndRange_returnsCharsInRange() {
        int start = 'a';
        int end = 'f';
        String result = RandomStringUtils.random(20, start, end, false, false);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= start && c < end);
        }
    }

    // Tests surrogate characters handling
    @Test
    public void testRandom_surrogateRange_handlesSurrogatePairs() {
        char[] surrogates = new char[]{55296, 56320};
        String result = RandomStringUtils.random(2, 0, surrogates.length, false, false, surrogates, new Random(100L));
        assertEquals(2, result.length());
    }
}