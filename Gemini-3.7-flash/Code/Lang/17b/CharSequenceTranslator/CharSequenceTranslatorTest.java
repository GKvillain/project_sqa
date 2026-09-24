package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CharSequenceTranslatorTest {

    // Helper translator that does not consume any characters (consumed = 0)
    private static class PassthroughTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0;
        }
    }

    // Helper translator that translates specific characters
    private static class ReplaceCharTranslator extends CharSequenceTranslator {
        private final char target;
        private final String replacement;

        public ReplaceCharTranslator(char target, String replacement) {
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

    // Helper translator that consumes multiple characters
    private static class ReplaceWordTranslator extends CharSequenceTranslator {
        private final String word;
        private final String replacement;

        public ReplaceWordTranslator(String word, String replacement) {
            this.word = word;
            this.replacement = replacement;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (index + word.length() <= input.length()) {
                CharSequence sub = input.subSequence(index, index + word.length());
                if (word.equals(sub.toString())) {
                    out.write(replacement);
                    return word.length();
                }
            }
            return 0;
        }
    }

    // Tests null input returns null
    @Test
    public void testTranslate_nullInput_returnsNull() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        assertNull(translator.translate(null));
    }

    // Tests empty string input returns empty string
    @Test
    public void testTranslate_emptyInput_returnsEmptyString() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        assertEquals("", translator.translate(""));
    }

    // Tests normal translation with consumed == 0
    @Test
    public void testTranslate_noConsumed_returnsOriginalString() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        assertEquals("Hello World", translator.translate("Hello World"));
    }

    // Tests translation when consumed > 0
    @Test
    public void testTranslate_singleCharConsumed_replacesCorrectly() {
        CharSequenceTranslator translator = new ReplaceCharTranslator('a', "X");
        assertEquals("XbXcX", translator.translate("abaca"));
    }

    // Tests translation with multi-character consumed
    @Test
    public void testTranslate_multipleCharsConsumed_replacesCorrectly() {
        CharSequenceTranslator translator = new ReplaceWordTranslator("foo", "bar");
        assertEquals("bar and bar", translator.translate("foo and foo"));
    }

    // Tests surrogate pair translation without replacement (exposes defect Lang-17)
    @Test
    public void testTranslate_surrogatePairUnmodified_preservesSurrogatePair() {
        CharSequenceTranslator translator = new PassthroughTranslator();
        // Supplementary character: \uD83D\uDE00 (smiling face) followed by regular text
        String input = "\uD83D\uDE00 Hello \uD83D\uDE00";
        assertEquals(input, translator.translate(input));
    }

    // Tests surrogate pair at the end of input string (boundary condition for surrogate handling)
    @Test
    public void testTranslate_surrogatePairAtEnd_translatesCorrectly() {
        CharSequenceTranslator translator = new ReplaceCharTranslator('a', "A");
        String input = "a\uD83D\uDE00";
        assertEquals("A\uD83D\uDE00", translator.translate(input));
    }

    // Tests null writer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTranslate_nullWriter_throwsIllegalArgumentException() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        translator.translate("test", null);
    }

    // Tests null input with writer does not throw and leaves writer untouched
    @Test
    public void testTranslate_nullInputWithWriter_doesNothing() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    // Tests empty input with writer leaves writer untouched
    @Test
    public void testTranslate_emptyInputWithWriter_writesNothing() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        StringWriter writer = new StringWriter();
        translator.translate("", writer);
        assertEquals("", writer.toString());
    }

    // Tests writer throwing IOException propagates correctly
    @Test(expected = IOException.class)
    public void testTranslate_writerThrowsIOException_propagatesException() throws IOException {
        CharSequenceTranslator translator = new PassthroughTranslator();
        Writer brokenWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated IO Error");
            }

            @Override
            public void flush() throws IOException {}

            @Override
            public void close() throws IOException {}
        };
        translator.translate("A", brokenWriter);
    }

    // Tests with() method merging multiple translators
    @Test
    public void testWith_multipleTranslators_mergesAndExecutesInOrder() {
        CharSequenceTranslator t1 = new ReplaceCharTranslator('a', "A");
        CharSequenceTranslator t2 = new ReplaceCharTranslator('b', "B");
        CharSequenceTranslator combined = t1.with(t2);

        assertEquals("ABcABc", combined.translate("abcabc"));
    }

    // Tests hex conversion for lower boundary codepoint
    @Test
    public void testHex_zero_returnsZeroString() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    // Tests hex conversion for standard ASCII character
    @Test
    public void testHex_asciiCharacter_returnsUppercaseHex() {
        assertEquals("41", CharSequenceTranslator.hex('A'));
        assertEquals("61", CharSequenceTranslator.hex('a'));
        assertEquals("20", CharSequenceTranslator.hex(' '));
    }

    // Tests hex conversion for supplementary codepoint
    @Test
    public void testHex_supplementaryCodePoint_returnsUppercaseHex() {
        assertEquals("1F600", CharSequenceTranslator.hex(0x1F600));
        assertEquals("FFFF", CharSequenceTranslator.hex(0xFFFF));
    }
}