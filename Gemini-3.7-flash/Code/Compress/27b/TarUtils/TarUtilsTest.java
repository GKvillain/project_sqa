package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    // Tests parsing standard octal string with trailing space and NUL
    @Test
    public void testParseOctal_standardOctal_returnsCorrectValue() {
        byte[] buffer = "0000755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, val);
    }

    // Tests parsing octal with leading spaces
    @Test
    public void testParseOctal_leadingSpaces_returnsCorrectValue() {
        byte[] buffer = "   755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, val);
    }

    // Tests parsing all NUL buffer returns 0L
    @Test
    public void testParseOctal_allNulBuffer_returnsZero() {
        byte[] buffer = new byte[8];
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    // Tests parsing octal when length is less than 2 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsIllegalArgumentException() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests parsing octal with invalid character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidOctalChar_throwsIllegalArgumentException() {
        byte[] buffer = "0000855 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing octal with missing trailing space or NUL
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_missingTrailingSpaceOrNul_throwsIllegalArgumentException() {
        byte[] buffer = "12345678".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing octal with max 7-byte octal value (Defects4J 27b bug target)
    @Test
    public void testParseOctal_maxOctalValidBuffer_returnsCorrectValue() {
        byte[] buffer = "7777777 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(07777777L, val);
    }

    // Tests parseOctalOrBinary with standard octal
    @Test
    public void testParseOctalOrBinary_octalValue_returnsParsedValue() {
        byte[] buffer = "0000755 \0".getBytes();
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(0755L, val);
    }

    // Tests parseOctalOrBinary with binary positive format (length < 9)
    @Test
    public void testParseOctalOrBinary_binaryPositiveSmall_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 0, 1 };
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, val);
    }

    // Tests parseOctalOrBinary with binary negative format (length < 9)
    @Test
    public void testParseOctalOrBinary_binaryNegativeSmall_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe };
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, val);
    }

    // Tests parseOctalOrBinary with binary positive format (length >= 9)
    @Test
    public void testParseOctalOrBinary_binaryPositiveBigInteger_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x10 };
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(16L, val);
    }

    // Tests parseBoolean
    @Test
    public void testParseBoolean_validInput_returnsExpectedBoolean() {
        byte[] buffer = new byte[] { 1, 0 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
    }

    // Tests parseName and formatNameBytes round-trip
    @Test
    public void testParseNameAndFormatNameBytes_validString_roundTripsCorrectly() {
        byte[] buffer = new byte[20];
        int offset = TarUtils.formatNameBytes("testname", buffer, 0, 15);
        assertEquals(15, offset);
        String parsed = TarUtils.parseName(buffer, 0, 15);
        assertEquals("testname", parsed);
    }

    // Tests parseName when entry is all NULs
    @Test
    public void testParseName_allNulBuffer_returnsEmptyString() {
        byte[] buffer = new byte[10];
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", parsed);
    }

    // Tests formatUnsignedOctalString and formatOctalBytes
    @Test
    public void testFormatOctalBytes_validValue_writesExpectedBytes() {
        byte[] buffer = new byte[8];
        int offset = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, offset);
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, val);
    }

    // Tests formatUnsignedOctalString when value is too large throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsIllegalArgumentException() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(0777777L, buffer, 0, 3);
    }

    // Tests formatLongOctalBytes
    @Test
    public void testFormatLongOctalBytes_validValue_writesExpectedBytes() {
        byte[] buffer = new byte[8];
        int offset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, offset);
        assertEquals((byte) ' ', buffer[7]);
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, val);
    }

    // Tests formatLongOctalOrBinaryBytes with octal and binary values
    @Test
    public void testFormatLongOctalOrBinaryBytes_values_formatsCorrectly() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0755L, buffer, 0, buffer.length);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(0755L, val);

        byte[] binBuffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0x1ffffffffL, binBuffer, 0, binBuffer.length);
        long binVal = TarUtils.parseOctalOrBinary(binBuffer, 0, binBuffer.length);
        assertEquals(0x1ffffffffL, binVal);
    }

    // Tests formatCheckSumOctalBytes and computeCheckSum
    @Test
    public void testFormatCheckSumOctalBytesAndComputeCheckSum_validChecksum_succeeds() {
        byte[] buffer = new byte[8];
        TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(0, buffer[6]);
        assertEquals((byte) ' ', buffer[7]);
        long sum = TarUtils.computeCheckSum(buffer);
        assertTrue(sum > 0);
    }

    // Tests verifyCheckSum heuristic check
    @Test
    public void testVerifyCheckSum_validHeader_returnsTrue() {
        byte[] header = new byte[512];
        TarUtils.formatCheckSumOctalBytes(0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        long cs = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(cs, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue(TarUtils.verifyCheckSum(header));
    }
}