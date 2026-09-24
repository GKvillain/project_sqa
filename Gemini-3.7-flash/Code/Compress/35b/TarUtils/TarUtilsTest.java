package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    // Tests normal octal parsing with valid input and trailing space/NUL
    @Test
    public void testParseOctal_validOctalString_returnsParsedValue() {
        byte[] buffer = " 012345 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(012345L, result);
    }

    // Tests parsing octal when leading byte is NUL
    @Test
    public void testParseOctal_leadingNul_returnsZero() {
        byte[] buffer = new byte[] { 0, '1', '2', ' ' };
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parsing octal with buffer length less than 2
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = new byte[] { '7' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests parsing octal with invalid character
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidChar_throwsException() {
        byte[] buffer = " 012845 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing octal or binary when value is positive binary
    @Test
    public void testParseOctalOrBinary_positiveBinary_returnsValue() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 1 };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, result);
    }

    // Tests parsing octal or binary when value is negative binary
    @Test
    public void testParseOctalOrBinary_negativeBinary_returnsNegativeValue() {
        byte[] buffer = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, result);
    }

    // Tests parsing octal or binary exceeding 9 bytes with large BigInteger
    @Test
    public void testParseOctalOrBinary_largeBinary_returnsValue() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, result);
    }

    // Tests boolean parsing with 1 and 0
    @Test
    public void testParseBoolean_validBytes_returnsCorrectBoolean() {
        byte[] buffer = new byte[] { 1, 0, 2 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    // Tests entry name parsing and formatting roundtrip
    @Test
    public void testParseNameAndFormatNameBytes_validName_preservesName() {
        byte[] buffer = new byte[10];
        int offset = TarUtils.formatNameBytes("test", buffer, 0, buffer.length);
        assertEquals(10, offset);
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test", name);
    }

    // Tests name parsing when buffer contains only NULs
    @Test
    public void testParseName_allNuls_returnsEmptyString() {
        byte[] buffer = new byte[10];
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", name);
    }

    // Tests formatting octal bytes followed by space and NUL
    @Test
    public void testFormatOctalBytes_validValue_correctFormatting() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals("0000755 \0", new String(buffer));
    }

    // Tests formatting long octal bytes followed by space
    @Test
    public void testFormatLongOctalBytes_validValue_correctFormatting() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals("0000755 ", new String(buffer));
    }

    // Tests formatting long octal or binary for negative values
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeValue_writesBinary() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatLongOctalOrBinaryBytes(-1L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals((byte) 0xff, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-1L, parsed);
    }

    // Tests formatting long octal or binary when value exceeds octal max
    @Test
    public void testFormatLongOctalOrBinaryBytes_valueExceedingMax_writesBinary() {
        byte[] buffer = new byte[8];
        long bigValue = 0x1FFFFFFFFL;
        int resultOffset = TarUtils.formatLongOctalOrBinaryBytes(bigValue, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals((byte) 0x80, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(bigValue, parsed);
    }

    // Tests formatting checksum octal bytes followed by NUL and space
    @Test
    public void testFormatCheckSumOctalBytes_validValue_correctFormatting() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals('0', buffer[0]);
        assertEquals(0, buffer[6]);
        assertEquals((byte) ' ', buffer[7]);
    }

    // Tests compute checksum calculation
    @Test
    public void testComputeCheckSum_validBuffer_returnsSumOfBytes() {
        byte[] buffer = new byte[] { 1, 2, 3, (byte) 255 };
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(1 + 2 + 3 + 255, sum);
    }

    // Tests checksum verification with valid header
    @Test
    public void testVerifyCheckSum_validHeader_returnsTrue() {
        byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = 'A';
        }
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests checksum verification with corrupt header
    @Test
    public void testVerifyCheckSum_corruptHeader_returnsFalse() {
        byte[] header = new byte[512];
        TarUtils.formatCheckSumOctalBytes(12345L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        header[0] = 'X';
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}