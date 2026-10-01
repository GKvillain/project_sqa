package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CharSequenceTranslatorTest {

    // Helper implementation for testing
    private static class SimpleTranslator extends CharSequenceTranslator {
        private final char target;
        private final String replacement;

        SimpleTranslator(char target, String replacement) {
            this.target = target;
            this.replacement = replacement;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (input.charAt(index) == target) {
                out.write(replacement);
                return 1;
            }
            return 0;
        }
    }

    // Helper implementation consuming surrogate pair / multiple characters
    private static class MultiCharTranslator extends CharSequenceTranslator {
        private final String match;
        private final String replacement;
        private final int consumedCodePoints;

        MultiCharTranslator(String match, String replacement, int consumedCodePoints) {
            this.match = match;
            this.replacement = replacement;
            this.consumedCodePoints = consumedCodePoints;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (input.toString().startsWith(match, index)) {
                out.write(replacement);
                return consumedCodePoints;
            }
            return 0;
        }
    }

    // Tests translate(CharSequence) with null input returning null
    @Test
    public void testTranslateString_nullInput_returnsNull() {
        CharSequenceTranslator translator = new SimpleTranslator('a', "b");
        assertNull(translator.translate(null));
    }

    // Tests translate(CharSequence) with empty input returning empty string
    @Test
    public void testTranslateString_emptyInput_returnsEmptyString() {
        CharSequenceTranslator translator = new SimpleTranslator('a', "b");
        assertEquals("", translator.translate(""));
    }

    // Tests translate(CharSequence) with normal string input
    @Test
    public void testTranslateString_validInput_translatesSuccessfully() {
        CharSequenceTranslator translator = new SimpleTranslator('a', "X");
        assertEquals("XbcX", translator.translate("abca"));
    }

    // Tests translate(CharSequence, Writer) with null Writer throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTranslateWriter_nullWriter_throwsIllegalArgumentException() throws IOException {
        CharSequenceTranslator translator = new SimpleTranslator('a', "b");
        translator.translate("test", null);
    }

    // Tests translate(CharSequence, Writer) with null input doing nothing
    @Test
    public void testTranslateWriter_nullInput_writerRemainsEmpty() throws IOException {
        CharSequenceTranslator translator = new SimpleTranslator('a', "b");
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    // Tests translate(CharSequence, Writer) with empty input
    @Test
    public void testTranslateWriter_emptyInput_writerRemainsEmpty() throws IOException {
        CharSequenceTranslator translator = new SimpleTranslator('a', "b");
        StringWriter writer = new StringWriter();
        translator.translate("", writer);
        assertEquals("", writer.toString());
    }

    // Tests translate(CharSequence, Writer) when translator consumes 0 characters (default pass-through)
    @Test
    public void testTranslateWriter_noMatchConsumedZero_passesThroughUnchanged() throws IOException {
        CharSequenceTranslator translator = new SimpleTranslator('z', "X");
        StringWriter writer = new StringWriter();
        translator.translate("hello", writer);
        assertEquals("hello", writer.toString());
    }

    // Tests translate(CharSequence, Writer) with surrogate pair passed through when consumed == 0
    @Test
    public void testTranslateWriter_surrogatePairPassThrough_writesSurrogatePair() throws IOException {
        CharSequenceTranslator translator = new SimpleTranslator('z', "X");
        StringWriter writer = new StringWriter();
        String surrogatePair = "\uD83D\uDE30"; // Supplementary character U+1F630
        translator.translate(surrogatePair, writer);
        assertEquals(surrogatePair, writer.toString());
    }

    // Tests translate(CharSequence, Writer) with surrogate pair consumed by translator
    @Test
    public void testTranslateWriter_surrogatePairConsumed_translatesCorrectly() throws IOException {
        String surrogatePair = "\uD83D\uDE30";
        CharSequenceTranslator translator = new MultiCharTranslator(surrogatePair, "[EMOJI]", 1);
        StringWriter writer = new StringWriter();
        translator.translate("A" + surrogatePair + "B", writer);
        assertEquals("A[EMOJI]B", writer.toString());
    }

    // Tests translate(CharSequence, Writer) consuming multiple code points
    @Test
    public void testTranslateWriter_multipleCodePointsConsumed_advancesPositionCorrectly() throws IOException {
        CharSequenceTranslator translator = new MultiCharTranslator("abc", "XYZ", 3);
        StringWriter writer = new StringWriter();
        translator.translate("123abc456", writer);
        assertEquals("123XYZ456", writer.toString());
    }

    // Tests with() merging multiple translators
    @Test
    public void testWith_multipleTranslators_translatesInOrder() {
        CharSequenceTranslator t1 = new SimpleTranslator('a', "1");
        CharSequenceTranslator t2 = new SimpleTranslator('b', "2");
        CharSequenceTranslator merged = t1.with(t2);

        assertNotNull(merged);
        assertEquals("12c12", merged.translate("abcab"));
    }

    // Tests hex() with standard ASCII character
    @Test
    public void testHex_asciiCodepoint_returnsUppercaseHex() {
        assertEquals("41", CharSequenceTranslator.hex('A'));
    }

    // Tests hex() with zero codepoint
    @Test
    public void testHex_zeroCodepoint_returnsZero() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    // Tests hex() with multi-digit hexadecimal requiring uppercase conversion
    @Test
    public void testHex_lowercaseHexValues_returnsUppercaseString() {
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("FF", CharSequenceTranslator.hex(255));
    }

    // Tests hex() with supplementary codepoint (surrogate code point range)
    @Test
    public void testHex_supplementaryCodepoint_returnsCorrectHex() {
        assertEquals("1F630", CharSequenceTranslator.hex(0x1F630));
    }
}