package org.apache.commons.compress.archivers.tar;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TarUtilsTest {

    // Tests parsing standard octal string with trailing NUL
    @Test
    public void testParseOctal_standardInput_returnsValue() {
        byte[] buffer = "000755\0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, result);
    }

    // Tests parsing octal string with leading spaces and trailing space
    @Test
    public void testParseOctal_leadingAndTrailingSpaces_returnsValue() {
        byte[] buffer = "   123 ".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, result);
    }

    // Tests parsing buffer starting with NUL returns 0
    @Test
    public void testParseOctal_leadingNull_returnsZero() {
        byte[] buffer = new byte[8];
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parseOctal with length less than 2 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = new byte[]{ '0' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests parseOctal without trailing space or NUL throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_missingTrailingSpaceOrNull_throwsException() {
        byte[] buffer = "12345678".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parseOctal with invalid octal character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidOctalDigit_throwsException() {
        byte[] buffer = "000855\0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing boolean value
    @Test
    public void testParseBoolean_validInput_returnsCorrectBoolean() {
        byte[] buffer = new byte[]{ 1, 0, 2 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    // Tests parseName with trailing NULs
    @Test
    public void testParseName_withTrailingNulls_returnsName() {
        byte[] buffer = "testfile.txt\0\0\0\0".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("testfile.txt", name);
    }

    // Tests parseName on empty / all NUL buffer
    @Test
    public void testParseName_allNulls_returnsEmptyString() {
        byte[] buffer = new byte[10];
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", name);
    }

    // Tests formatNameBytes with short name padded with NUL
    @Test
    public void testFormatNameBytes_shortName_padsWithNulls() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatNameBytes("abc", buffer, 0, 10);
        assertEquals(10, nextOffset);
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals('c', buffer[2]);
        assertEquals(0, buffer[3]);
        assertEquals(0, buffer[9]);
    }

    // Tests formatNameBytes with truncation when name exceeds length
    @Test
    public void testFormatNameBytes_longName_truncates() {
        byte[] buffer = new byte[4];
        int nextOffset = TarUtils.formatNameBytes("abcdef", buffer, 0, 4);
        assertEquals(4, nextOffset);
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals('c', buffer[2]);
        assertEquals('d', buffer[3]);
    }

    // Tests formatUnsignedOctalString with zero and non-zero values
    @Test
    public void testFormatUnsignedOctalString_validValues_formatsCorrectly() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 6);
        assertEquals("000000", new String(buffer));

        TarUtils.formatUnsignedOctalString(0755L, buffer, 0, 6);
        assertEquals("000755", new String(buffer));
    }

    // Tests formatUnsignedOctalString throws IllegalArgumentException when value does not fit
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsException() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(01000L, buffer, 0, 2);
    }

    // Tests formatOctalBytes writes octal number followed by space and NUL
    @Test
    public void testFormatOctalBytes_validInput_formatsWithSpaceAndNull() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals('0', buffer[0]);
        assertEquals('0', buffer[1]);
        assertEquals('0', buffer[2]);
        assertEquals('7', buffer[3]);
        assertEquals('5', buffer[4]);
        assertEquals('5', buffer[5]);
        assertEquals(' ', buffer[6]);
        assertEquals(0, buffer[7]);
    }

    // Tests formatLongOctalBytes writes octal number followed by space
    @Test
    public void testFormatLongOctalBytes_validInput_formatsWithTrailingSpace() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals('0', buffer[0]);
        assertEquals('0', buffer[1]);
        assertEquals('0', buffer[2]);
        assertEquals('0', buffer[3]);
        assertEquals('7', buffer[4]);
        assertEquals('5', buffer[5]);
        assertEquals('5', buffer[6]);
        assertEquals(' ', buffer[7]);
    }

    // Tests formatCheckSumOctalBytes writes octal number followed by NUL and space
    @Test
    public void testFormatCheckSumOctalBytes_validInput_formatsWithNullAndSpace() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals('0', buffer[0]);
        assertEquals('0', buffer[1]);
        assertEquals('0', buffer[2]);
        assertEquals('1', buffer[3]);
        assertEquals('2', buffer[4]);
        assertEquals('3', buffer[5]);
        assertEquals(0, buffer[6]);
        assertEquals(' ', buffer[7]);
    }

    // Tests round trip for binary format using formatLongOctalOrBinaryBytes and parseOctalOrBinary
    @Test
    public void testFormatAndParseLongOctalOrBinary_positiveBinary_roundTrips() {
        long value = TarConstants.MAXSIZE + 1000L;
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 12);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        assertEquals(value, parsed);
    }

    // Tests negative binary format round trip
    @Test
    public void testFormatAndParseLongOctalOrBinary_negativeBinary_roundTrips() {
        long value = -12345L;
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 8);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(value, parsed);
    }

    // Tests computeCheckSum sums byte values as unsigned bytes
    @Test
    public void testComputeCheckSum_validBuffer_returnsUnsignedSum() {
        byte[] buffer = new byte[]{ 1, 2, 3, (byte) 255 };
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(1 + 2 + 3 + 255, sum);
    }

    // Tests verifyCheckSum returns true for correctly formatted header checksum
    @Test
    public void testVerifyCheckSum_validHeader_returnsTrue() {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 'a');

        // Fill checksum field with spaces to compute expected sum
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests verifyCheckSum returns false for corrupted header checksum
    @Test
    public void testVerifyCheckSum_corruptedHeader_returnsFalse() {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 0);
        header[0] = 1; // modifies content so storedSum of 0 won't match (unsignedSum = 1 + 8*' ' = 257)
        TarUtils.formatCheckSumOctalBytes(0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}