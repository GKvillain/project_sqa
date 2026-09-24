package org.apache.commons.codec.language;

import java.util.Locale;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.junit.Test;
import static org.junit.Assert.*;

public class SoundexUtilsTest {

    // Tests null input for clean method
    @Test
    public void testClean_nullInput_returnsNull() {
        assertNull(SoundexUtils.clean(null));
    }

    // Tests empty string input for clean method
    @Test
    public void testClean_emptyInput_returnsEmptyString() {
        assertEquals("", SoundexUtils.clean(""));
    }

    // Tests string with all letters converted to uppercase
    @Test
    public void testClean_allLettersLowerCase_returnsUpperCase() {
        assertEquals("HELLO", SoundexUtils.clean("hello"));
    }

    // Tests string containing non-letter characters
    @Test
    public void testClean_mixedCharacters_returnsOnlyUpperLetters() {
        assertEquals("HELLOWORLD", SoundexUtils.clean("Hello, World! 123"));
    }

    // Tests string with no letters
    @Test
    public void testClean_noLetters_returnsEmptyString() {
        assertEquals("", SoundexUtils.clean("1234-!@#$ "));
    }

    // Tests clean method with Turkish locale to detect locale sensitivity defect
    @Test
    public void testClean_turkishLocale_returnsEnglishUpperCase() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals("I", SoundexUtils.clean("i"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    // Tests null first parameter for differenceEncoded method
    @Test
    public void testDifferenceEncoded_nullFirstString_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, "A123"));
    }

    // Tests null second parameter for differenceEncoded method
    @Test
    public void testDifferenceEncoded_nullSecondString_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded("A123", null));
    }

    // Tests both null parameters for differenceEncoded method
    @Test
    public void testDifferenceEncoded_bothNull_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, null));
    }

    // Tests empty strings for differenceEncoded method
    @Test
    public void testDifferenceEncoded_emptyStrings_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded("", ""));
    }

    // Tests identical strings for differenceEncoded method
    @Test
    public void testDifferenceEncoded_identicalStrings_returnsFullLength() {
        assertEquals(4, SoundexUtils.differenceEncoded("S123", "S123"));
    }

    // Tests completely different strings for differenceEncoded method
    @Test
    public void testDifferenceEncoded_completelyDifferentStrings_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded("AAAA", "BBBB"));
    }

    // Tests partially matching strings of different lengths
    @Test
    public void testDifferenceEncoded_differentLengthsPartialMatch_returnsMatchCount() {
        assertEquals(3, SoundexUtils.differenceEncoded("ABCD", "ABCEFG"));
    }

    // Tests difference method using a functional StringEncoder
    @Test
    public void testDifference_validEncoderAndStrings_returnsCorrectCount() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            public Object encode(Object obj) throws EncoderException {
                return encode((String) obj);
            }
            public String encode(String str) throws EncoderException {
                return str != null ? str.toUpperCase() : null;
            }
        };
        assertEquals(3, SoundexUtils.difference(encoder, "test", "team"));
    }

    // Tests difference method when encoder throws EncoderException
    @Test(expected = EncoderException.class)
    public void testDifference_encoderThrowsException_throwsEncoderException() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            public Object encode(Object obj) throws EncoderException {
                throw new EncoderException("Encoding failed");
            }
            public String encode(String str) throws EncoderException {
                throw new EncoderException("Encoding failed");
            }
        };
        SoundexUtils.difference(encoder, "test1", "test2");
    }
}