package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.codec.EncoderException;

public class CaverphoneTest {

    // Tests null input returns default code "1111111111"
    @Test
    public void testCaverphone_nullInput_returnsDefaultCode() {
        assertEquals("1111111111", new Caverphone().caverphone(null));
    }

    // Tests empty string returns default code
    @Test
    public void testCaverphone_emptyInput_returnsDefaultCode() {
        assertEquals("1111111111", new Caverphone().caverphone(""));
    }

    // Tests input with only non-letter characters returns default code
    @Test
    public void testCaverphone_nonLetterOnly_returnsDefaultCode() {
        assertEquals("1111111111", new Caverphone().caverphone("123!@#"));
    }

    // Tests single vowel 'a' produces expected code
    @Test
    public void testCaverphone_singleVowel_returnsExpectedCode() {
        assertEquals("A111111111", new Caverphone().caverphone("a"));
    }

    // Tests single consonant 'b' produces expected code (b -> p)
    @Test
    public void testCaverphone_singleConsonant_returnsExpectedCode() {
        assertEquals("p111111111", new Caverphone().caverphone("b"));
    }

    // Tests start pattern "cough" replaced correctly
    @Test
    public void testCaverphone_startPatternCough_returnsExpectedCode() {
        assertEquals("KF11111111", new Caverphone().caverphone("cough"));
    }

    // Tests start pattern "gn" replaced correctly
    @Test
    public void testCaverphone_startPatternGn_returnsExpectedCode() {
        assertEquals("N111111111", new Caverphone().caverphone("gn"));
    }

    // Tests start pattern "mb" replaced correctly
    @Test
    public void testCaverphone_startPatternMb_returnsExpectedCode() {
        assertEquals("M111111111", new Caverphone().caverphone("mb"));
    }

    // Tests start pattern "enough" replaced correctly
    @Test
    public void testCaverphone_startPatternEnough_returnsExpectedCode() {
        assertEquals("ANF1111111", new Caverphone().caverphone("enough"));
    }

    // Tests start pattern "trough" replaced correctly
    @Test
    public void testCaverphone_startPatternTrough_returnsExpectedCode() {
        assertEquals("TRF1111111", new Caverphone().caverphone("trough"));
    }

    // Tests "tch" replacement in middle of word
    @Test
    public void testCaverphone_tchReplacement_returnsExpectedCode() {
        assertEquals("K111111111", new Caverphone().caverphone("tch"));
    }

    // Tests final 'e' removal at end of word
    @Test
    public void testCaverphone_finalERemoval_removesE() {
        assertEquals("1111111111", new Caverphone().caverphone("e"));
        assertEquals("M111111111", new Caverphone().caverphone("me"));
    }

    // Tests 'y' handling at start of word
    @Test
    public void testCaverphone_yHandling_returnsExpectedCode() {
        assertEquals("A111111111", new Caverphone().caverphone("y"));
    }

    // Tests "gh" becomes "22" and then removed (produces default code)
    @Test
    public void testCaverphone_ghReplacement_removes2() {
        assertEquals("1111111111", new Caverphone().caverphone("gh"));
    }

    // Tests vowel handling (starting with vowels and multiple vowels)
    @Test
    public void testCaverphone_vowelHandling_returnsExpectedCode() {
        assertEquals("A111111111", new Caverphone().caverphone("ae"));
    }

    // Tests encode(Object) with non-String throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncodeObject_nonString_throwsEncoderException() throws EncoderException {
        new Caverphone().encode(123);
    }

    // Tests encode(String) returns same as caverphone(String)
    @Test
    public void testEncodeString_returnsCaverphoneResult() {
        Caverphone c = new Caverphone();
        String input = "hello";
        assertEquals(c.caverphone(input), c.encode(input));
    }

    // Tests isCaverphoneEqual returns true for identical inputs
    @Test
    public void testIsCaverphoneEqual_sameStrings_returnsTrue() {
        assertTrue(new Caverphone().isCaverphoneEqual("hello", "hello"));
    }

    // Tests isCaverphoneEqual returns false for different inputs
    @Test
    public void testIsCaverphoneEqual_differentStrings_returnsFalse() {
        assertFalse(new Caverphone().isCaverphoneEqual("hello", "world"));
    }

    // Tests that the result is always 10 characters long
    @Test
    public void testCaverphone_anyInput_lengthIsTen() {
        assertEquals(10, new Caverphone().caverphone("anyString").length());
    }

    // ==================== New test cases for uncovered parts ====================

    // Tests encode(Object) with a valid String should return same as caverphone
    @Test
    public void testEncodeObject_validString_returnsCaverphoneResult() throws EncoderException {
        Caverphone c = new Caverphone();
        String input = "hello";
        assertEquals(c.caverphone(input), c.encode((Object) input));
    }

    // Tests isCaverphoneEqual with null and null returns true (both become default)
    @Test
    public void testIsCaverphoneEqual_nullAndNull_returnsTrue() {
        assertTrue(new Caverphone().isCaverphoneEqual(null, null));
    }

    // Tests isCaverphoneEqual with null and non-null returns false
    @Test
    public void testIsCaverphoneEqual_nullAndString_returnsFalse() {
        assertFalse(new Caverphone().isCaverphoneEqual(null, "hello"));
    }

    // Tests caverphone with leading/trailing spaces returns default code
    @Test
    public void testCaverphone_inputWithSpaces_returnsDefaultCode() {
        assertEquals("1111111111", new Caverphone().caverphone(" hello "));
    }

    // Tests start pattern "kn" replaced correctly (similar to gn)
    @Test
    public void testCaverphone_startPatternKn_returnsExpectedCode() {
        assertEquals("N111111111", new Caverphone().caverphone("kn"));
    }

    // Tests single 'c' produces expected code (c -> K)
    @Test
    public void testCaverphone_singleC_returnsExpectedCode() {
        assertEquals("K111111111", new Caverphone().caverphone("c"));
    }
}