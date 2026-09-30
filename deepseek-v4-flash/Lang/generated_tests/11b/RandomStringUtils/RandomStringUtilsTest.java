package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class RandomStringUtilsTest {

    // Custom Random that returns predetermined values, cycling through array
    private static class PredictableRandom extends Random {
        private final int[] values;
        private int index;

        PredictableRandom(int[] values) {
            this.values = values;
            this.index = 0;
        }

        @Override
        public int nextInt(int bound) {
            int val = values[index % values.length];
            index++;
            // Ensure value is within [0, bound-1]
            if (bound > 0) {
                return val % bound;
            }
            return val; // will cause IllegalArgumentException downstream
        }
    }

    // ============================= NORMAL CASES =================================

    // Tests random(0) returns empty string
    @Test
    public void testRandom_zeroCount_returnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
    }

    // Tests randomAscii(10) returns string of length 10 with chars in [32,126]
    @Test
    public void testRandomAscii_positiveCount_returnsCorrectRange() {
        String result = RandomStringUtils.randomAscii(10);
        assertEquals(10, result.length());
        for (int i = 0; i < result.length(); i++) {
            char ch = result.charAt(i);
            assertTrue("Char out of range: " + (int) ch, ch >= 32 && ch <= 126);
        }
    }

    // Tests randomAlphabetic(10) returns only letters
    @Test
    public void testRandomAlphabetic_positiveCount_returnsOnlyLetters() {
        String result = RandomStringUtils.randomAlphabetic(10);
        assertEquals(10, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Not a letter: " + result.charAt(i), Character.isLetter(result.charAt(i)));
        }
    }

    // Tests randomAlphanumeric(10) returns letters or digits
    @Test
    public void testRandomAlphanumeric_positiveCount_returnsLettersOrDigits() {
        String result = RandomStringUtils.randomAlphanumeric(10);
        assertEquals(10, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Not letter or digit: " + result.charAt(i),
                       Character.isLetterOrDigit(result.charAt(i)));
        }
    }

    // Tests randomNumeric(10) returns only digits
    @Test
    public void testRandomNumeric_positiveCount_returnsOnlyDigits() {
        String result = RandomStringUtils.randomNumeric(10);
        assertEquals(10, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Not a digit: " + result.charAt(i), Character.isDigit(result.charAt(i)));
        }
    }

    // ============================= BOUNDARY / INVALID CASES =====================

    // Tests random(-1) throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_negativeCount_throwsException() {
        RandomStringUtils.random(-1);
    }

    // Tests random(int, char...) with empty char array throws
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_emptyCharArray_throwsException() {
        RandomStringUtils.random(5, new char[0]);
    }

    // Tests random(0, char[]) returns empty
    @Test
    public void testRandom_zeroCountCharArray_returnsEmpty() {
        assertEquals("", RandomStringUtils.random(0, 'a', 'b'));
    }

    // Tests random(int, char[]) with null array uses default set
    @Test
    public void testRandom_nullCharArray_usesDefault() {
        String result = RandomStringUtils.random(5, (char[]) null);
        assertEquals(5, result.length());
    }

    // Tests random(int, String) with null string uses default set
    @Test
    public void testRandom_nullString_usesDefault() {
        String result = RandomStringUtils.random(5, (String) null);
        assertEquals(5, result.length());
    }

    // Tests gap=0 (start==end) throws IllegalArgumentException from Random.nextInt
    @Test(expected = IllegalArgumentException.class)
    public void testRandom_gapZero_throwsException() {
        RandomStringUtils.random(5, 10, 10, false, false, null, new Random());
    }

    // Tests start=0, end=0, letters=false, numbers=false uses Integer.MAX_VALUE
    @Test
    public void testRandom_startEndZero_lettersAndNumbersFalse_usesWideRange() {
        String result = RandomStringUtils.random(5, 0, 0, false, false, null, new PredictableRandom(new int[]{1000}));
        assertEquals(5, result.length());
    }

    // Tests custom chars array – only those chars are used
    @Test
    public void testRandom_withCharsArray_usesOnlyGivenChars() {
        char[] chars = new char[]{'X', 'Y', 'Z'};
        String result = RandomStringUtils.random(5, 0, 0, false, false, chars, new PredictableRandom(new int[]{1, 2, 0}));
        assertEquals(5, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Unexpected char: " + result.charAt(i),
                       result.charAt(i) == 'X' || result.charAt(i) == 'Y' || result.charAt(i) == 'Z');
        }
    }

    // ============================= SURROGATE / INFINITE LOOP DEFECTS ==============

    // Tests low surrogate (56320-57343) with count=1 and letters=numbers=false
    // This forces the if(count==0) branch, causing infinite loop -> timeout
    @Test(timeout = 1000)
    public void testRandom_surrogateLow_countOne_timeout() {
        // low surrogate value 56320, gap must be >56320, set start=0 end=57344
        RandomStringUtils.random(1, 0, 57344, false, false, null,
                new PredictableRandom(new int[]{56320}));
    }

    // Tests high surrogate (55296-56191) with count=1 -> infinite loop
    @Test(timeout = 1000)
    public void testRandom_surrogateHigh_countOne_timeout() {
        RandomStringUtils.random(1, 0, 57344, false, false, null,
                new PredictableRandom(new int[]{55296}));
    }

    // Tests private high surrogate (56192-56319) with count=1 -> infinite loop
    @Test(timeout = 1000)
    public void testRandom_surrogatePrivateHigh_countOne_timeout() {
        RandomStringUtils.random(1, 0, 57344, false, false, null,
                new PredictableRandom(new int[]{56192}));
    }

    // Tests low surrogate with count=2, should terminate and return length=2
    @Test
    public void testRandom_surrogateLow_countTwo_success() {
        // First value for surrogate index, second for the high surrogate offset (<<128)
        String result = RandomStringUtils.random(2, 0, 57344, false, false, null,
                new PredictableRandom(new int[]{56320, 50}));
        assertEquals(2, result.length());
    }

    // Tests letters=true, numbers=false with a non-letter char (digit '0') -> infinite loop
    @Test(timeout = 1000)
    public void testRandom_lettersTrue_nonLetterChar_timeout() {
        // char '0' = 48, letters=true, numbers=false => filter fails -> count++ infinite
        RandomStringUtils.random(1, 0, 128, true, false, null,
                new PredictableRandom(new int[]{48}));
    }

    // Tests letters=false, numbers=true with a non-digit char (letter 'A') -> infinite loop
    @Test(timeout = 1000)
    public void testRandom_numbersTrue_nonDigitChar_timeout() {
        RandomStringUtils.random(1, 0, 128, false, true, null,
                new PredictableRandom(new int[]{65}));
    }

    // Tests letters=false, numbers=false (no filter) should handle surrogates correctly (no infinite loop)
    // Already covered by previous surrogate tests
}