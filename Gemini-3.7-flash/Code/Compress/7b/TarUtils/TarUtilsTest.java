package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    // Tests parsing octal with leading spaces and trailing NUL
    @Test
    public void testParseOctal_leadingSpacesAndTrailingNull_returnsParsedValue() {
        byte[] buffer = "  123\0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, result);
    }

    // Tests parsing octal with leading zeros and trailing space
    @Test
    public void testParseOctal_leadingZerosAndTrailingSpace_returnsParsedValue() {
        byte[] buffer = "000755 ".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, result);
    }

    // Tests parsing octal with zero value
    @Test
    public void testParseOctal_zeroValue_returnsZero() {
        byte[] buffer = "0\0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parsing octal with invalid character throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidOctalChar_throwsException() {
        byte[] buffer = "128\0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing name with trailing NUL
    @Test
    public void testParseName_nullTerminatedString_returnsParsedName() {
        byte[] buffer = new byte[] { 't', 'e', 's', 't', 0, 'e', 'x', 't', 'r', 'a' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test", name);
    }

    // Tests parsing name that fills the entire buffer length without NUL
    @Test
    public void testParseName_fullLengthNoNull_returnsCompleteName() {
        byte[] buffer = new byte[] { 'h', 'e', 'l', 'l', 'o' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello", name);
    }

    // Tests parsing empty name where first byte is NUL
    @Test
    public void testParseName_firstByteNull_returnsEmptyString() {
        byte[] buffer = new byte[] { 0, 'a', 'b', 'c' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", name);
    }

    // Tests formatNameBytes with shorter name padded with NUL
    @Test
    public void testFormatNameBytes_shortName_padsWithNulls() {
        byte[] buffer = new byte[8];
        int newOffset = TarUtils.formatNameBytes("file", buffer, 0, buffer.length);
        assertEquals(8, newOffset);
        assertEquals('f', (char) buffer[0]);
        assertEquals('i', (char) buffer[1]);
        assertEquals('l', (char) buffer[2]);
        assertEquals('e', (char) buffer[3]);
        assertEquals(0, buffer[4]);
        assertEquals(0, buffer[5]);
        assertEquals(0, buffer[6]);
        assertEquals(0, buffer[7]);
    }

    // Tests formatNameBytes with long name truncated
    @Test
    public void testFormatNameBytes_longName_truncatesToLength() {
        byte[] buffer = new byte[4];
        int newOffset = TarUtils.formatNameBytes("longfilename", buffer, 0, buffer.length);
        assertEquals(4, newOffset);
        assertEquals("long", new String(buffer));
    }

    // Tests formatUnsignedOctalString for zero value
    @Test
    public void testFormatUnsignedOctalString_zeroValue_fillsWithLeadingZeros() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertEquals("0000", new String(buffer));
    }

    // Tests formatUnsignedOctalString for positive value
    @Test
    public void testFormatUnsignedOctalString_positiveValue_formatsCorrectly() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0755L, buffer, 0, 6);
        assertEquals("000755", new String(buffer));
    }

    // Tests formatUnsignedOctalString when value does not fit in buffer
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsException() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(012345L, buffer, 0, 2);
    }

    // Tests formatOctalBytes creates octal with trailing space and NUL
    @Test
    public void testFormatOctalBytes_validValue_appendsSpaceAndNull() {
        byte[] buffer = new byte[8];
        int newOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, 8);
        assertEquals(8, newOffset);
        assertEquals("0000755 \0", new String(buffer));
    }

    // Tests formatLongOctalBytes creates octal with trailing space
    @Test
    public void testFormatLongOctalBytes_validValue_appendsTrailingSpace() {
        byte[] buffer = new byte[8];
        int newOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, 8);
        assertEquals(8, newOffset);
        assertEquals("0000755 ", new String(buffer));
    }

    // Tests formatCheckSumOctalBytes creates octal with trailing NUL and space
    @Test
    public void testFormatCheckSumOctalBytes_validValue_appendsNullAndSpace() {
        byte[] buffer = new byte[8];
        int newOffset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, 8);
        assertEquals(8, newOffset);
        assertEquals('0', (char) buffer[0]);
        assertEquals('0', (char) buffer[1]);
        assertEquals('0', (char) buffer[2]);
        assertEquals('1', (char) buffer[3]);
        assertEquals('2', (char) buffer[4]);
        assertEquals('3', (char) buffer[5]);
        assertEquals(0, buffer[6]);
        assertEquals(' ', (char) buffer[7]);
    }

    // Tests computeCheckSum calculates sum of unsigned byte values
    @Test
    public void testComputeCheckSum_validBuffer_returnsCorrectSum() {
        byte[] buffer = new byte[] { (byte) 255, 1, 2, 3 };
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(255L + 1L + 2L + 3L, sum);
    }
}