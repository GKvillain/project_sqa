package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

public class TarUtilsTest {

    // Test parseOctal with valid octal string
    @Test
    public void testParseOctal_validInput_returnsCorrectValue() {
        byte[] buffer = "000755 ".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, result); // 0755 octal = 493 decimal
    }

    // Test parseOctal with leading NUL -> returns 0
    @Test
    public void testParseOctal_leadingNull_returnsZero() {
        byte[] buffer = new byte[] {0, '1', '2', '3', '4', '5', ' ', 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    // Test parseOctal with invalid byte -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidCharacter_throwsException() {
        byte[] buffer = "12a34 ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Test parseOctal length less than 2 -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThan2_throwsException() {
        TarUtils.parseOctal(new byte[2], 0, 1);
    }

    // Test parseOctalOrBinary when MSB not set (octal path)
    @Test
    public void testParseOctalOrBinary_octalPath_returnsParsedOctal() {
        byte[] buffer = "000755 ".getBytes();
        assertEquals(493L, TarUtils.parseOctalOrBinary(buffer, 0, buffer.length));
    }

    // Test parseOctalOrBinary binary positive, length < 9
    @Test
    public void testParseOctalOrBinary_binaryPositiveLessThan9_returnsValue() {
        byte[] buffer = new byte[] {(byte)0x80, 0x12, 0x34, 0x56};
        assertEquals(0x123456L, TarUtils.parseOctalOrBinary(buffer, 0, 4));
    }

    // Test parseOctalOrBinary binary positive, length >= 9
    @Test
    public void testParseOctalOrBinary_binaryPositiveLength9_returnsValue() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte)0x80;
        buffer[1] = 0x01; // positive value 1
        // rest bytes already 0
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Test parseOctalOrBinary binary negative, length < 9
    @Test
    public void testParseOctalOrBinary_binaryNegativeLessThan9_returnsValue() {
        byte[] buffer = new byte[] {(byte)0xff, (byte)0xff, (byte)0xff};
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 3));
    }

    // Test parseOctalOrBinary binary negative, length >= 9
    @Test
    public void testParseOctalOrBinary_binaryNegativeLength9_returnsValue() {
        byte[] buffer = new byte[9];
        for (int i = 0; i < 9; i++) {
            buffer[i] = (byte)0xff;
        }
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Test parseBoolean with 1 -> true
    @Test
    public void testParseBoolean_one_returnsTrue() {
        byte[] buffer = new byte[] {1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    // Test parseBoolean with 0 -> false
    @Test
    public void testParseBoolean_zero_returnsFalse() {
        byte[] buffer = new byte[] {0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    // Test parseName with trailing NULs
    @Test
    public void testParseName_withTrailingNulls_returnsName() {
        byte[] buffer = new byte[] {'H', 'e', 'l', 'l', 'o', 0, 0, 0};
        assertEquals("Hello", TarUtils.parseName(buffer, 0, buffer.length));
    }

    // Test formatUnsignedOctalString zero
    @Test
    public void testFormatUnsignedOctalString_zero_fillsZeros() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0, buffer, 0, 6);
        assertArrayEquals(new byte[] {'0','0','0','0','0','0'}, buffer);
    }

    // Test formatUnsignedOctalString overflow -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_overflow_throwsException() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(512, buffer, 0, 3); // 512 octal = 1000 exceeds 3 digits
    }

    // Test formatOctalBytes typical use
    @Test
    public void testFormatOctalBytes_validInput_returnsOffset() {
        byte[] buffer = new byte[8];
        int result = TarUtils.formatOctalBytes(65L, buffer, 0, 8);
        // 65 octal = 101, formatted with leading zeros: "0000101 " + NUL
        assertEquals(8, result);  // offset + length
        assertArrayEquals("0000101 \0".getBytes(), buffer);
    }

    // Test computeCheckSum
    @Test
    public void testComputeCheckSum_variousBytes_returnsCorrectSum() {
        byte[] buf = new byte[] {1, 2, 3};
        // unsigned sum = 1+2+3 = 6
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    // Test verifyCheckSum where stored sum matches unsigned sum
    @Test
    public void testVerifyCheckSum_unsignedMatch_returnsTrue() {
        // create a 512-byte header full of NULs except checksum fields set to octal 192
        byte[] header = new byte[512];
        long storedSum = 192L;
        String octal = String.format("%06o", storedSum); // "000300"
        byte[] octalBytes = octal.getBytes();
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = octalBytes[i];
        }
        // remaining checksum bytes (2 bytes) left as NUL, but verifyCheckSum will replace them with ' ' for unsignedSum calc
        // all other bytes are 0, unsignedSum = 6*32 = 192 => match
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Test verifyCheckSum where no match
    @Test
    public void testVerifyCheckSum_noMatch_returnsFalse() {
        byte[] header = new byte[512];
        // fill with spaces (32)
        for (int i = 0; i < 512; i++) {
            header[i] = ' ';
        }
        // set checksum digits to zeros (storedSum = 0)
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = '0';
        }
        // unsignedSum = 512*32 = 16384, storedSum = 0 -> no match
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    // Test formatLongOctalOrBinaryBytes with negative value and length >= 9 -> binary
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeBinaryLength9_usesBinary() {
        byte[] buf = new byte[9];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 9);
        byte[] expected = new byte[9];
        for (int i = 0; i < 9; i++) {
            expected[i] = (byte)0xff;
        }
        assertArrayEquals(expected, buf);
    }

    // ========== New test cases to improve coverage ==========

    // Test parseOctal with buffer containing space at end
    @Test
    public void testParseOctal_withSpaceAtEnd_returnsCorrectValue() {
        byte[] buffer = "12345 ".getBytes();
        assertEquals(5349L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    // Test parseOctal with buffer containing NULs before octal digits
    @Test
    public void testParseOctal_withLeadingNullsAndSpace_returnsCorrectValue() {
        byte[] buffer = new byte[] {0, 0, '1', '2', '3', ' ', 0};
        assertEquals(83L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    // Test parseOctalOrBinary with MSB not set and length < 2 (edge case)
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinary_octalPathLengthTooShort_throwsException() {
        byte[] buffer = new byte[] {'1'};
        TarUtils.parseOctalOrBinary(buffer, 0, 1);
    }

    // Test parseOctalOrBinary with binary positive, length exactly 9, MSB set
    @Test
    public void testParseOctalOrBinary_binaryPositiveLength9_returnsCorrectLargeValue() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte)0x80;
        buffer[1] = (byte)0x12;
        buffer[2] = (byte)0x34;
        buffer[3] = (byte)0x56;
        buffer[4] = (byte)0x78;
        buffer[5] = (byte)0x9a;
        buffer[6] = (byte)0xbc;
        buffer[7] = (byte)0xde;
        buffer[8] = (byte)0xf0;
        long expected = 0x123456789abcdef0L;
        assertEquals(expected, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Test parseOctalOrBinary with binary negative, length exactly 9, all bits set
    @Test
    public void testParseOctalOrBinary_binaryNegativeLength9_returnsNegativeOne() {
        byte[] buffer = new byte[9];
        for (int i = 0; i < 9; i++) {
            buffer[i] = (byte)0xff;
        }
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    // Test parseName with no NUL terminator (entire buffer is name)
    @Test
    public void testParseName_noNullTerminator_returnsName() {
        byte[] buffer = new byte[] {'H', 'e', 'l', 'l', 'o'};
        assertEquals("Hello", TarUtils.parseName(buffer, 0, buffer.length));
    }

    // Test parseName with empty buffer
    @Test
    public void testParseName_emptyBuffer_returnsEmptyString() {
        byte[] buffer = new byte[0];
        assertEquals("", TarUtils.parseName(buffer, 0, 0));
    }

    // Test formatUnsignedOctalString with small positive value
    @Test
    public void testFormatUnsignedOctalString_smallValue_fillsCorrectly() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(7, buffer, 0, 4);
        assertArrayEquals(new byte[] {'0', '0', '0', '7'}, buffer);
    }

    // Test formatUnsignedOctalString with value that exactly fills length
    @Test
    public void testFormatUnsignedOctalString_exactFit_fillsCorrectly() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(63, buffer, 0, 3); // 63 octal = 77
        assertArrayEquals(new byte[] {'0', '7', '7'}, buffer);
    }

    // Test formatOctalBytes with zero value
    @Test
    public void testFormatOctalBytes_zero_fillsZerosWithTrailingSpaceAndNull() {
        byte[] buffer = new byte[8];
        int result = TarUtils.formatOctalBytes(0L, buffer, 0, 8);
        assertEquals(8, result);
        assertArrayEquals(new byte[] {'0','0','0','0','0','0',' ','\0'}, buffer);
    }

    // Test formatLongOctalBytes (not covered yet) - basic positive value
    @Test
    public void testFormatLongOctalBytes_positiveValue_fillsCorrectly() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalBytes(65L, buf, 0, 8);
        // 65 octal = 101, formatted as "0000101" + NUL
        byte[] expected = "0000101\0".getBytes();
        assertArrayEquals(expected, buf);
    }

    // Test formatLongOctalBytes with zero value
    @Test
    public void testFormatLongOctalBytes_zero_fillsZerosWithNull() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalBytes(0L, buf, 0, 8);
        byte[] expected = "0000000\0".getBytes();
        assertArrayEquals(expected, buf);
    }

    // Test formatLongOctalOrBinaryBytes with positive value that fits in octal, length < 9
    @Test
    public void testFormatLongOctalOrBinaryBytes_positiveOctalLengthLessThan9_usesOctal() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(65L, buf, 0, 8);
        byte[] expected = "0000101\0".getBytes();
        assertArrayEquals(expected, buf);
    }

    // Test formatLongOctalOrBinaryBytes with positive value that does not fit octal, length < 9
    @Test
    public void testFormatLongOctalOrBinaryBytes_positiveTooLargeForOctal_usesBinary() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 8);
        // Should write binary with MSB set
        assertTrue((buf[0] & 0x80) != 0);
    }

    // Test formatLongOctalOrBinaryBytes with negative value and length < 9 -> binary
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeBinaryLengthLessThan9_usesBinary() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 4);
        byte[] expected = new byte[] {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
        assertArrayEquals(expected, buf);
    }

    // Test formatLongOctalOrBinaryBytes with zero value
    @Test
    public void testFormatLongOctalOrBinaryBytes_zeroValue_usesOctal() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0L, buf, 0, 8);
        byte[] expected = "0000000\0".getBytes();
        assertArrayEquals(expected, buf);
    }
}