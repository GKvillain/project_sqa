package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link CharSequenceTranslator}.
 */
public class CharSequenceTranslatorTest {

    private static class PassthroughTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0;
        }
    }

    private static class FixedReplaceTranslator extends CharSequenceTranslator {
        private final String search;
        private final String replacement;

        public FixedReplaceTranslator(String search, String replacement) {
            this.search = search;
            this.replacement = replacement;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (index + search.length() <= input.length()) {
                String sub = input.subSequence(index, index + search.length()).toString();
                if (sub.equals(search)) {
                    out.write(replacement);
                    return Character.codePointCount(search, 0, search.length());
                }
            }
            return 0;
        }
    }

    private static class CustomIOExceptionWriter extends Writer {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("Simulated IOException");
        }

        @Override
        public void flush() throws IOException {
            throw new IOException("Simulated IOException");
        }

        @Override
        public void close() throws IOException {
            throw new IOException("Simulated IOException");
        }
    }

    // Tests translate method with null CharSequence input returning null
    @Test
    public void testTranslate_nullInput_returnsNull() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        assertNull(translator.translate(null));
    }

    // Tests translate method with empty string input returning empty string
    @Test
    public void testTranslate_emptyString_returnsEmptyString() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        assertEquals("", translator.translate(""));
    }

    // Tests translate method with normal ASCII string without modifications
    @Test
    public void testTranslate_normalString_returnsSameString() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        assertEquals("Hello World", translator.translate("Hello World"));
    }

    // Tests translate with Writer when Writer is null, expecting IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTranslate_nullWriter_throwsIllegalArgumentException() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        translator.translate("test", null);
    }

    // Tests translate with Writer when input is null, doing nothing
    @Test
    public void testTranslate_nullInputWithWriter_writesNothing() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    // Tests translate with Writer propagating IOException when Writer throws
    @Test(expected = IOException.class)
    public void testTranslate_writerThrowsIOException_propagatesException() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        translator.translate("test", new CustomIOExceptionWriter());
    }

    // Tests surrogate pairs translation (regression test for Defects4J Lang-17)
    @Test
    public void testTranslate_surrogatePairInMiddleOfString_preservesCharacters() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        // Musical G-Clef character \uD834\uDD1E (codepoint 0x1D11E, surrogate pair)
        String input = "a\uD834\uDD1Eb";
        String result = translator.translate(input);
        assertEquals(input, result);
    }

    // Tests supplementary character at the beginning and end of string
    @Test
    public void testTranslate_multipleSurrogatePairs_preservesCharacters() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        String input = "\uD83D\uDE30Test\uD83D\uDC31";
        String result = translator.translate(input);
        assertEquals(input, result);
    }

    // Tests translation when codepoints are consumed (positive consumed branch)
    @Test
    public void testTranslate_consumedCodepoints_translatesAndAdvancesCorrectly() {
        CharSequenceTranslator translator = new FixedReplaceTranslator("foo", "bar");
        assertEquals("bar baz bar", translator.translate("foo baz foo"));
    }

    // Tests translation when consumed length covers surrogate pairs
    @Test
    public void testTranslate_consumedSurrogatePair_replacesCorrectly() {
        CharSequenceTranslator translator = new FixedReplaceTranslator("\uD834\uDD1E", "CLEF");
        assertEquals("Note: CLEF done", translator.translate("Note: \uD834\uDD1E done"));
    }

    // Tests with() method merging multiple translators
    @Test
    public void testWith_multipleTranslators_mergesAndExecutesInOrder() {
        CharSequenceTranslator t1 = new FixedReplaceTranslator("a", "1");
        CharSequenceTranslator t2 = new FixedReplaceTranslator("b", "2");
        CharSequenceTranslator combined = t1.with(t2);

        assertEquals("12c", combined.translate("abc"));
    }

    // Tests hex method with small codepoints
    @Test
    public void testHex_smallCodepoints_returnsUpperCaseHex() {
        assertEquals("0", CharSequenceTranslator.hex(0));
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("F", CharSequenceTranslator.hex(15));
        assertEquals("10", CharSequenceTranslator.hex(16));
        assertEquals("20", CharSequenceTranslator.hex(32));
    }

    // Tests hex method with boundary codepoints
    @Test
    public void testHex_largeCodepoints_returnsUpperCaseHex() {
        assertEquals("FFFF", CharSequenceTranslator.hex(0xFFFF));
        assertEquals("10FFFF", CharSequenceTranslator.hex(0x10FFFF));
    }
}