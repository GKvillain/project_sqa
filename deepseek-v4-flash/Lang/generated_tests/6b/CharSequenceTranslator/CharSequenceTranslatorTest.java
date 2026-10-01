package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

public class CharSequenceTranslatorTest {

    // Tests null input returns null for translate(CharSequence)
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

    // Tests empty input returns empty string
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

    // Tests null Writer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTranslate_writerNull_throwsIllegalArgumentException() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        translator.translate("test", null);
    }

    // Tests null input in translate(Writer) does not call Writer
    @Test
    public void testTranslate_nullInput_doesNothing() throws IOException {
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

    // Tests empty input in translate(Writer) produces no output
    @Test
    public void testTranslate_emptyInput_doesNothing() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate("", writer);
        assertEquals("", writer.toString());
    }

    // Tests consumed == 0 branch for BMP character
    @Test
    public void testTranslate_consumedZero_writesSingleBMPChar() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate("A", writer);
        assertEquals("A", writer.toString());
    }

    // Tests consumed == 0 branch for supplementary character (surrogate pair)
    @Test
    public void testTranslate_consumedZero_writesSupplementaryChar() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter writer = new StringWriter();
        String supp = "\uD840\uDC00"; // U+20000
        translator.translate(supp, writer);
        assertEquals(supp, writer.toString());
    }

    // Tests consumed > 0 branch – final method does not write anything
    @Test
    public void testTranslate_consumedPositive_noWriteFromFinalMethod() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 1;
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate("A", writer);
        assertEquals("", writer.toString());
    }

    // Tests defect: consumed multiple char units (surrogate pair) with for loop charCount bug.
    // In defective version, 'A' after the surrogate pair is skipped.
    @Test
    public void testTranslate_consumedMultiple_withSupplementary_skipsRemainingChars() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0) {
                    out.write(input.subSequence(index, index + 2).toString());
                    return 2;
                } else {
                    return 0;
                }
            }
        };
        StringWriter writer = new StringWriter();
        String input = "\uD840\uDC00A";
        translator.translate(input, writer);
        assertEquals("\uD840\uDC00A", writer.toString());
    }

    // Tests IOException from abstract method is wrapped in RuntimeException
    @Test(expected = RuntimeException.class)
    public void testTranslate_ioException_wrappedInRuntimeException() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw new IOException("test");
            }
        };
        translator.translate("test");
    }

    // Tests with() merges translators and resulting object works
    @Test
    public void testWith_mergesTranslators() {
        CharSequenceTranslator translator1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator translator2 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator merged = translator1.with(translator2);
        assertNotNull(merged);
        try {
            merged.translate("test", new StringWriter());
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    // Tests hex() returns uppercase hexadecimal
    @Test
    public void testHex_codepoint_returnsUppercase() {
        assertEquals("41", CharSequenceTranslator.hex(65));
        assertEquals("FF", CharSequenceTranslator.hex(255));
        assertEquals("10FFFF", CharSequenceTranslator.hex(0x10FFFF));
    }

    // Tests hex() with zero
    @Test
    public void testHex_zero_returnsZero() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    // Tests hex() with minimum and maximum codepoints
    @Test
    public void testHex_minMaxCodepoint_returnsCorrectHex() {
        assertEquals("0", CharSequenceTranslator.hex(0));
        assertEquals("10FFFF", CharSequenceTranslator.hex(0x10FFFF));
    }

    // Tests multiple iterations mixing consumed == 0 and consumed > 0
    @Test
    public void testTranslate_multipleIterations_consumedZeroAndPositive() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate("ABC", writer);
        assertEquals("BC", writer.toString());
    }

    // Tests loop boundary when input length is 1 (BMP char, consumed 0)
    @Test
    public void testTranslate_singleChar_consumedZero() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate("X", writer);
        assertEquals("X", writer.toString());
    }
}