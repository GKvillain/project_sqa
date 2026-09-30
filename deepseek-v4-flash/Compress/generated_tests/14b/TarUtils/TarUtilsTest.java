package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;

public class TarUtilsTest {

    // parseOctal: valid octal string with trailing space
    @Test
    public void testParseOctal_validInput_returnsCorrectValue() {
        byte[] buffer = "0000123 ".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, 8);
        assertEquals(83L, result);
    }

    // parseOctal: length < 2 should throw
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // parseOctal: all NUL bytes returns 0
    @Test
    public void testParseOctal_allNULs_returnsZero() {
        byte[] buffer = new byte[10];
        long result = TarUtils.parseOctal(buffer, 0, 10);
        assertEquals(0L, result);
    }

    // parseOctal: leading NUL followed by valid octal (workaround not implemented in buggy version)
    @Test
    public void testParseOctal_leadingNUL_returnsZero() {
        byte[] buffer = new byte[] {0, '1', ' '};
        long result = TarUtils.parseOctal(buffer, 0, 3);
        assertEquals(0L, result);
    }

    // parseOctal: invalid character (not octal) throws
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidCharacter_throwsException() {
        byte[] buffer = "128 ".getBytes();
        TarUtils.parseOctal(buffer, 0, 4);
    }

    // parseOctal: missing trailing space or NUL throws
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_missingTrailer_throwsException() {
        byte[] buffer = "123".getBytes();
        TarUtils.parseOctal(buffer, 0, 3);
    }

    // parseOctalOrBinary: MSB not set -> octal path
    @Test
    public void testParseOctalOrBinary_octalPath_returnsParsedOctal() {
        byte[] buffer = "0000123 ".getBytes();
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(83L, result);
    }

    // parseOctalOrBinary: MSB set -> binary path
    @Test
    public void testParseOctalOrBinary_binaryPath_returnsBinaryValue() {
        byte[] buffer = new byte[] {(byte)0x81, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(257L, result);
    }

    // parseOctalOrBinary: binary number that would overflow signed long throws
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinary_binaryOverflow_throwsException() {
        byte[] buffer = new byte[9];
        for (int i = 0; i < 9; i++) {
            buffer[i] = (byte) 0xFF;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, 9);
    }

    // formatUnsignedOctalString: zero value
    @Test
    public void testFormatUnsignedOctalString_zero_returnsAllZeros() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertArrayEquals(new byte[] {48, 48, 48, 48}, buffer);
    }

    // formatUnsignedOctalString: positive value
    @Test
    public void testFormatUnsignedOctalString_positiveValue_correctFormat() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(83L, buffer, 0, 6);
        assertArrayEquals(new byte[] {48,48,48,49,50,51}, buffer);
    }

    // formatUnsignedOctalString: value too large for buffer
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsException() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(512L, buffer, 0, 3);
    }

    // computeCheckSum: simple buffer
    @Test
    public void testComputeCheckSum_simpleBuffer_returnsCorrectSum() {
        byte[] buffer = new byte[] {1, 2, 3};
        long result = TarUtils.computeCheckSum(buffer);
        assertEquals(6L, result);
    }

    // parseBoolean: true and false values
    @Test
    public void testParseBoolean_trueAndFalse_returnsCorrect() {
        assertTrue(TarUtils.parseBoolean(new byte[] {1}, 0));
        assertFalse(TarUtils.parseBoolean(new byte[] {0}, 0));
        assertFalse(TarUtils.parseBoolean(new byte[] {2}, 0));
    }

    // parseName: null terminator stops parsing
    @Test
    public void testParseName_withNullTerminator_returnsStringBeforeNull() {
        byte[] buffer = "hello\0world".getBytes();
        String result = TarUtils.parseName(buffer, 0, 11);
        assertEquals("hello", result);
    }

    // parseName: full buffer without null
    @Test
    public void testParseName_fullBufferNoNull_returnsFullString() {
        byte[] buffer = "hello".getBytes();
        String result = TarUtils.parseName(buffer, 0, 5);
        assertEquals("hello", result);
    }

    // formatNameBytes: name shorter than length pads with NUL
    @Test
    public void testFormatNameBytes_shorterThanLength_padsWithNUL() {
        byte[] buffer = new byte[10];
        int newOffset = TarUtils.formatNameBytes("abc", buffer, 0, 10);
        assertEquals(10, newOffset);
        byte[] expected = new byte[10];
        expected[0] = 'a'; expected[1] = 'b'; expected[2] = 'c';
        assertArrayEquals(expected, buffer);
    }

    // formatNameBytes: name longer than length truncates
    @Test
    public void testFormatNameBytes_nameLongerThanLength_truncates() {
        byte[] buffer = new byte[3];
        TarUtils.formatNameBytes("abcdef", buffer, 0, 3);
        assertArrayEquals(new byte[] {'a','b','c'}, buffer);
    }

    // formatLongOctalOrBinaryBytes: value small enough for octal representation
    @Test
    public void testFormatLongOctalOrBinaryBytes_octalPath_returnsFormatted() {
        int length = 8;
        byte[] buffer = new byte[length];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(100L, buffer, 0, length);
        assertEquals(length, newOffset);
        assertTrue((buffer[0] & 0x80) == 0);
        assertEquals(' ', buffer[length - 1]);
    }

    // formatLongOctalOrBinaryBytes: value large => binary representation
    @Test
    public void testFormatLongOctalOrBinaryBytes_binaryPath_returnsFormatted() {
        int length = 8;
        byte[] buffer = new byte[length];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buffer, 0, length);
        assertEquals(length, newOffset);
        assertTrue((buffer[0] & 0x80) != 0);
    }

    // formatCheckSumOctalBytes: returns offset+length and places NUL and space
    @Test
    public void testFormatCheckSumOctalBytes_returnsOffset() {
        byte[] buffer = new byte[10];
        int result = TarUtils.formatCheckSumOctalBytes(123L, buffer, 0, 10);
        assertEquals(10, result);
        assertEquals(0, buffer[8]);
        assertEquals(' ', buffer[9]);
    }
}