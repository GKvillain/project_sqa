package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUM_OFFSET;
import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUMLEN;

import org.junit.Test;

public class TarUtilsTest {

    // Tests valid octal input
    @Test
    public void testParseOctal_validInput_returnsCorrectValue() {
        byte[] buffer = "00000123 ".getBytes();
        assertEquals(83, TarUtils.parseOctal(buffer, 0, 9));
    }

    // Tests leading NUL returns 0
    @Test
    public void testParseOctal_leadingNul_returnsZero() {
        byte[] buffer = new byte[10];
        buffer[0] = 0;
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    // Tests length less than 2 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        TarUtils.parseOctal(new byte[1], 0, 1);
    }

    // Tests invalid byte throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidByte_throwsException() {
        byte[] buffer = "12x".getBytes();
        TarUtils.parseOctal(buffer, 0, 3);
    }

    // Tests octal path of parseOctalOrBinary
    @Test
    public void testParseOctalOrBinary_octalPath_returnsCorrectValue() {
        byte[] buffer = "00000123 ".getBytes();
        assertEquals(83, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Tests binary positive value (length < 9)
    @Test
    public void testParseOctalOrBinary_binaryPositiveLengthLessThan9() {
        byte[] buffer = new byte[]{(byte)0x80, 0, 0, 0, 0, 0, 0, 1};
        assertEquals(1, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    // Tests binary negative value (length < 9)
    @Test
    public void testParseOctalOrBinary_binaryNegativeLengthLessThan9() {
        byte[] buffer = new byte[]{(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    // Tests binary positive value (length == 9)
    @Test
    public void testParseOctalOrBinary_binaryPositiveLength9() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte)0x80;
        buffer[8] = 1;
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Tests binary negative value (length == 9)
    @Test
    public void testParseOctalOrBinary_binaryNegativeLength9() {
        byte[] buffer = new byte[9];
        for (int i = 0; i < 9; i++) buffer[i] = (byte)0xff;
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Tests checksum matching signed sum (all zeros)
    @Test
    public void testVerifyCheckSum_matchesSignedSum_returnsTrue() {
        byte[] header = new byte[512];
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests checksum matching unsigned sum (value 256 decimal)
    @Test
    public void testVerifyCheckSum_matchesUnsignedSum_returnsTrue() {
        byte[] header = new byte[512];
        int off = CHKSUM_OFFSET;
        header[off] = '0';
        header[off + 1] = '0';
        header[off + 2] = '0';
        header[off + 3] = '4';
        header[off + 4] = '0';
        header[off + 5] = '0';
        header[off + 6] = 0;
        header[off + 7] = ' ';
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests checksum mismatch
    @Test
    public void testVerifyCheckSum_mismatch_returnsFalse() {
        byte[] header = new byte[512];
        int off = CHKSUM_OFFSET;
        header[off] = '0';
        header[off + 1] = '0';
        header[off + 2] = '0';
        header[off + 3] = '0';
        header[off + 4] = '0';
        header[off + 5] = '1';
        header[off + 6] = 0;
        header[off + 7] = ' ';
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    // Tests formatting zero value
    @Test
    public void testFormatUnsignedOctalString_valueZero_writesZeroes() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0, buf, 0, 4);
        assertArrayEquals(new byte[] {'0', '0', '0', '0'}, buf);
    }

    // Tests overflow when buffer too small
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_overflow_throwsException() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(64, buf, 0, 2);
    }

    // Tests octal formatting for small positive value
    @Test
    public void testFormatLongOctalOrBinaryBytes_smallPositive_octalFormat() {
        byte[] buf = new byte[12];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(0, buf, 0, 8);
        assertEquals(8, newOffset);
        byte[] expected = "0000000 ".getBytes();
        assertArrayEquals(expected, buf);
    }

    // Tests binary formatting for negative value (length 9)
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeLength9_binaryFormat() {
        byte[] buf = new byte[12];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(-1, buf, 0, 9);
        assertEquals(9, newOffset);
        byte[] expected = new byte[9];
        for (int i = 0; i < 9; i++) expected[i] = (byte)0xff;
        assertArrayEquals(expected, buf);
    }

    // Tests parseName with NUL terminator
    @Test
    public void testParseName_normalStringWithNul() {
        byte[] buffer = "Hello\0".getBytes();
        assertEquals("Hello", TarUtils.parseName(buffer, 0, 6));
    }

    // Tests parseName with all NULs returns empty string
    @Test
    public void testParseName_allNulls_returnsEmptyString() {
        byte[] buffer = new byte[10];
        assertEquals("", TarUtils.parseName(buffer, 0, 10));
    }

    // Tests formatNameBytes writes name padded with NUL
    @Test
    public void testFormatNameBytes_writesNamePaddedWithNul() {
        byte[] buf = new byte[10];
        int newOffset = TarUtils.formatNameBytes("test", buf, 0, 10);
        assertEquals(10, newOffset);
        byte[] expected = new byte[]{'t', 'e', 's', 't', 0, 0, 0, 0, 0, 0};
        assertArrayEquals(expected, buf);
    }

    // ===================== New test cases for uncovered coverage =====================

    // parseOctal
    @Test
    public void testParseOctal_spacesOnly_returnsZero() {
        byte[] buffer = "  ".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    @Test
    public void testParseOctal_nonZeroOffset_returnsCorrectValue() {
        byte[] buffer = "xxx0000123 ".getBytes();
        assertEquals(83, TarUtils.parseOctal(buffer, 3, 9));
    }

    // parseOctalOrBinary
    @Test
    public void testParseOctalOrBinary_binaryPositiveLength8() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte)0x80;
        buffer[7] = 127; // positive 127
        assertEquals(127, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    @Test
    public void testParseOctalOrBinary_binaryNegativeLength8() {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte)0xff;
        }
        buffer[7] = (byte)0xfe; // two's complement -2
        assertEquals(-2L, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    @Test
    public void testParseOctalOrBinary_octalWithNul() {
        byte[] buffer = "0000123\0".getBytes();
        assertEquals(83, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    // formatUnsignedOctalString
    @Test
    public void testFormatUnsignedOctalString_smallValue() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(1, buf, 0, 4);
        assertArrayEquals(new byte[] {'0', '0', '0', '1'}, buf);
    }

    @Test
    public void testFormatUnsignedOctalString_mediumValue() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(63, buf, 0, 2);
        assertArrayEquals(new byte[] {'7', '7'}, buf);
    }

    // formatLongOctalOrBinaryBytes
    @Test
    public void testFormatLongOctalOrBinaryBytes_smallPositiveOctal() {
        byte[] buf = new byte[12];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(1, buf, 0, 8);
        assertEquals(8, newOffset);
        byte[] expected = "0000001 ".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes_positiveValueBinaryLength8() {
        // value = 8^7 = 2097152 -> forces binary format for length 8
        byte[] buf = new byte[12];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(2097152L, buf, 0, 8);
        assertEquals(8, newOffset);
        // first byte must be 0x80 (positive binary marker)
        assertEquals((byte)0x80, buf[0]);
        // remainder (big-endian): 2097152 = 0x00200000 -> last two bytes = 0x20, 0x00
        assertEquals((byte)0x20, buf[6]);
        assertEquals((byte)0x00, buf[7]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeValueLength8() {
        byte[] buf = new byte[12];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 8);
        assertEquals(8, newOffset);
        byte[] expected = new byte[8];
        for (int i = 0; i < 8; i++) expected[i] = (byte)0xff;
        assertArrayEquals(expected, buf);
    }

    // parseName
    @Test
    public void testParseName_withoutNul_returnsFullString() {
        byte[] buffer = "Hello".getBytes();
        assertEquals("Hello", TarUtils.parseName(buffer, 0, 5));
    }

    @Test
    public void testParseName_nulInMiddle() {
        byte[] buffer = "Hel\0lo".getBytes();
        assertEquals("Hel", TarUtils.parseName(buffer, 0, 6));
    }

    // formatNameBytes
    @Test
    public void testFormatNameBytes_nameExactLength() {
        byte[] buf = new byte[4];
        int newOffset = TarUtils.formatNameBytes("test", buf, 0, 4);
        assertEquals(4, newOffset);
        // name fills exactly, no NUL padding
        assertArrayEquals(new byte[] {'t', 'e', 's', 't'}, buf);
    }

    @Test
    public void testFormatNameBytes_nameEmpty() {
        byte[] buf = new byte[5];
        int newOffset = TarUtils.formatNameBytes("", buf, 0, 5);
        assertEquals(5, newOffset);
        assertArrayEquals(new byte[5], buf); // all zeros (NUL)
    }
}