package org.apache.commons.compress.archivers.tar;

import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUMLEN;
import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUM_OFFSET;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TarUtilsTest {

    // Helper: round-trip test for formatLongOctalOrBinaryBytes + parseOctalOrBinary
    private void roundTripFormatAndParse(long value, int length) {
        byte[] buf = new byte[length];
        int written = TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, length);
        assertEquals(length, written);
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, length);
        assertEquals(value, parsed);
    }

    // ========== parseOctal ==========

    @Test
    public void testParseOctal_valid_returnsValue() {
        byte[] buf = "0000644\0".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctal_leadingZero_returnsZero() {
        byte[] buf = new byte[]{0, ' ', ' ', ' ', ' ', ' ', ' ', '\0'};
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctal_leadingSpaces_returnsValue() {
        byte[] buf = "     12\0".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(10L, result); // octal 12 -> decimal 10
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidChar_throwsException() {
        byte[] buf = "0000x00\0".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_tooShort_throwsException() {
        byte[] buf = new byte[]{'1'};
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    // ========== parseOctalOrBinary ==========

    @Test
    public void testParseOctalOrBinary_octal_returnsValue() {
        byte[] buf = "0000644\0".getBytes();
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalOrBinary_leadingNul_returnsZero() {
        byte[] buf = new byte[10];
        buf[0] = 0;
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctalOrBinary_binaryShortNegative_roundtrip() {
        roundTripFormatAndParse(-1L, 8);
        roundTripFormatAndParse(-256L, 8);
    }

    @Test
    public void testParseOctalOrBinary_binaryLongNegative_roundtrip() {
        roundTripFormatAndParse(-1L, 12);
        roundTripFormatAndParse(Long.MIN_VALUE, 12);
    }

    // ========== parseBoolean ==========

    @Test
    public void testParseBoolean_one_returnsTrue() {
        byte[] buf = {1};
        assertTrue(TarUtils.parseBoolean(buf, 0));
    }

    @Test
    public void testParseBoolean_zero_returnsFalse() {
        byte[] buf = {0};
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }

    // ========== parseName ==========

    @Test
    public void testParseName_normal_returnsString() {
        byte[] buf = "hello\0".getBytes();
        String result = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello", result);
    }

    @Test
    public void testParseName_empty_returnsEmpty() {
        byte[] buf = new byte[]{0, 0, 0};
        String result = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("", result);
    }

    // ========== formatUnsignedOctalString ==========

    @Test
    public void testFormatUnsignedOctalString_zero_fillsZero() {
        byte[] buf = new byte[10];
        TarUtils.formatUnsignedOctalString(0, buf, 0, buf.length);
        assertEquals("0000000000", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsException() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(64, buf, 0, buf.length);
    }

    // ========== formatOctalBytes ==========

    @Test
    public void testFormatOctalBytes_valid_returnsOffset() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatOctalBytes(0, buf, 0, buf.length);
        assertEquals(10, result);
        assertEquals(' ', buf[8]);
        assertEquals(0, buf[9]);
    }

    // ========== formatLongOctalBytes ==========

    @Test
    public void testFormatLongOctalBytes_valid_returnsOffset() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatLongOctalBytes(0, buf, 0, buf.length);
        assertEquals(10, result);
        assertEquals(' ', buf[9]);
        assertTrue("should end with space", buf[9] == ' ');
    }

    // ========== formatCheckSumOctalBytes ==========

    @Test
    public void testFormatCheckSumOctalBytes_valid_returnsOffset() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatCheckSumOctalBytes(0, buf, 0, buf.length);
        assertEquals(10, result);
        assertEquals(0, buf[8]);
        assertEquals(' ', buf[9]);
    }

    // ========== formatLongOctalOrBinaryBytes (round-trip) ==========

    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeShort_roundtrip() {
        roundTripFormatAndParse(-1L, 8);
        roundTripFormatAndParse(-256L, 8);
        roundTripFormatAndParse(Long.MIN_VALUE + 1, 8);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes_positiveLargeShort_roundtrip() {
        long max = 0x00FFFFFFFFFFFFFFL; // max positive for 8 bytes binary
        roundTripFormatAndParse(max, 8);
        roundTripFormatAndParse(max - 1, 8);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeLong_roundtrip() {
        roundTripFormatAndParse(-1L, 12);
        roundTripFormatAndParse(Long.MIN_VALUE, 12);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes_positiveLargeLong_roundtrip() {
        roundTripFormatAndParse(Long.MAX_VALUE, 12);
        roundTripFormatAndParse(0x1234567890L, 12);
    }

    // ========== verifyCheckSum ==========

    @Test
    public void testVerifyCheckSum_validChecksum_returnsTrue() {
        // header with all zeros -> unsignedSum = 8*32 = 256
        byte[] header = new byte[512];
        // storedSum = 256 = 0o400
        byte[] checksumBytes = "000400\0 ".getBytes();
        System.arraycopy(checksumBytes, 0, header, CHKSUM_OFFSET, CHKSUMLEN);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSum_storedSumGreaterThanUnsigned_returnsTrue() {
        byte[] header = new byte[512];
        // storedSum = 777777 octal = 262143 > unsignedSum (256)
        byte[] checksumBytes = "777777\0 ".getBytes();
        System.arraycopy(checksumBytes, 0, header, CHKSUM_OFFSET, CHKSUMLEN);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSum_storedSumLessThanUnsigned_returnsFalse() {
        byte[] header = new byte[512];
        // storedSum = 0, unsignedSum = 256
        byte[] checksumBytes = "000000\0 ".getBytes();
        System.arraycopy(checksumBytes, 0, header, CHKSUM_OFFSET, CHKSUMLEN);
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}