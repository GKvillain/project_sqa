package org.apache.commons.codec.language;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import java.util.Locale;

public class SoundexUtilsTest {

    // Tests clean with null input -> returns null
    @Test
    public void testClean_nullInput_returnsNull() {
        assertNull(SoundexUtils.clean(null));
    }

    // Tests clean with empty string -> returns empty string
    @Test
    public void testClean_emptyString_returnsEmptyString() {
        assertEquals("", SoundexUtils.clean(""));
    }

    // Tests clean with only upper case letters -> returns same string
    @Test
    public void testClean_onlyUpperCaseLetters_returnsSame() {
        assertEquals("ABCD", SoundexUtils.clean("ABCD"));
    }

    // Tests clean with mixed case letters -> returns upper case
    @Test
    public void testClean_mixedCaseLetters_returnsUpperCase() {
        assertEquals("TEST", SoundexUtils.clean("TeSt"));
    }

    // Tests clean with lower case 'i' in Turkish locale to expose defect (uses default locale)
    @Test
    public void testClean_lowercaseIWithTurkishLocale_returnsUpperCaseI() {
        Locale original = Locale.getDefault();
        Locale.setDefault(new Locale("tr"));
        try {
            // In Turkish locale, "i".toUpperCase() returns "İ" (I with dot)
            // Bug: str.toUpperCase() without Locale.ENGLISH in clean()
            assertEquals("I", SoundexUtils.clean("i"));
        } finally {
            Locale.setDefault(original);
        }
    }

    // Tests clean with letters, spaces and digits -> returns letters only uppercase
    @Test
    public void testClean_withSpacesAndDigits_returnsLettersOnlyUpperCase() {
        assertEquals("ABC", SoundexUtils.clean("a1 b2 c3"));
    }

    // Tests clean with only non-letter characters -> returns empty string
    @Test
    public void testClean_onlyNonLetters_returnsEmptyString() {
        assertEquals("", SoundexUtils.clean("123!@#"));
    }

    // Tests clean with mixed letters and punctuation -> returns uppercase letters
    @Test
    public void testClean_mixedLettersPunctuation_returnsUpperCaseLetters() {
        assertEquals("HELLOWORLD", SoundexUtils.clean("Hello, World!"));
    }

    // Tests differenceEncoded with both null -> returns 0
    @Test
    public void testDifferenceEncoded_bothNull_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, null));
    }

    // Tests differenceEncoded with first null -> returns 0
    @Test
    public void testDifferenceEncoded_firstNull_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, "abc"));
    }

    // Tests differenceEncoded with second null -> returns 0
    @Test
    public void testDifferenceEncoded_secondNull_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded("abc", null));
    }

    // Tests differenceEncoded with equal strings -> returns length
    @Test
    public void testDifferenceEncoded_equalStrings_returnsLength() {
        assertEquals(4, SoundexUtils.differenceEncoded("ABCD", "ABCD"));
    }

    // Tests differenceEncoded with partially matching strings -> returns correct count
    @Test
    public void testDifferenceEncoded_partialMatch_returnsCorrectCount() {
        assertEquals(2, SoundexUtils.differenceEncoded("ABCD", "ABXY"));
    }

    // Tests differenceEncoded with no matching characters -> returns 0
    @Test
    public void testDifferenceEncoded_noMatch_returnsZero() {
        assertEquals(0, SoundexUtils.differenceEncoded("ABCD", "WXYZ"));
    }

    // Tests differenceEncoded with different lengths -> matches up to min length
    @Test
    public void testDifferenceEncoded_differentLengths_returnsCountOfMinLength() {
        assertEquals(3, SoundexUtils.differenceEncoded("ABCDE", "ABC"));
    }

    // Tests difference with a working StringEncoder -> returns correct difference
    @Test
    public void testDifference_normalCase_returnsCorrectDifference() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                if (source == null) return null;
                String upper = source.toUpperCase(Locale.ENGLISH);
                return upper.length() >= 4 ? upper.substring(0, 4) : upper;
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        // "Smith" -> "SMIT", "Smyth" -> "SMYT", first two chars match
        assertEquals(2, SoundexUtils.difference(encoder, "Smith", "Smyth"));
    }

    // Tests difference when encoder throws exception -> throws EncoderException
    @Test(expected = EncoderException.class)
    public void testDifference_encoderThrowsException_throwsEncoderException() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                throw new EncoderException("simulated error");
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        SoundexUtils.difference(encoder, "test", "test");
    }

    // ========== New tests for uncovered parts ==========

    // Tests difference with both null inputs -> returns 0
    @Test
    public void testDifference_bothNull_returnsZero() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                return source; // null in, null out
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        assertEquals(0, SoundexUtils.difference(encoder, null, null));
    }

    // Tests difference with first null input -> returns 0
    @Test
    public void testDifference_firstNull_returnsZero() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                return source;
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        assertEquals(0, SoundexUtils.difference(encoder, null, "test"));
    }

    // Tests difference with second null input -> returns 0
    @Test
    public void testDifference_secondNull_returnsZero() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                return source;
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        assertEquals(0, SoundexUtils.difference(encoder, "test", null));
    }

    // Tests difference with encoder returning null for normal input -> returns 0
    @Test
    public void testDifference_encoderReturnsNull_returnsZero() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                return null; // Simulate encoder returning null
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        assertEquals(0, SoundexUtils.difference(encoder, "test1", "test2"));
    }

    // Tests difference with exact match -> returns length of encoded string
    @Test
    public void testDifference_exactMatch_returnsLength() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public String encode(String source) throws EncoderException {
                return "ABCD";
            }
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }
        };
        assertEquals(4, SoundexUtils.difference(encoder, "anything", "anything"));
    }
}