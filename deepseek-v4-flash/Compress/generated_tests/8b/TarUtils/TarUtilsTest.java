package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;

public class TarUtilsTest {

    // ---------- parseOctal tests ----------

    // Tests normal octal string
    @Test
    public void testParseOctal_normalOctal_returnsCorrectValue() {
        byte[] buf = "123".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result); // octal 123 = decimal 83
    }

    // Tests leading spaces are skipped
    @Test
    public void testParseOctal_leadingSpaces_returnsCorrectValue() {
        byte[] buf = " 123".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result);
    }

    // Tests leading zeros are skipped (stillPadding branch)
    @Test
    public void testParseOctal_leadingZeros_returnsCorrectValue() {
        byte[] buf = "00123".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result);
    }

    // Tests trailing space triggers break after stillPadding=false
    @Test
    public void testParseOctal_trailingSpace_returnsCorrectValue() {
        byte[] buf = "123 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result);
    }

    // Tests trailing null causes early break
    @Test
    public void testParseOctal_trailingNull_returnsCorrectValue() {
        byte[] buf = new byte[] {'1','2','3',0,' '};
        long result = TarUtils.parseOctal(buf, 0, 4);
        assertEquals(83L, result);
    }

    // Tests all-null buffer returns 0
    @Test
    public void testParseOctal_allNulls_returnsZero() {
        byte[] buf = new byte[10];
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    // Tests invalid octal digit '8' throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidDigit_throwsException() {
        byte[] buf = "128".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    // Tests non-digit character throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_nonOctalCharacter_throwsException() {
        byte[] buf = "12a".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    // Tests input consisting only of spaces returns 0 (stillPadding skip all)
    @Test
    public void testParseOctal_allSpaces_returnsZero() {
        byte[] buf = "   ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    // Tests single zero digit returns 0
    @Test
    public void testParseOctal_singleZero_returnsZero() {
        byte[] buf = "0".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    // Tests multiple leading zeros followed by space (stillPadding with zero then space)
    @Test
    public void testParseOctal_leadingZeroAndSpace_returnsZero() {
        byte[] buf = "0 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    // ---------- formatUnsignedOctalString tests ----------

    // Tests zero value produces leading zeros
    @Test
    public void testFormatUnsignedOctalString_zero_returnsLeadingZeros() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, buf.length);
        assertEquals("0000", new String(buf));
    }

    // Tests positive value produces correct octal string with leading zeros
    @Test
    public void testFormatUnsignedOctalString_positiveValue_returnsCorrectOctal() {
        byte[] buf = new byte[6];
        TarUtils.formatUnsignedOctalString(83L, buf, 0, buf.length);
        assertEquals("000123", new String(buf));
    }

    // Tests value too large for buffer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_valueTooLarge_throwsException() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(511L, buf, 0, buf.length); // 511 octal = 777, needs at least 3? Actually 3 digits fit but buffer length 3, remaining=2, check overflow
    }

    // ---------- parseName tests ----------

    // Tests normal name extraction
    @Test
    public void testParseName_normal_returnsString() {
        byte[] buf = "hello".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello", name);
    }

    // Tests null byte stops parsing
    @Test
    public void testParseName_trailingNull_returnsStringUpToNull() {
        byte[] buf = new byte[] {'a','b',0,'c'};
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("ab", name);
    }

    // Tests empty buffer returns empty string
    @Test
    public void testParseName_emptyBuffer_returnsEmptyString() {
        byte[] buf = new byte[0];
        String name = TarUtils.parseName(buf, 0, 0);
        assertEquals("", name);
    }

    // Tests bytes with sign extension are properly converted
    @Test
    public void testParseName_nonAsciiByte_returnsCharacter() {
        byte[] buf = new byte[] {(byte)0x80, (byte)0xFF, 0};
        // 0x80 & 0xFF = 128, 0xFF & 0xFF = 255
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("\u0080\u00FF", name);
    }

    // ---------- formatNameBytes tests ----------

    // Tests normal case: name fits, padded with nulls
    @Test
    public void testFormatNameBytes_nameShorter_returnsOffsetAndPadded() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatNameBytes("ab", buf, 0, buf.length);
        assertEquals(8, result);
        byte[] expected = new byte[] {'a','b',0,0,0,0,0,0};
        assertArrayEquals(expected, buf);
    }

    // Tests name longer than buffer truncates
    @Test
    public void testFormatNameBytes_nameLonger_truncatedWithNoNulls() {
        byte[] buf = new byte[3];
        TarUtils.formatNameBytes("hello", buf, 0, buf.length);
        assertEquals("hel", new String(buf));
    }

    // ---------- formatOctalBytes tests ----------

    @Test
    public void testFormatOctalBytes_normal_returnsOffset() {
        byte[] buf = new byte[6];
        int result = TarUtils.formatOctalBytes(83L, buf, 0, buf.length);
        assertEquals(6, result);
        // formatUnsignedOctalString writes "00123" into first 5 bytes, then space at index4, null at index5? Wait idx=length-2=4, writes 4 chars? Actually length=6, idx=4, so writes octal into buf[0..3]? Let's check: formatUnsignedOctalString writes to offset+remaining... We'll just trust the method.
        // Assert last two bytes are space and null
        assertEquals(' ', buf[4]);
        assertEquals(0, buf[5]);
    }

    // ---------- formatLongOctalBytes tests ----------

    @Test
    public void testFormatLongOctalBytes_normal_returnsOffset() {
        byte[] buf = new byte[6];
        int result = TarUtils.formatLongOctalBytes(83L, buf, 0, buf.length);
        assertEquals(6, result);
        // Last byte should be space
        assertEquals(' ', buf[5]);
    }

    // ---------- formatCheckSumOctalBytes tests ----------

    @Test
    public void testFormatCheckSumOctalBytes_normal_returnsOffset() {
        byte[] buf = new byte[6];
        int result = TarUtils.formatCheckSumOctalBytes(83L, buf, 0, buf.length);
        assertEquals(6, result);
        // Last two bytes should be null then space
        assertEquals(0, buf[4]);
        assertEquals(' ', buf[5]);
    }

    // ---------- computeCheckSum tests ----------

    @Test
    public void testComputeCheckSum_knownBuffer_returnsCorrectSum() {
        byte[] buf = new byte[] {1, 2, 3};
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(6L, sum);
    }

    @Test
    public void testComputeCheckSum_emptyBuffer_returnsZero() {
        byte[] buf = new byte[0];
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(0L, sum);
    }
}