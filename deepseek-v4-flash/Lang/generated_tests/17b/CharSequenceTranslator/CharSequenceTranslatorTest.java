package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;

public class CharSequenceTranslatorTest {

    private static final CharSequenceTranslator IDENTITY_TRANSLATOR = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0;
        }
    };

    private static final CharSequenceTranslator HEX_WRITER = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            int cp = Character.codePointAt(input, index);
            out.write('[');
            out.write(hex(cp));
            out.write(']');
            return 1;
        }
    };

    private static final CharSequenceTranslator CONSUMED_TWO = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 2;
        }
    };

    @Test
    public void testTranslate_nullInput_returnsNull() {
        assertNull(IDENTITY_TRANSLATOR.translate(null));
    }

    @Test
    public void testTranslate_emptyInput_returnsEmptyString() {
        assertEquals("", IDENTITY_TRANSLATOR.translate(""));
    }

    @Test
    public void testTranslate_normalAsciiInput_usesTranslator() {
        String input = "AB";
        String expected = "[41][42]";
        assertEquals(expected, HEX_WRITER.translate(input));
    }

    @Test
    public void testTranslate_surrogatePair_consumed0_writesOriginalChar() {
        String input = "\uD800\uDC00";
        assertEquals(input, IDENTITY_TRANSLATOR.translate(input));
    }

    @Test
    public void testTranslate_surrogatePair_consumed1_posUpdate() {
        String input = "\uD800\uDC00A";
        String expected = "[10000][41]";
        assertEquals(expected, HEX_WRITER.translate(input));
    }

    @Test
    public void testTranslate_surrogatePair_mixedLength() {
        String input = "\uD800\uDC00BC";
        String expected = "[10000][42][43]";
        assertEquals(expected, HEX_WRITER.translate(input));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTranslate_nullWriter_throwsIllegalArgumentException() throws IOException {
        IDENTITY_TRANSLATOR.translate("test", null);
    }

    @Test
    public void testTranslate_nullInputWithWriter_doesNothing() throws IOException {
        StringWriter sw = new StringWriter();
        IDENTITY_TRANSLATOR.translate(null, sw);
        assertEquals("", sw.toString());
    }

    @Test
    public void testTranslate_writerThrowsIOException_internally() throws IOException {
        Writer faulty = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("forced");
            }
            @Override
            public void flush() {}
            @Override
            public void close() {}
        };
        try {
            IDENTITY_TRANSLATOR.translate("x", faulty);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(expected = RuntimeException.class)
    public void testTranslate_writerThrowsIOException_inTranslateMethod() {
        CharSequenceTranslator throwingTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw new IOException("from abstract");
            }
        };
        throwingTranslator.translate("anything");
    }

    @Test
    public void testHex_zeroCodepoint_returnsZero() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    @Test
    public void testHex_positiveCodepoint_returnsUppercase() {
        assertEquals("FF", CharSequenceTranslator.hex(255));
    }

    @Test
    public void testHex_maxCodepoint_returnsValidUppercase() {
        assertEquals("10FFFF", CharSequenceTranslator.hex(0x10FFFF));
    }

    @Test
    public void testWith_multipleTranslators_returnsAggregateTranslator() {
        CharSequenceTranslator addA = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('A');
                return 1;
            }
        };
        CharSequenceTranslator addB = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('B');
                return 1;
            }
        };
        CharSequenceTranslator combined = addA.with(addB);
        assertEquals("A", combined.translate("X"));
    }

    @Test
    public void testWith_firstTranslatorReturnsZero_secondUsed() {
        CharSequenceTranslator zero = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator addC = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('C');
                return 1;
            }
        };
        CharSequenceTranslator combined = zero.with(addC);
        assertEquals("C", combined.translate("Y"));
    }

    @Test
    public void testTranslate_posLtLenMinus2_trueBranch() {
        String input = "XYZ";
        String expected = "[58][59][5A]";
        assertEquals(expected, HEX_WRITER.translate(input));
    }

    @Test
    public void testTranslate_posLtLenMinus2_falseBranch() {
        String input = "XY";
        String expected = "[58][59]";
        assertEquals(expected, HEX_WRITER.translate(input));
    }

    @Test
    public void testTranslate_consumedTwo_correctPosUpdate() {
        String input = "AB";
        assertEquals("", CONSUMED_TWO.translate(input));
    }
}