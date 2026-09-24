package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    // Tests parsing standard octal string with trailing space and NUL
    @Test
    public void testParseOctal_validOctalWithTrailingSpaceAndNul_returnsValue() {
        byte[] buffer = " 0755 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, result);
    }

    // Tests parsing buffer containing all NUL bytes returns 0L
    @Test
    public void testParseOctal_allNulBuffer_returnsZero() {
        byte[] buffer = new byte[8];
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parsing octal string with leading spaces and leading zeros
    @Test
    public void testParseOctal_leadingSpacesAndZeros_returnsValue() {
        byte[] buffer = "   000123 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(83L, result);
    }

    // Tests parsing octal string containing invalid non-octal digit throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidOctalDigit_throwsIllegalArgumentException() {
        byte[] buffer = " 0789 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing octal string missing trailing NUL or space throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_missingTrailingNulOrSpace_throwsIllegalArgumentException() {
        byte[] buffer = "1234".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing octal buffer with length less than 2 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsIllegalArgumentException() {
        byte[] buffer = "0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing buffer with all spaces throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_allSpaces_throwsIllegalArgumentException() {
        byte[] buffer = "   ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing octal string with embedded space throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_embeddedSpace_throwsIllegalArgumentException() {
        byte[] buffer = " 12 3 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parsing entry name terminated by NUL
    @Test
    public void testParseName_validNameTerminatedByNul_returnsString() {
        byte[] buffer = "test-file.txt\0extra-bytes".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test-file.txt", name);
    }

    // Tests parsing entry name occupying full buffer without trailing NUL
    @Test
    public void testParseName_fullBufferNoNul_returnsString() {
        byte[] buffer = "test".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test", name);
    }

    // Tests parsing entry name starting with NUL returns empty string
    @Test
    public void testParseName_emptyBufferWithLeadingNul_returnsEmptyString() {
        byte[] buffer = new byte[]{0, 'a', 'b'};
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", name);
    }

    // Tests formatting short name into buffer pads remaining space with NUL
    @Test
    public void testFormatNameBytes_shortName_padsWithNul() {
        byte[] buffer = new byte[8];
        int newOffset = TarUtils.formatNameBytes("abc", buffer, 0, 8);
        assertEquals(8, newOffset);
        assertEquals((byte) 'a', buffer[0]);
        assertEquals((byte) 'b', buffer[1]);
        assertEquals((byte) 'c', buffer[2]);
        assertEquals(0, buffer[3]);
        assertEquals(0, buffer[7]);
    }

    // Tests formatting name longer than buffer truncates the string
    @Test
    public void testFormatNameBytes_nameLongerThanBuffer_truncates() {
        byte[] buffer = new byte[3];
        int newOffset = TarUtils.formatNameBytes("abcdef", buffer, 0, 3);
        assertEquals(3, newOffset);
        assertEquals((byte) 'a', buffer[0]);
        assertEquals((byte) 'b', buffer[1]);
        assertEquals((byte) 'c', buffer[2]);
    }

    // Tests formatting zero unsigned octal string fills leading zeros
    @Test
    public void testFormatUnsignedOctalString_zeroValue_fillsWithLeadingZeros() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertArrayEquals(new byte[]{'0', '0', '0', '0'}, buffer);
    }

    // Tests formatting positive value to unsigned octal string
    @Test
    public void testFormatUnsignedOctalString_nonZeroValue_formatsCorrectly() {
        byte[] buffer = new byte[5];
        TarUtils.formatUnsignedOctalString(63L, buffer, 0, 5); // 63 decimal == 77 octal -> "00077"
        assertArrayEquals(new byte[]{'0', '0', '0', '7', '7'}, buffer);
    }

    // Tests formatting value too large for buffer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsIllegalArgumentException() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(512L, buffer, 0, 2); // 512 decimal == 1000 octal (needs 4 chars)
    }

    // Tests formatting octal bytes writes trailing space and NUL
    @Test
    public void testFormatOctalBytes_validValue_writesTrailingSpaceAndNul() {
        byte[] buffer = new byte[6];
        int newOffset = TarUtils.formatOctalBytes(8L, buffer, 0, 6); // 8 decimal == 10 octal -> "0010 \0"
        assertEquals(6, newOffset);
        assertEquals((byte) '0', buffer[0]);
        assertEquals((byte) '0', buffer[1]);
        assertEquals((byte) '1', buffer[2]);
        assertEquals((byte) '0', buffer[3]);
        assertEquals((byte) ' ', buffer[4]);
        assertEquals(0, buffer[5]);
    }

    // Tests formatting long octal bytes writes trailing space
    @Test
    public void testFormatLongOctalBytes_validValue_writesTrailingSpace() {
        byte[] buffer = new byte[5];
        int newOffset = TarUtils.formatLongOctalBytes(8L, buffer, 0, 5); // 8 decimal == 10 octal -> "0010 "
        assertEquals(5, newOffset);
        assertEquals((byte) '0', buffer[0]);
        assertEquals((byte) '0', buffer[1]);
        assertEquals((byte) '1', buffer[2]);
        assertEquals((byte) '0', buffer[3]);
        assertEquals((byte) ' ', buffer[4]);
    }

    // Tests formatting checksum octal bytes writes NUL and trailing space
    @Test
    public void testFormatCheckSumOctalBytes_validValue_writesNulAndTrailingSpace() {
        byte[] buffer = new byte[6];
        int newOffset = TarUtils.formatCheckSumOctalBytes(8L, buffer, 0, 6); // 8 decimal == 10 octal -> "0010\0 "
        assertEquals(6, newOffset);
        assertEquals((byte) '0', buffer[0]);
        assertEquals((byte) '0', buffer[1]);
        assertEquals((byte) '1', buffer[2]);
        assertEquals((byte) '0', buffer[3]);
        assertEquals(0, buffer[4]);
        assertEquals((byte) ' ', buffer[5]);
    }

    // Tests computing checksum of buffer handles byte masking
    @Test
    public void testComputeCheckSum_validBuffer_returnsSum() {
        byte[] buffer = new byte[]{(byte) 255, 1, 2};
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(258L, sum);
    }
}