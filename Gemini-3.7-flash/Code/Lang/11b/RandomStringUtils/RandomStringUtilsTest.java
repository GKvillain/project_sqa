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
    public void testConstructor_default_createsInstance() {
        RandomStringUtils rsu = new RandomStringUtils();
        assertNotNull(rsu);
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
        assertEquals("", RandomStringUtils.random(0, 'a', 'b', 'c'));
    }

    // Tests negative count throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCount_throwsIllegalArgumentException() {
        RandomStringUtils.random(-1);
    }

    // Tests empty character array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyCharArray_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, new char[0]);
    }

    // Tests empty string character set throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyString_throwsIllegalArgumentException() {
        RandomStringUtils.random(5, "");
    }

    // Tests random string of specified length
    @Test
    public void testRandom_positiveCount_returnsCorrectLength() {
        int count = 50;
        String result = RandomStringUtils.random(count);
        assertNotNull(result);
        assertEquals(count, result.length());
    }

    // Tests random ASCII generates characters between 32 and 126 inclusive
    @Test
    public void testRandomAscii_validCount_returnsAsciiPrintableOnly() {
        int count = 100;
        String result = RandomStringUtils.randomAscii(count);
        assertEquals(count, result.length());
        for (char ch : result.toCharArray()) {
            assertTrue("Character '" + ch + "' is not ASCII printable (32-126)", ch >= 32 && ch <= 126);
        }
    }

    // Tests random alphabetic generates only letter characters
    @Test
    public void testRandomAlphabetic_validCount_returnsLettersOnly() {
        int count = 100;
        String result = RandomStringUtils.randomAlphabetic(count);
        assertEquals(count, result.length());
        for (char ch : result.toCharArray()) {
            assertTrue("Character '" + ch + "' is not a letter", Character.isLetter(ch));
        }
    }

    // Tests random alphanumeric generates only letters and digits
    @Test
    public void testRandomAlphanumeric_validCount_returnsLettersAndDigitsOnly() {
        int count = 100;
        String result = RandomStringUtils.randomAlphanumeric(count);
        assertEquals(count, result.length());
        for (char ch : result.toCharArray()) {
            assertTrue("Character '" + ch + "' is not letter or digit", Character.isLetterOrDigit(ch));
        }
    }

    // Tests random numeric generates only digit characters
    @Test
    public void testRandomNumeric_validCount_returnsDigitsOnly() {
        int count = 100;
        String result = RandomStringUtils.randomNumeric(count);
        assertEquals(count, result.length());
        for (char ch : result.toCharArray()) {
            assertTrue("Character '" + ch + "' is not a digit", Character.isDigit(ch));
        }
    }

    // Tests random with boolean flags for letters and numbers
    @Test
    public void testRandom_booleanFlags_returnsMatchingCharacters() {
        int count = 50;
        String lettersOnly = RandomStringUtils.random(count, true, false);
        assertEquals(count, lettersOnly.length());
        for (char ch : lettersOnly.toCharArray()) {
            assertTrue(Character.isLetter(ch));
        }

        String digitsOnly = RandomStringUtils.random(count, false, true);
        assertEquals(count, digitsOnly.length());
        for (char ch : digitsOnly.toCharArray()) {
            assertTrue(Character.isDigit(ch));
        }
    }

    // Tests random with custom character array
    @Test
    public void testRandom_charArray_returnsOnlyCharsFromArray() {
        char[] chars = new char[]{'a', 'b', 'c', 'X', 'Y', 'Z'};
        int count = 50;
        String result = RandomStringUtils.random(count, chars);
        assertEquals(count, result.length());
        String allowed = new String(chars);
        for (char ch : result.toCharArray()) {
            assertTrue("Unexpected character: " + ch, allowed.indexOf(ch) >= 0);
        }
    }

    // Tests random with custom string of characters
    @Test
    public void testRandom_stringChars_returnsOnlyCharsFromString() {
        String allowed = "xyz123";
        int count = 50;
        String result = RandomStringUtils.random(count, allowed);
        assertEquals(count, result.length());
        for (char ch : result.toCharArray()) {
            assertTrue("Unexpected character: " + ch, allowed.indexOf(ch) >= 0);
        }
    }

    // Tests random with null string defaults to all characters
    @Test
    public void testRandom_nullString_returnsCorrectLength() {
        int count = 10;
        String result = RandomStringUtils.random(count, (String) null);
        assertNotNull(result);
        assertEquals(count, result.length());
    }

    // Tests random with null char array defaults to all characters
    @Test
    public void testRandom_nullCharArray_returnsCorrectLength() {
        int count = 10;
        String result = RandomStringUtils.random(count, (char[]) null);
        assertNotNull(result);
        assertEquals(count, result.length());
    }

    // Tests random with explicit start and end range
    @Test
    public void testRandom_rangeSpecified_returnsCharactersWithinRange() {
        int count = 50;
        int start = 'a';
        int end = 'f' + 1;
        String result = RandomStringUtils.random(count, start, end, false, false);
        assertEquals(count, result.length());
        for (char ch : result.toCharArray()) {
            assertTrue("Character out of range: " + ch, ch >= 'a' && ch <= 'f');
        }
    }

    // Tests deterministic output when using seeded Random instance
    @Test
    public void testRandom_seededRandom_returnsDeterministicResult() {
        int count = 20;
        long seed = 123456789L;

        String result1 = RandomStringUtils.random(count, 0, 0, true, true, null, new Random(seed));
        String result2 = RandomStringUtils.random(count, 0, 0, true, true, null, new Random(seed));

        assertNotNull(result1);
        assertEquals(result1, result2);
    }

    // Tests high and low surrogate character handling in random generation
    @Test
    public void testRandom_surrogatePairHandling_generatesValidPair() {
        char[] surrogates = new char[]{'\uD800', '\uDC00'};
        String result = RandomStringUtils.random(2, 0, 0, false, false, surrogates, new Random(1L));
        assertEquals(2, result.length());
    }
}