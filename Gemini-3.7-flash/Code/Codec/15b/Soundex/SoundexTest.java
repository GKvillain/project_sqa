package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SoundexTest {

    private Soundex soundex;

    @Before
    public void setUp() {
        this.soundex = new Soundex();
    }

    // Tests null input handling
    @Test
    public void testSoundex_nullInput_returnsNull() {
        assertNull(this.soundex.soundex(null));
    }

    // Tests empty string input handling
    @Test
    public void testSoundex_emptyString_returnsEmptyString() {
        assertEquals("", this.soundex.soundex(""));
        assertEquals("", this.soundex.soundex("   "));
    }

    // Tests short input that requires zero padding
    @Test
    public void testSoundex_singleAndShortNames_returnsPaddedCode() {
        assertEquals("A000", this.soundex.soundex("A"));
        assertEquals("B000", this.soundex.soundex("B"));
        assertEquals("C000", this.soundex.soundex("C"));
    }

    // Tests standard names encoding correctly
    @Test
    public void testSoundex_standardNames_returnsCorrectSoundexCode() {
        assertEquals("W252", this.soundex.soundex("Washington"));
        assertEquals("L000", this.soundex.soundex("Lee"));
        assertEquals("G362", this.soundex.soundex("Gutierrez"));
        assertEquals("P236", this.soundex.soundex("Pfister"));
        assertEquals("J250", this.soundex.soundex("Jackson"));
        assertEquals("T522", this.soundex.soundex("Tymczak"));
    }

    // Tests consonants separated by H or W with same code
    @Test
    public void testSoundex_hwRuleSameCode_mergesCodes() {
        assertEquals("A261", this.soundex.soundex("Ashcraft"));
        assertEquals("A261", this.soundex.soundex("Ashcroft"));
        assertEquals("B200", this.soundex.soundex("Bougher"));
    }

    // Tests consonants separated by vowels with same code
    @Test
    public void testSoundex_vowelsSeparatingSameCode_doesNotMergeCodes() {
        assertEquals("T252", this.soundex.soundex("Tymczak"));
        assertEquals("H555", this.soundex.soundex("Honeyman"));
    }

    // Tests consecutive identical letters and same code letters
    @Test
    public void testSoundex_adjacentSameCodeLetters_encodedOnce() {
        assertEquals("W452", this.soundex.soundex("Williams"));
        assertEquals("S530", this.soundex.soundex("Smith"));
        assertEquals("S530", this.soundex.soundex("Smyth"));
    }

    // Tests encode(Object) with valid String object
    @Test
    public void testEncode_stringObject_returnsSoundexCodeObject() throws EncoderException {
        final Object result = this.soundex.encode((Object) "Washington");
        assertNotNull(result);
        assertEquals("W252", result);
    }

    // Tests encode(Object) with non-String object throwing EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws EncoderException {
        this.soundex.encode(Integer.valueOf(12345));
    }

    // Tests encode(String) method
    @Test
    public void testEncode_stringParameter_returnsSoundexCode() {
        assertEquals("W252", this.soundex.encode("Washington"));
        assertEquals("S530", this.soundex.encode("Smith"));
    }

    // Tests difference method calculating soundex distance
    @Test
    public void testDifference_similarAndDifferentStrings_returnsExpectedScore() throws EncoderException {
        assertEquals(4, this.soundex.difference("Smith", "Smythe"));
        assertEquals(0, this.soundex.difference("Smith", ""));
        assertEquals(2, this.soundex.difference("Washington", "Lee"));
    }

    // Tests custom mapping with char array constructor
    @Test
    public void testSoundex_customCharArrayMapping_encodesCorrectly() {
        final Soundex customSoundex = new Soundex(Soundex.US_ENGLISH_MAPPING_STRING.toCharArray());
        assertEquals("W252", customSoundex.soundex("Washington"));
    }

    // Tests custom mapping with string constructor
    @Test
    public void testSoundex_customStringMapping_encodesCorrectly() {
        final Soundex customSoundex = new Soundex(Soundex.US_ENGLISH_MAPPING_STRING);
        assertEquals("W252", customSoundex.soundex("Washington"));
    }

    // Tests mapping character beyond custom map length
    @Test(expected = IllegalArgumentException.class)
    public void testSoundex_unmappedCharacter_throwsIllegalArgumentException() {
        final Soundex shortMapSoundex = new Soundex("0123");
        shortMapSoundex.soundex("ZEBRA");
    }

    // Tests deprecated maxLength getter and setter
    @Test
    @SuppressWarnings("deprecation")
    public void testMaxLength_setAndGet_retainsValue() {
        final Soundex sndx = new Soundex();
        assertEquals(4, sndx.getMaxLength());
        sndx.setMaxLength(6);
        assertEquals(6, sndx.getMaxLength());
    }

    // Tests static instance US_ENGLISH
    @Test
    public void testSoundex_usEnglishStaticInstance_encodesCorrectly() {
        assertEquals("W252", Soundex.US_ENGLISH.soundex("Washington"));
    }
}