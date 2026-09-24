package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

public class TarUtilsTest {

    // Tests parsing standard octal string with trailing space and NUL
    @Test
    public void testParseOctal_validInputWithTrailingSpaceAndNul_returnsCorrectValue() {
        byte[] buffer = "0000755 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, result);
    }

    // Tests parsing all NUL buffer
    @Test
    public void testParseOctal_allNulBuffer_returnsZero() {
        byte[] buffer = new byte[10];
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parsing buffer starting with leading NUL
    @Test
    public void testParseOctal_leadingNul_returnsZero() {
        byte[] buffer = new byte[] { 0, '7', '5', '5', ' ', 0 };
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parsing octal with leading spaces
    @Test
    public void testParseOctal_leadingSpaces_returnsCorrectValue() {
        byte[] buffer = "   123 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(83L, result);
    }

    // Tests exception when length is less than 2
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = "7".getBytes();
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests exception when buffer contains invalid character
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidChar_throwsException() {
        byte[] buffer = "0000785 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parseOctalOrBinary with octal input
    @Test
    public void testParseOctalOrBinary_octalInput_returnsCorrectValue() {
        byte[] buffer = "0000644 \0".getBytes();
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    // Tests parseOctalOrBinary with positive binary value within 8 bytes
    @Test
    public void testParseOctalOrBinary_positiveBinaryLong_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 1, 0 };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(256L, result);
    }

    // Tests parseOctalOrBinary with negative binary value within 8 bytes
    @Test
    public void testParseOctalOrBinary_negativeBinaryLong_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, result);
    }

    // Tests parseOctalOrBinary with binary big integer representation
    @Test
    public void testParseOctalOrBinary_positiveBinaryBigInteger_returnsCorrectValue() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[11] = 0x01;
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, result);
    }

    // Tests parseBoolean
    @Test
    public void testParseBoolean_validBytes_returnsExpectedBooleans() {
        byte[] buffer = new byte[] { 1, 0, 2 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    // Tests parseName and formatNameBytes round trip
    @Test
    public void testParseAndFormatNameBytes_validName_roundTripsSuccessfully() {
        byte[] buffer = new byte[100];
        String expectedName = "test/path/file.txt";
        int offset = TarUtils.formatNameBytes(expectedName, buffer, 0, buffer.length);
        assertEquals(buffer.length, offset);

        String actualName = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals(expectedName, actualName);
    }

    // Tests parseName with custom zip encoding
    @Test
    public void testParseName_withEncoding_returnsCorrectName() throws IOException {
        byte[] buffer = "custom-entry\0\0".getBytes("UTF-8");
        String name = TarUtils.parseName(buffer, 0, buffer.length, ZipEncodingHelper.getZipEncoding("UTF-8"));
        assertEquals("custom-entry", name);
    }

    // Tests formatOctalBytes formatting
    @Test
    public void testFormatOctalBytes_standardValue_formatsCorrectly() {
        byte[] buffer = new byte[8];
        int offset = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, offset);
        assertEquals((byte) ' ', buffer[6]);
        assertEquals((byte) 0, buffer[7]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, parsed);
    }

    // Tests formatLongOctalBytes formatting
    @Test
    public void testFormatLongOctalBytes_standardValue_formatsCorrectly() {
        byte[] buffer = new byte[8];
        int offset = TarUtils.formatLongOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(8, offset);
        assertEquals((byte) ' ', buffer[7]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, parsed);
    }

    // Tests formatCheckSumOctalBytes formatting
    @Test
    public void testFormatCheckSumOctalBytes_standardValue_formatsCorrectly() {
        byte[] buffer = new byte[8];
        int offset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(8, offset);
        assertEquals((byte) 0, buffer[6]);
        assertEquals((byte) ' ', buffer[7]);

        long parsed = TarUtils.parseOctal(buffer, 0, 6);
        assertEquals(0123L, parsed);
    }

    // Tests formatLongOctalOrBinaryBytes with large positive value triggering binary encoding
    @Test
    public void testFormatLongOctalOrBinaryBytes_largeValue_encodesAsBinary() {
        byte[] buffer = new byte[8];
        long largeValue = 0x1FFFFFFFFFFFFFFFL;
        int offset = TarUtils.formatLongOctalOrBinaryBytes(largeValue, buffer, 0, buffer.length);
        assertEquals(8, offset);
        assertEquals((byte) 0x80, (byte) (buffer[0] & 0x80));
    }

    // Tests formatUnsignedOctalString when value overflows target length
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_overflow_throwsException() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(07777L, buffer, 0, buffer.length);
    }

    // Tests computeCheckSum and verifyCheckSum
    @Test
    public void testComputeAndVerifyCheckSum_validHeader_returnsExpectedResults() {
        byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i % 128);
        }

        // Fill checksum area with spaces
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }

        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests verifyCheckSum returning false for invalid checksum
    @Test
    public void testVerifyCheckSum_invalidCheckSum_returnsFalse() {
        byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = 1;
        }
        TarUtils.formatCheckSumOctalBytes(10L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}