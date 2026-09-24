package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import static org.junit.Assert.*;

public class CharSequenceTranslatorTest {

    // Tests translate helper with null input
    @Test
    public void testTranslate_nullInput_returnsNull() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertNull(translator.translate(null));
    }

    // Tests translate helper with empty input
    @Test
    public void testTranslate_emptyInput_returnsEmptyString() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertEquals("", translator.translate(""));
    }

    // Tests translate helper with normal string where nothing is consumed
    @Test
    public void testTranslate_noConsumption_copiesInput() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertEquals("test", translator.translate("test"));
    }

    // Tests translate helper with custom replacement
    @Test
    public void testTranslate_singleCharConsumed_replacesCharacter() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write("XYZ");
                    return 1;
                }
                return 0;
            }
        };
        assertEquals("XYZbcXYZ", translator.translate("abcva".substring(0, 4) + "a"));
    }

    // Tests translate with null writer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTranslateWriter_nullWriter_throwsIllegalArgumentException() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        translator.translate("test", null);
    }

    // Tests translate with null input and valid writer
    @Test
    public void testTranslateWriter_nullInput_doesNothing() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    // Tests surrogate pair consumption (Lang-6 regression test)
    @Test
    public void testTranslate_surrogatePairConsumed_translatesCorrectly() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0 && Character.codePointAt(input, index) == 0x1F630) {
                    out.write("[SMILEY]");
                    return 2;
                }
                return 0;
            }
        };
        String surrogate = "\uD83D\uDE30";
        assertEquals("[SMILEY]", translator.translate(surrogate));
    }

    // Tests consuming multiple characters sequentially
    @Test
    public void testTranslate_multipleCharsConsumed_advancesPositionCorrectly() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0 && input.subSequence(index, index + 3).toString().equals("foo")) {
                    out.write("bar");
                    return 3;
                }
                return 0;
            }
        };
        assertEquals("barbaz", translator.translate("foobaz"));
    }

    // Tests merging with other translators using with()
    @Test
    public void testWith_multipleTranslators_mergesTranslators() {
        CharSequenceTranslator t1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == '1') {
                    out.write("one");
                    return 1;
                }
                return 0;
            }
        };
        CharSequenceTranslator t2 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == '2') {
                    out.write("two");
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator combined = t1.with(t2);
        assertNotNull(combined);
        assertEquals("onetwo3", combined.translate("123"));
    }

    // Tests hex conversion for basic ASCII codepoint
    @Test
    public void testHex_asciiCodePoint_returnsUpperHex() {
        assertEquals("41", CharSequenceTranslator.hex('A'));
        assertEquals("61", CharSequenceTranslator.hex('a'));
        assertEquals("20", CharSequenceTranslator.hex(' '));
    }

    // Tests hex conversion for zero codepoint
    @Test
    public void testHex_zeroCodePoint_returnsZeroHex() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    // Tests hex conversion for supplementary codepoint
    @Test
    public void testHex_supplementaryCodePoint_returnsUpperHex() {
        assertEquals("1F630", CharSequenceTranslator.hex(0x1F630));
    }
}