package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    // Tests normal parsing of an octal string with trailing space and NUL
    @Test
    public void testParseOctal_validOctalString_returnsParsedLong() {
        byte[] buffer = " 0123456 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123456L, result);
    }

    // Tests all NUL input returns 0
    @Test
    public void testParseOctal_allNulBytes_returnsZero() {
        byte[] buffer = new byte[10];
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests length less than 2 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests missing trailing space or NUL throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_missingTrailingSpaceOrNul_throwsException() {
        byte[] buffer = "12345678".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests invalid octal character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidOctalDigit_throwsException() {
        byte[] buffer = "012845 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parseOctalOrBinary with standard octal input (MSB not set)
    @Test
    public void testParseOctalOrBinary_octalMode_returnsParsedValue() {
        byte[] buffer = "0000755 \0".getBytes();
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(0755L, result);
    }

    // Tests parseOctalOrBinary with binary format (MSB set)
    @Test
    public void testParseOctalOrBinary_binaryMode_returnsCorrectValue() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0x80;
        buffer[7] = 0x01;
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, result);
    }

    // Tests parseOctalOrBinary exceeding signed long capacity throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinary_exceedsMaxLong_throwsException() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0xFF;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i] = (byte) 0xFF;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    // Tests parseBoolean for true (1) and false (non-1)
    @Test
    public void testParseBoolean_validValues_returnsExpectedBoolean() {
        byte[] buffer = new byte[] { 1, 0, 2 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    // Tests parseName with trailing NUL
    @Test
    public void testParseName_withTrailingNul_returnsTruncatedName() {
        byte[] buffer = new byte[] { 't', 'e', 's', 't', 0, 'a', 'b', 'c' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test", name);
    }

    // Tests formatNameBytes when name length is less than buffer length (pads with NUL)
    @Test
    public void testFormatNameBytes_nameShorterThanLength_padsWithNul() {
        byte[] buffer = new byte[10];
        int offset = TarUtils.formatNameBytes("file", buffer, 0, 10);
        assertEquals(10, offset);
        assertEquals('f', (char) buffer[0]);
        assertEquals('i', (char) buffer[1]);
        assertEquals('l', (char) buffer[2]);
        assertEquals('e', (char) buffer[3]);
        assertEquals(0, buffer[4]);
    }

    // Tests formatUnsignedOctalString with zero value
    @Test
    public void testFormatUnsignedOctalString_zeroValue_fillsWithLeadingZeros() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 6);
        assertEquals("000000", new String(buffer));
    }

    // Tests formatUnsignedOctalString with value that does not fit throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueDoesNotFit_throwsException() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(07777L, buffer, 0, 2);
    }

    // Tests formatOctalBytes creates octal string with trailing space and NUL
    @Test
    public void testFormatOctalBytes_validValue_formatsWithSpaceAndNul() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals("0000123 \0", new String(buffer));
    }

    // Tests formatLongOctalBytes creates octal string with trailing space
    @Test
    public void testFormatLongOctalBytes_validValue_formatsWithTrailingSpace() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals("0000755 ", new String(buffer));
    }

    // Tests formatLongOctalOrBinaryBytes when value fits into octal representation
    @Test
    public void testFormatLongOctalOrBinaryBytes_fitsInOctal_formatsAsOctal() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatLongOctalOrBinaryBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals("0000755 ", new String(buffer));
    }

    // Tests formatLongOctalOrBinaryBytes when value is too large for octal, formats as binary
    @Test
    public void testFormatLongOctalOrBinaryBytes_tooLargeForOctal_formatsAsBinary() {
        byte[] buffer = new byte[8];
        long largeValue = TarConstants.MAXID + 10L;
        int resultOffset = TarUtils.formatLongOctalOrBinaryBytes(largeValue, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals(largeValue, TarUtils.parseOctalOrBinary(buffer, 0, buffer.length));
    }

    // Tests formatLongOctalOrBinaryBytes value too large for field length throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytes_exceedsFieldLength_throwsException() {
        byte[] buffer = new byte[2];
        TarUtils.formatLongOctalOrBinaryBytes(0xFFFFFFL, buffer, 0, 2);
    }

    // Tests formatCheckSumOctalBytes creates octal string with trailing NUL and space
    @Test
    public void testFormatCheckSumOctalBytes_validValue_formatsWithNulAndSpace() {
        byte[] buffer = new byte[8];
        int resultOffset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(8, resultOffset);
        assertEquals("0000123\0 ", new String(buffer));
    }

    // Tests computeCheckSum sums all unsigned byte values in buffer
    @Test
    public void testComputeCheckSum_validBuffer_returnsCorrectSum() {
        byte[] buffer = new byte[] { (byte) 0xFF, 1, 2 };
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(255 + 1 + 2, sum);
    }
}