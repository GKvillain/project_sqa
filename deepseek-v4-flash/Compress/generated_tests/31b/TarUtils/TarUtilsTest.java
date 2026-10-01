package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUM_OFFSET;
import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUMLEN;

import org.junit.Test;

public class TarUtilsTest {

    // Parsing octal: length < 2 should throw IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThan2_throwsException() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Parsing octal: leading NUL returns 0
    @Test
    public void testParseOctal_leadingNull_returnsZero() {
        byte[] buffer = new byte[] {0, '1', '2'};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }

    // Parsing octal: leading spaces skipped
    @Test
    public void testParseOctal_leadingSpaces_skips() {
        byte[] buffer = new byte[] {' ', ' ', '1', '2', ' ', 0};
        assertEquals(10L, TarUtils.parseOctal(buffer, 0, 6));
    }

    // Parsing octal: valid octal produces correct long value
    @Test
    public void testParseOctal_validOctal_returnsValue() {
        byte[] buffer = new byte[] {'1', '2', '3', '4', '5', 0};
        // 12345 octal = 5349 decimal
        assertEquals(5349L, TarUtils.parseOctal(buffer, 0, 6));
    }

    // Parsing octal: invalid character should throw
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidCharacter_throwsException() {
        byte[] buffer = new byte[] {'1', '8', ' ', 0};
        TarUtils.parseOctal(buffer, 0, 4);
    }

    // Parsing octal or binary: MSB not set -> octal branch
    @Test
    public void testParseOctalOrBinary_octalBranch() {
        byte[] buffer = new byte[] {'1', '2', '3', ' ', 0};
        assertEquals(83L, TarUtils.parseOctalOrBinary(buffer, 0, 5));
    }

    // Parsing octal or binary: positive binary (length < 9)
    @Test
    public void testParseOctalOrBinary_binaryPositiveSmall() {
        byte[] buffer = new byte[] {(byte) 0x80, 0x01}; // value = 1
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 2));
    }

    // Parsing octal or binary: negative binary (length < 9)
    @Test
    public void testParseOctalOrBinary_binaryNegativeSmall() {
        byte[] buffer = new byte[] {(byte) 0xff, (byte) 0xff}; // two's complement -1
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 2));
    }

    // Parsing octal or binary: big integer (length >= 9) positive
    @Test
    public void testParseOctalOrBinary_binaryBigInteger() {
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0x80;           // positive binary marker
        buffer[9] = 1;                     // last byte = 1
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 10));
    }

    // Parsing octal or binary: overflow (> 63 bit)
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinary_binaryOverflow() {
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0x80;
        for (int i = 1; i < 10; i++) {
            buffer[i] = (byte) 0xff;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, 10);
    }

    // parseBoolean returns true when byte == 1
    @Test
    public void testParseBoolean_byte1_returnsTrue() {
        byte[] buffer = new byte[] {1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    // parseBoolean returns false when byte != 1 (0)
    @Test
    public void testParseBoolean_byte0_returnsFalse() {
        byte[] buffer = new byte[] {0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    // parseName stops at NUL
    @Test
    public void testParseName_withNullTermination() {
        byte[] buffer = new byte[] {'h', 'e', 'l', 'l', 'o', 0, 'x'};
        assertEquals("hello", TarUtils.parseName(buffer, 0, 7));
    }

    // parseName returns empty string when all bytes are NUL
    @Test
    public void testParseName_emptyBuffer_returnsEmpty() {
        byte[] buffer = new byte[] {0, 0, 0};
        assertEquals("", TarUtils.parseName(buffer, 0, 3));
    }

    // formatUnsignedOctalString: zero value
    @Test
    public void testFormatUnsignedOctalString_zero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 4);
        assertArrayEquals(new byte[] {'0', '0', '0', '0'}, buf);
    }

    // formatUnsignedOctalString: overflow should throw
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_overflow() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(64L, buf, 0, 2); // 64 decimal = 100 octal, needs 3 digits
    }

    // formatUnsignedOctalString: normal positive value
    @Test
    public void testFormatUnsignedOctalString_normal() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(10L, buf, 0, 4); // 10 decimal = 12 octal
        assertArrayEquals(new byte[] {'0', '0', '1', '2'}, buf);
    }

    // formatLongOctalOrBinaryBytes: value fits in octal
    @Test
    public void testFormatLongOctalOrBinaryBytes_octal() {
        byte[] buf = new byte[8];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(100L, buf, 0, 8);
        assertEquals(8, newOffset);
        assertEquals(' ', buf[7] & 0xff);   // trailing space
        assertEquals(0, buf[0] & 0xff);     // leading zero
    }

    // formatLongOctalOrBinaryBytes: negative value stored as binary
    @Test
    public void testFormatLongOctalOrBinaryBytes_binaryNegative() {
        byte[] buf = new byte[10];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 10);
        assertEquals((byte) 0xff, buf[0]);     // negative marker
        for (int i = 1; i < 10; i++) {
            assertEquals((byte) 0xff, buf[i]); // all bytes 0xff for -1
        }
    }

    // formatLongOctalOrBinaryBytes: binary overflow in formatLongBinary
    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytes_overflowBinary() {
        byte[] buf = new byte[2];
        // 256 requires 1 byte of data (max 255) -> overflow
        TarUtils.formatLongOctalOrBinaryBytes(256L, buf, 0, 2);
    }

    // verifiyCheckSum: stored == signed sum
    @Test
    public void testVerifyCheckSum_signedSumMatch() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) (i % 256);

        // compute signed sum with checksum bytes replaced by spaces
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            byte b = header[i];
            if (i >= CHKSUM_OFFSET && i < CHKSUM_OFFSET + CHKSUMLEN) {
                b = ' ';
            }
            signedSum += b;
        }

        // write stored checksum as octal with trailing NUL and space
        String octal = Long.toOctalString(signedSum);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        header[CHKSUM_OFFSET + 6] = 0;
        header[CHKSUM_OFFSET + 7] = ' ';

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // verifiyCheckSum: stored == unsigned sum
    @Test
    public void testVerifyCheckSum_unsignedSumMatch() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) (i % 256);

        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            byte b = header[i];
            if (i >= CHKSUM_OFFSET && i < CHKSUM_OFFSET + CHKSUMLEN) {
                b = ' ';
            }
            unsignedSum += (b & 0xff);
        }

        String octal = Long.toOctalString(unsignedSum);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        header[CHKSUM_OFFSET + 6] = 0;
        header[CHKSUM_OFFSET + 7] = ' ';

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // verifiyCheckSum: stored > unsigned (COMPRESS-177)
    @Test
    public void testVerifyCheckSum_storedGreaterThanUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) (i % 256);

        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            byte b = header[i];
            if (i >= CHKSUM_OFFSET && i < CHKSUM_OFFSET + CHKSUMLEN) {
                b = ' ';
            }
            unsignedSum += (b & 0xff);
        }

        long storedSum = unsignedSum + 100;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        header[CHKSUM_OFFSET + 6] = 0;
        header[CHKSUM_OFFSET + 7] = ' ';

        assertTrue("Expected true when stored > unsigned", TarUtils.verifyCheckSum(header));
    }

    // computeCheckSum returns sum of unsigned bytes
    @Test
    public void testComputeCheckSum_simple() {
        byte[] buf = new byte[] {1, 2, 3};
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    // ==================== New test cases for uncovered lines ====================

    // parseName: non-empty name without NUL termination
    @Test
    public void testParseName_noNullTermination() {
        byte[] buffer = new byte[] {'h', 'e', 'l', 'l', 'o'};
        assertEquals("hello", TarUtils.parseName(buffer, 0, 5));
    }

    // parseName: empty buffer with all NULs (non-zero length)
    @Test
    public void testParseName_allNulls() {
        byte[] buffer = new byte[] {0, 0, 0, 0};
        assertEquals("", TarUtils.parseName(buffer, 0, 4));
    }

    // parseOctalOrBinary: negative binary with length >= 9
    @Test
    public void testParseOctalOrBinary_binaryNegativeBig() {
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0xff;  // negative marker
        for (int i = 1; i < 10; i++) {
            buffer[i] = (byte) 0xff;
        }
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, 10));
    }

    // formatLongOctalOrBinaryBytes: positive value stored as binary (doesn't fit octal)
    @Test
    public void testFormatLongOctalOrBinaryBytes_binaryPositive() {
        byte[] buf = new byte[4];
        // Value that doesn't fit in octal representation at this length
        // For length=4, max octal is 7777 (decimal 4095)
        TarUtils.formatLongOctalOrBinaryBytes(5000L, buf, 0, 4);
        assertEquals((byte) 0x80, buf[0]);  // positive binary marker
    }

    // formatLongOctalOrBinaryBytes: value fits in octal but with leading zeros
    @Test
    public void testFormatLongOctalOrBinaryBytes_octalWithLeadingZeros() {
        byte[] buf = new byte[12];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(0L, buf, 0, 12);
        assertEquals(12, newOffset);
        // Should be all '0' characters followed by space
        for (int i = 0; i < 11; i++) {
            assertEquals('0', buf[i]);
        }
        assertEquals(' ', buf[11]);
    }

    // formatUnsignedOctalString: value exactly fits buffer
    @Test
    public void testFormatUnsignedOctalString_exactFit() {
        byte[] buf = new byte[3];
        // 64 octal = 100, needs exactly 3 digits
        TarUtils.formatUnsignedOctalString(64L, buf, 0, 3);
        assertArrayEquals(new byte[] {'1', '0', '0'}, buf);
    }

    // parseOctal: all spaces (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_allSpaces_throwsException() {
        byte[] buffer = new byte[] {' ', ' ', ' ', 0};
        TarUtils.parseOctal(buffer, 0, 4);
    }

    // parseOctal: only NUL after spaces
    @Test
    public void testParseOctal_spacesThenNull() {
        byte[] buffer = new byte[] {' ', ' ', 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }
}