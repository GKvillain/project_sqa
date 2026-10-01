package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

public class TarUtilsTest {

    // Tests parseOctal: normal valid octal string
    @Test
    public void testParseOctal_validValue_returnsCorrectLong() {
        byte[] buffer = "000123 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(83L, result);
    }

    // Tests parseOctal: value with leading spaces
    @Test
    public void testParseOctal_leadingSpaces_returnsCorrectLong() {
        byte[] buffer = "  000123 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(83L, result);
    }

    // Tests parseOctal: buffer with all NULs returns 0
    @Test
    public void testParseOctal_allNuls_returnsZero() {
        byte[] buffer = new byte[12]; // all zeros
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parseOctal: buffer with leading NUL returns 0
    @Test
    public void testParseOctal_leadingNul_returnsZero() {
        byte[] buffer = "\0  123 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parseOctal: length less than 2 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = "12".getBytes();
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests parseOctal: invalid byte (not octal digit)
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidByte_throwsException() {
        byte[] buffer = "12x45 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parseOctal: trailing space/NUL missing
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_trailerMissing_throwsException() {
        byte[] buffer = "123".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parseOctalOrBinary: octal value calls parseOctal
    @Test
    public void testParseOctalOrBinary_octalValue_returnsCorrectLong() {
        byte[] buffer = "000123 \0".getBytes();
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(83L, result);
    }

    // Tests parseOctalOrBinary: negative binary value (length < 9)
    @Test
    public void testParseOctalOrBinary_negativeBinaryShort_returnsCorrectLong() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xff;
        // fill with 0xff for negative 2's complement of 1
        for (int i = 1; i < 8; i++) {
            buffer[i] = (byte) 0xff;
        }
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(-1L, result);
    }

    // Tests parseOctalOrBinary: positive binary value
    @Test
    public void testParseOctalOrBinary_positiveBinary_returnsCorrectLong() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0x80; // indicates binary mode, positive
        buffer[1] = 0; // high byte zero
        buffer[2] = 0;
        buffer[3] = 0;
        buffer[4] = 0;
        buffer[5] = 0;
        buffer[6] = 0;
        buffer[7] = 42; // value = 42
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(42L, result);
    }

    // Tests parseBoolean: byte value 1 returns true
    @Test
    public void testParseBoolean_byteOne_returnsTrue() {
        byte[] buffer = new byte[] {1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    // Tests parseBoolean: byte value 0 returns false
    @Test
    public void testParseBoolean_byteZero_returnsFalse() {
        byte[] buffer = new byte[] {0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    // Tests parseBoolean: byte value other than 0/1 returns false
    @Test
    public void testParseBoolean_byteTwo_returnsFalse() {
        byte[] buffer = new byte[] {2};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    // Tests parseName: with trailing NULs
    @Test
    public void testParseName_trailingNuls_returnsTrimmedString() {
        byte[] buffer = new byte[20];
        byte[] nameBytes = "test.txt".getBytes();
        System.arraycopy(nameBytes, 0, buffer, 0, nameBytes.length);
        // rest of buffer is already null (0)
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test.txt", result);
    }

    // Tests parseName: buffer with only NULs returns empty string
    @Test
    public void testParseName_allNuls_returnsEmptyString() {
        byte[] buffer = new byte[10];
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", result);
    }

    // Tests formatUnsignedOctalString: value 0
    @Test
    public void testFormatUnsignedOctalString_zeroValue_writesLeadingZeros() {
        byte[] buffer = new byte[12];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 12);
        assertEquals("000000000000", new String(buffer));
    }

    // Tests formatUnsignedOctalString: positive value
    @Test
    public void testFormatUnsignedOctalString_positiveValue_correctOctal() {
        byte[] buffer = new byte[12];
        TarUtils.formatUnsignedOctalString(83L, buffer, 0, 12);
        assertEquals("000000000123", new String(buffer));
    }

    // Tests formatUnsignedOctalString: value too large for buffer
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsException() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(1000L, buffer, 0, 3);
    }

    // Tests formatOctalBytes: normal value
    @Test
    public void testFormatOctalBytes_validValue_writesOctalWithTrailer() {
        byte[] buffer = new byte[12];
        TarUtils.formatOctalBytes(83L, buffer, 0, 12);
        byte[] expected = "000000000123\0 ".getBytes();
        assertArrayEquals(expected, buffer);
    }

    // Tests formatLongOctalBytes: normal value
    @Test
    public void testFormatLongOctalBytes_validValue_writesOctalWithSpace() {
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalBytes(83L, buffer, 0, 12);
        byte[] expected = "000000000123 ".getBytes();
        assertArrayEquals(expected, buffer);
    }

    // Tests formatCheckSumOctalBytes: normal value
    @Test
    public void testFormatCheckSumOctalBytes_validValue_writesOctalWithNulAndSpace() {
        byte[] buffer = new byte[12];
        TarUtils.formatCheckSumOctalBytes(83L, buffer, 0, 12);
        byte[] expected = "000000000123\0 ".getBytes();
        assertArrayEquals(expected, buffer);
    }

    // Tests computeCheckSum: all zero bytes
    @Test
    public void testComputeCheckSum_allZeros_returnsZero() {
        byte[] buf = new byte[10];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    // Tests computeCheckSum: non-zero bytes
    @Test
    public void testComputeCheckSum_someValues_returnsCorrectSum() {
        byte[] buf = new byte[] {1, 2, 3};
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    // Tests verifyCheckSum: header with matching checksum (simplified)
    @Test
    public void testVerifyCheckSum_matchingChecksum_returnsTrue() {
        byte[] header = new byte[512];
        // Set up a minimal header with correct checksum
        // The checksum is at offset 148, 8 bytes
        // We set it to octal '0000000\0 ' which is sum 0
        // But to have a passing check, the sum in the field must match
        // For simplicity, test with header that has a valid checksum
        // We'll compute a simple one: all zeros => stored 0, unsignedSum 0
        // Set checksum field to "0000000\0 " which is 0
        byte[] checksumBytes = "0000000\0 ".getBytes();
        System.arraycopy(checksumBytes, 0, header, 148, 8);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests formatLongOctalOrBinaryBytes: value fits in octal (positive small)
    @Test
    public void testFormatLongOctalOrBinaryBytes_fitsOctal_writesOctal() {
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(83L, buffer, 0, 12);
        byte[] expected = "000000000123 ".getBytes();
        assertArrayEquals(expected, buffer);
    }

    // Tests formatLongOctalOrBinaryBytes: value too large for octal, binary used (length < 9)
    @Test
    public void testFormatLongOctalOrBinaryBytes_largeValueBinary_writesBinary() {
        byte[] buffer = new byte[8];
        // value larger than max for octal but fits in 7 bytes
        TarUtils.formatLongOctalOrBinaryBytes(0x7fffffffffffffffL, buffer, 0, 8);
        // Check that first byte indicates binary (positive)
        assertEquals((byte) 0x80, buffer[0]);
    }

    // Tests formatLongOctalOrBinaryBytes: negative value
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeValue_writesNegativeBinary() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buffer, 0, 8);
        // Check first byte indicates negative binary
        assertEquals((byte) 0xff, buffer[0]);
        // The rest should be 0xff for -1
        for (int i = 1; i < 8; i++) {
            assertEquals((byte) 0xff, buffer[i]);
        }
    }
}