package org.apache.commons.codec.language;

import static org.junit.Assert.*;

import org.apache.commons.codec.EncoderException;
import org.junit.Test;

public class SoundexTest {

    // Tests normal encoding with standard US English mapping
    @Test
    public void testSoundex_standardName_returnsCorrectCode() {
        Soundex soundex = new Soundex();
        assertEquals("W252", soundex.soundex("Washington"));
        assertEquals("L000", soundex.soundex("Lee"));
        assertEquals("P236", soundex.soundex("Packard"));
    }

    // Tests null input returns null
    @Test
    public void testSoundex_nullInput_returnsNull() {
        Soundex soundex = new Soundex();
        assertNull(soundex.soundex(null));
    }

    // Tests empty string returns empty string
    @Test
    public void testSoundex_emptyString_returnsEmptyString() {
        Soundex soundex = new Soundex();
        assertEquals("", soundex.soundex(""));
    }

    // Tests single character encoding
    @Test
    public void testSoundex_singleCharacter_returnsCharacterWithZeros() {
        Soundex soundex = new Soundex();
        assertEquals("A000", soundex.soundex("A"));
        assertEquals("Z000", soundex.soundex("Z"));
    }

    // Tests two character encoding
    @Test
    public void testSoundex_twoCharacters_returnsCorrectCode() {
        Soundex soundex = new Soundex();
        assertEquals("A500", soundex.soundex("Al"));
    }

    // Tests encoding with vowels (vowels are not encoded typically)
    @Test
    public void testSoundex_vowelsInName_skipsVowels() {
        Soundex soundex = new Soundex();
        assertEquals("A000", soundex.soundex("Aeio"));
    }

    // Tests encoding where same code letters adjacent
    @Test
    public void testSoundex_adjacentSameCode_skipsSecond() {
        Soundex soundex = new Soundex();
        // 'B' and 'F' both map to '1', but 'B' is at index 0, 'F' at index 1 (Ashcraft)
        assertEquals("A261", soundex.soundex("Ashcraft"));
        assertEquals("A261", soundex.soundex("Ashcroft"));
    }

    // Tests HW rule: consonants separated by H or W are treated as one
    @Test
    public void testSoundex_HWSeparator_skipsSecondConsonant() {
        Soundex soundex = new Soundex();
        // 'L' -> 4, 'L' -> 4, separated by 'W' should not encode second 'L'
        assertEquals("L400", soundex.soundex("LL"));
        // More complex: 'L' -> 4, 'L' -> 4, separated by 'W' -> should not encode second 'L'
        assertEquals("L400", soundex.soundex("LWL"));
    }

    // Tests HW rule when first character is H or W
    @Test
    public void testSoundex_firstCharIsHW_ignoresHWPrewRule() {
        Soundex soundex = new Soundex();
        // 'H' is not encoded itself
        assertEquals("H000", soundex.soundex("H"));
        assertEquals("W000", soundex.soundex("W"));
    }

    // Tests encode(String) method
    @Test
    public void testEncode_stringInput_returnsSoundexCode() {
        Soundex soundex = new Soundex();
        assertEquals("W252", soundex.encode("Washington"));
    }

    // Tests encode(Object) method with valid String
    @Test
    public void testEncode_objectString_returnsSoundexCode() throws EncoderException {
        Soundex soundex = new Soundex();
        assertEquals("W252", soundex.encode((Object) "Washington"));
    }

    // Tests encode(Object) with non-String object throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_objectNonString_throwsEncoderException() throws EncoderException {
        Soundex soundex = new Soundex();
        soundex.encode((Object) 123);
    }

    // Tests difference method
    @Test
    public void testDifference_similarNames_returnsHighDifference() throws EncoderException {
        Soundex soundex = new Soundex();
        int diff = soundex.difference("Washington", "Washingtin");
        assertTrue("Expected similarity >= 2", diff >= 2);
    }

    // Tests difference with identical names
    @Test
    public void testDifference_identicalNames_returns4() throws EncoderException {
        Soundex soundex = new Soundex();
        assertEquals(4, soundex.difference("Lee", "Lee"));
    }

    // Tests difference with very different names
    @Test
    public void testDifference_differentNames_returnsLowDifference() throws EncoderException {
        Soundex soundex = new Soundex();
        int diff = soundex.difference("Smith", "Jones");
        assertTrue("Expected low similarity", diff <= 1);
    }

    // Tests character not in mapping throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSoundex_unmappedCharacter_throwsIllegalArgumentException() {
        Soundex soundex = new Soundex();
        soundex.soundex("À");
    }

    // Tests custom mapping constructor with char array
    @Test
    public void testSoundex_customCharArrayMapping_encodesCorrectly() {
        char[] mapping = new char[26];
        for (int i = 0; i < 26; i++) {
            mapping[i] = '0';
        }
        mapping[0] = '1'; // A -> 1
        mapping['L' - 'A'] = '4'; // L -> 4
        Soundex soundex = new Soundex(mapping);
        assertEquals("L000", soundex.soundex("L"));
        assertEquals("1000", soundex.soundex("A"));
        // 'B' maps to '0' in custom mapping, so should not appear
        assertEquals("B000", soundex.soundex("B"));
    }

    // Tests custom mapping constructor with String
    @Test
    public void testSoundex_customStringMapping_encodesCorrectly() {
        String mapping = "01230120022455012623010202";
        Soundex soundex = new Soundex(mapping);
        assertEquals("W252", soundex.soundex("Washington"));
    }

    // Tests encoding with letters that map to zero
    @Test
    public void testSoundex_lettersMapToZero_skipsThem() {
        Soundex soundex = new Soundex();
        // 'H' and 'W' map to '0' and vowels also map to '0'
        assertEquals("A000", soundex.soundex("A"));
        assertEquals("H000", soundex.soundex("H"));
        assertEquals("W000", soundex.soundex("W"));
    }

    // Tests long string encoding truncates to 4 characters
    @Test
    public void testSoundex_longString_truncatesToFourCharacters() {
        Soundex soundex = new Soundex();
        // "Washington" gives "W252", long name should still be 4 chars
        String result = soundex.soundex("Washington");
        assertEquals(4, result.length());
        assertEquals("W252", result);
    }

    // Tests encoding with hyphen or apostrophe? Not in spec, but skipped due to mapping letter only
    @Test
    public void testSoundex_specialCharacters_ignoredByClean() {
        Soundex soundex = new Soundex();
        // SoundexUtils.clean removes non-letter characters, so "O'Brien" becomes "OBRIEN"
        assertEquals("O165", soundex.soundex("O'Brien"));
    }

    // Tests boundary: maximum length of output is 4
    @Test
    public void testSoundex_outputAlwaysFourChars() {
        Soundex soundex = new Soundex();
        assertEquals(4, soundex.soundex("ABCDEFGHIJKLMNOPQRSTUVWXYZ").length());
    }

    // ================== New test cases for uncovered parts ==================

    // Tests getMaxLength default value
    @Test
    public void testGetMaxLength_defaultIsFour() {
        Soundex soundex = new Soundex();
        assertEquals(4, soundex.getMaxLength());
    }

    // Tests setMaxLength and its effect on encoded length
    @Test
    public void testSetMaxLength_updatesOutputLength() {
        Soundex soundex = new Soundex();
        soundex.setMaxLength(5);
        assertEquals(5, soundex.getMaxLength());
        String result = soundex.soundex("Washington");
        assertEquals(5, result.length());
        assertEquals('W', result.charAt(0));
    }

    // Tests setMaxLength with a smaller value
    @Test
    public void testSetMaxLength_smallerLength_returnsTruncatedCode() {
        Soundex soundex = new Soundex();
        soundex.setMaxLength(3);
        assertEquals(3, soundex.getMaxLength());
        assertEquals("W25", soundex.soundex("Washington"));
    }

    // Tests setMaxLength with negative value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaxLength_negative_throwsException() {
        Soundex soundex = new Soundex();
        soundex.setMaxLength(-1);
    }

    // Tests setMaxLength with zero throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaxLength_zero_throwsException() {
        Soundex soundex = new Soundex();
        soundex.setMaxLength(0);
    }

    // Tests difference with null first argument
    @Test(expected = IllegalArgumentException.class)
    public void testDifference_firstNull_throwsException() throws EncoderException {
        Soundex soundex = new Soundex();
        soundex.difference(null, "Smith");
    }

    // Tests difference with null second argument
    @Test(expected = IllegalArgumentException.class)
    public void testDifference_secondNull_throwsException() throws EncoderException {
        Soundex soundex = new Soundex();
        soundex.difference("Smith", null);
    }

    // Tests difference with both null arguments
    @Test(expected = IllegalArgumentException.class)
    public void testDifference_bothNull_throwsException() throws EncoderException {
        Soundex soundex = new Soundex();
        soundex.difference(null, null);
    }

    // Tests constructor with mapping string too short
    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMappingStringTooShort_throwsException() {
        String shortMapping = "0123012002245501262301020"; // 25 chars
        new Soundex(shortMapping);
    }

    // Tests constructor with mapping string too long
    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMappingStringTooLong_throwsException() {
        String longMapping = "012301200224550126230102020"; // 27 chars
        new Soundex(longMapping);
    }

    // Tests constructor with char array mapping too short
    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMappingCharArrTooShort_throwsException() {
        char[] mapping = new char[25];
        new Soundex(mapping);
    }

    // Tests constructor with char array mapping too long
    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMappingCharArrTooLong_throwsException() {
        char[] mapping = new char[27];
        new Soundex(mapping);
    }
}