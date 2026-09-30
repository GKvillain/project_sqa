package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * JUnit 4 test class for TarUtils.
 * Focuses on parseOctal (likely buggy) and other public static methods.
 */
public class TarUtilsTest {

    // Helper to create buffer from string with explicit length
    private byte[] buffer(final String s, final int length) {
        byte[] b = new byte[length];
        byte[] src = s.getBytes();
        System.arraycopy(src, 0, b, 0, Math.min(src.length, length));
        return b;
    }

    private byte[] buffer(final String s) {
        return s.getBytes();
    }

    // ---- parseOctal tests ----

    @Test
    // All spaces -> 0
    public void testParseOctal_allSpaces_returnsZero() {
        byte[] buf = buffer("   ", 4);
        assertEquals(0L, TarUtils.parseOctal(buf, 0, 4));
    }

    @Test
    // All zeros -> 0
    public void testParseOctal_allZeros_returnsZero() {
        byte[] buf = buffer("0000", 4);
        assertEquals(0L, TarUtils.parseOctal(buf, 0, 4));
    }

    @Test
    // Leading spaces then zeros -> 0
    public void testParseOctal_spacesAndZeros_returnsZero() {
        byte[] buf = buffer(" 00", 4);
        assertEquals(0L, TarUtils.parseOctal(buf, 0, 4));
    }

    @Test
    // Valid octal number without padding
    public void testParseOctal_validDigits_returnsValue() {
        byte[] buf = buffer("1234", 4);
        // 1234 octal = 1*512 + 2*64 + 3*8 + 4 = 512+128+24+4 = 668
        assertEquals(668L, TarUtils.parseOctal(buf, 0, 4));
    }

    @Test
    // Leading spaces then valid digits
    public void testParseOctal_leadingSpacesThenDigits_returnsValue() {
        byte[] buf = buffer("  1234", 6);
        assertEquals(668L, TarUtils.parseOctal(buf, 0, 6));
    }

    @Test
    // Leading zeros then valid digits
    public void testParseOctal_leadingZerosThenDigits_returnsValue() {
        byte[] buf = buffer("0001234", 7);
        assertEquals(668L, TarUtils.parseOctal(buf, 0, 7));
    }

    @Test
    // Trailing null stops parsing
    public void testParseOctal_trailingNull_returnsValue() {
        byte[] buf = new byte[] { '1', '2', 0, '3' };
        // 12 octal = 10 decimal
        assertEquals(10L, TarUtils.parseOctal(buf, 0, 4));
    }

    @Test
    // Trailing space after digits stops parsing
    public void testParseOctal_trailingSpace_returnsValue() {
        byte[] buf = buffer("12 ", 3);
        assertEquals(10L, TarUtils.parseOctal(buf, 0, 3));
    }

    @Test
    // Space after digits with trailing zeros (padding) – space breaks
    public void testParseOctal_spaceAfterDigitsWithPadding_returnsValue() {
        byte[] buf = buffer("12 0", 4);
        assertEquals(10L, TarUtils.parseOctal(buf, 0, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    // Invalid octal digit throws exception
    public void testParseOctal_invalidDigit_throwsException() {
        byte[] buf = buffer("18", 2);
        TarUtils.parseOctal(buf, 0, 2);
    }

    @Test
    // Zero-length input returns 0
    public void testParseOctal_zeroLength_returnsZero() {
        byte[] buf = buffer("123", 3);
        assertEquals(0L, TarUtils.parseOctal(buf, 0, 0));
    }

    @Test
    // Buffer with only nulls returns 0
    public void testParseOctal_onlyNulls_returnsZero() {
        byte[] buf = new byte[] { 0, 0, 0 };
        assertEquals(0L, TarUtils.parseOctal(buf, 0, 3));
    }

    // Additional parseOctal test: offset non-zero
    @Test
    public void testParseOctal_offsetNonZero_returnsValue() {
        byte[] buf = buffer("xxx1234", 7);
        assertEquals(668L, TarUtils.parseOctal(buf, 3, 4));
    }

    // ---- parseName tests ----

    @Test
    // Simple name without null termination
    public void testParseName_simpleNoNull_returnsName() {
        byte[] buf = buffer("hello");
        assertEquals("hello", TarUtils.parseName(buf, 0, 5));
    }

    @Test
    // Name with null termination stops parsing
    public void testParseName_withNull_returnsName() {
        byte[] buf = new byte[] { 'h', 'i', 0, 'x' };
        assertEquals("hi", TarUtils.parseName(buf, 0, 4));
    }

    @Test
    // Empty buffer
    public void testParseName_emptyBuffer_returnsEmptyString() {
        byte[] buf = new byte[0];
        assertEquals("", TarUtils.parseName(buf, 0, 0));
    }

    @Test
    // Offset non-zero
    public void testParseName_offsetNonZero_returnsCorrectName() {
        byte[] buf = new byte[] { 'x', 'y', 'a', 'b', 0 };
        assertEquals("ab", TarUtils.parseName(buf, 2, 3));
    }

    @Test
    // Leading null -> empty string
    public void testParseName_leadingNull_returnsEmptyString() {
        byte[] buf = new byte[] { 0, 'a', 'b' };
        assertEquals("", TarUtils.parseName(buf, 0, 3));
    }

    @Test
    // Name exactly fills buffer without null (no truncation, returns full name)
    public void testParseName_exactLengthNoNull_returnsFullName() {
        byte[] buf = buffer("abc");
        assertEquals("abc", TarUtils.parseName(buf, 0, 3));
    }

    // ---- formatNameBytes tests ----

    @Test
    // Copy name shorter than buffer, fill rest with NUL
    public void testFormatNameBytes_nameShorter_returnsOffset() {
        byte[] buf = new byte[10];
        int newOffset = TarUtils.formatNameBytes("abc", buf, 0, 10);
        assertEquals(10, newOffset);
        byte[] expected = new byte[] { 'a', 'b', 'c', 0, 0, 0, 0, 0, 0, 0 };
        assertArrayEquals(expected, buf);
    }

    @Test
    // Name longer than buffer, truncate
    public void testFormatNameBytes_nameLonger_truncates() {
        byte[] buf = new byte[3];
        TarUtils.formatNameBytes("abcdef", buf, 0, 3);
        assertArrayEquals(new byte[] { 'a', 'b', 'c' }, buf);
    }

    @Test
    // Offset non-zero
    public void testFormatNameBytes_offsetNonZero_padsCorrectly() {
        byte[] buf = new byte[6];
        int newOffset = TarUtils.formatNameBytes("ab", buf, 2, 4);
        assertEquals(6, newOffset);
        assertArrayEquals(new byte[] { 0, 0, 'a', 'b', 0, 0 }, buf);
    }

    @Test
    // Empty name
    public void testFormatNameBytes_emptyName_padsWithNulls() {
        byte[] buf = new byte[4];
        int newOffset = TarUtils.formatNameBytes("", buf, 0, 4);
        assertEquals(4, newOffset);
        assertArrayEquals(new byte[] { 0, 0, 0, 0 }, buf);
    }

    @Test
    // Name length equals buffer length (no null terminator if buffer full)
    public void testFormatNameBytes_nameEqualsBufferLength() {
        byte[] buf = new byte[3];
        TarUtils.formatNameBytes("abc", buf, 0, 3);
        assertArrayEquals(new byte[] { 'a', 'b', 'c' }, buf);
    }

    // ---- formatUnsignedOctalString tests ----

    @Test
    // Zero value produces '0' with leading zeros
    public void testFormatUnsignedOctalString_zero_putsZero() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 5);
        assertArrayEquals(new byte[] { '0', '0', '0', '0', '0' }, buf);
    }

    @Test
    // Typical value fits
    public void testFormatUnsignedOctalString_typical_works() {
        byte[] buf = new byte[6];
        TarUtils.formatUnsignedOctalString(13L, buf, 0, 6);
        // 13 decimal = 15 octal, should be "000015" (6 chars)
        assertArrayEquals(new byte[] { '0', '0', '0', '0', '1', '5' }, buf);
    }

    @Test(expected = IllegalArgumentException.class)
    // Value too large for buffer throws exception
    public void testFormatUnsignedOctalString_valueOverflows_throws() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(8L, buf, 0, 2); // 8 decimal = 10 octal, needs at least 2 digits
    }

    @Test
    // Smallest non-zero value
    public void testFormatUnsignedOctalString_one_works() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(1L, buf, 0, 3);
        assertArrayEquals(new byte[] { '0', '0', '1' }, buf);
    }

    @Test
    // Value 7 (single octal digit)
    public void testFormatUnsignedOctalString_seven_works() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(7L, buf, 0, 3);
        assertArrayEquals(new byte[] { '0', '0', '7' }, buf);
    }

    // ---- formatOctalBytes tests ----

    @Test
    // Check proper formatting with space and null
    public void testFormatOctalBytes_correctlyFormatted() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatOctalBytes(7L, buf, 0, 5);
        assertEquals(5, offset);
        // 7 octal = "07", then space, then null -> length 5
        assertArrayEquals(new byte[] { '0', '7', ' ', 0 }, buf);
    }

    @Test
    // Value zero
    public void testFormatOctalBytes_zero_works() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatOctalBytes(0L, buf, 0, 5);
        assertEquals(5, offset);
        assertArrayEquals(new byte[] { '0', '0', ' ', 0 }, buf);
    }

    @Test
    // Large value within buffer limits
    public void testFormatOctalBytes_largeValue_works() {
        byte[] buf = new byte[6];
        int offset = TarUtils.formatOctalBytes(64L, buf, 0, 6); // 64 decimal = 100 octal
        assertEquals(6, offset);
        assertArrayEquals(new byte[] { '1', '0', '0', ' ', 0 }, buf);
    }

    @Test(expected = IllegalArgumentException.class)
    // Buffer too small for value
    public void testFormatOctalBytes_bufferTooSmall_throws() {
        byte[] buf = new byte[3];
        TarUtils.formatOctalBytes(10L, buf, 0, 3); // 10 decimal = 12 octal, needs at least 2 digits + space + null = 4
    }

    // ---- formatLongOctalBytes tests ----

    @Test
    // Check proper formatting with trailing space
    public void testFormatLongOctalBytes_correctlyFormatted() {
        byte[] buf = new byte[4];
        int offset = TarUtils.formatLongOctalBytes(11L, buf, 0, 4);
        assertEquals(4, offset);
        // 11 octal = "13", then space -> length 4: "013 "
        assertArrayEquals(new byte[] { '0', '1', '3', ' ' }, buf);
    }

    @Test
    // Value zero
    public void testFormatLongOctalBytes_zero_works() {
        byte[] buf = new byte[4];
        int offset = TarUtils.formatLongOctalBytes(0L, buf, 0, 4);
        assertEquals(4, offset);
        assertArrayEquals(new byte[] { '0', '0', '0', ' ' }, buf);
    }

    @Test
    // Large value
    public void testFormatLongOctalBytes_largeValue_works() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatLongOctalBytes(64L, buf, 0, 5); // 64 decimal = 100 octal
        assertEquals(5, offset);
        assertArrayEquals(new byte[] { '0', '1', '0', '0', ' ' }, buf);
    }

    @Test(expected = IllegalArgumentException.class)
    // Buffer too small
    public void testFormatLongOctalBytes_bufferTooSmall_throws() {
        byte[] buf = new byte[2];
        TarUtils.formatLongOctalBytes(10L, buf, 0, 2); // "12 " min 3 bytes
    }

    // ---- formatCheckSumOctalBytes tests ----

    @Test
    // Check proper ordering: null then space
    public void testFormatCheckSumOctalBytes_correctlyFormatted() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatCheckSumOctalBytes(15L, buf, 0, 5);
        assertEquals(5, offset);
        // 15 octal = "017", then null, then space -> indices: 0='0',1='1',2='7',3=0,4=' '
        assertArrayEquals(new byte[] { '0', '1', '7', 0, ' ' }, buf);
    }

    @Test
    // Value zero
    public void testFormatCheckSumOctalBytes_zero_works() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatCheckSumOctalBytes(0L, buf, 0, 5);
        assertEquals(5, offset);
        assertArrayEquals(new byte[] { '0', '0', '0', 0, ' ' }, buf);
    }

    @Test
    // Large value
    public void testFormatCheckSumOctalBytes_largeValue_works() {
        byte[] buf = new byte[6];
        int offset = TarUtils.formatCheckSumOctalBytes(64L, buf, 0, 6); // 64 decimal = 100 octal
        assertEquals(6, offset);
        assertArrayEquals(new byte[] { '1', '0', '0', 0, ' ' }, buf);
    }

    @Test(expected = IllegalArgumentException.class)
    // Buffer too small
    public void testFormatCheckSumOctalBytes_bufferTooSmall_throws() {
        byte[] buf = new byte[3];
        TarUtils.formatCheckSumOctalBytes(8L, buf, 0, 3); // "10" null space needs 4
    }

    // ---- computeCheckSum tests ----

    @Test
    // Simple sum of bytes
    public void testComputeCheckSum_simple_returnsSum() {
        byte[] buf = new byte[] { 1, 2, 3 };
        // 1+2+3 = 6
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    @Test
    // Buffer with all zeros
    public void testComputeCheckSum_allZeros_returnsZero() {
        byte[] buf = new byte[10];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    @Test
    // Negative bytes (signed byte -1 = 255 as unsigned int)
    public void testComputeCheckSum_negativeBytes_returnsSum() {
        byte[] buf = new byte[] { -1, 1 }; // -1 + 1 = 0? Actually as unsigned: 255+1=256, but Java byte sum with int: -1+1=0
        // computeCheckSum likely treats bytes as unsigned? Let's test actual behavior: TarUtils.computeCheckSum does (buf[i] & 0xFF)
        // So sum = 255 + 1 = 256
        assertEquals(256L, TarUtils.computeCheckSum(buf));
    }

    @Test
    // Empty buffer
    public void testComputeCheckSum_emptyBuffer_returnsZero() {
        byte[] buf = new byte[0];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }
}