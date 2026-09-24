package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class TarUtilsTest {

    // Tests standard valid octal parsing
    @Test
    public void testParseOctal_standardInput_returnsCorrectValue() {
        byte[] buffer = "0000755 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, value);
    }

    // Tests octal parsing with leading spaces and trailing space/NUL
    @Test
    public void testParseOctal_leadingSpacesAndTrailingSpaceNul_returnsCorrectValue() {
        byte[] buffer = "   123 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, value);
    }

    // Tests octal parsing when buffer contains all NUL bytes
    @Test
    public void testParseOctal_allNulBytes_returnsZero() {
        byte[] buffer = new byte[8];
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    // Tests octal parsing when leading byte is NUL
    @Test
    public void testParseOctal_leadingNul_returnsZero() {
        byte[] buffer = new byte[] { 0, '1', '2', '3', ' ', 0 };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    // Tests octal parsing when length is less than 2
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsException() {
        byte[] buffer = new byte[] { '7' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests octal parsing with invalid trailing byte
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidTrailer_throwsException() {
        byte[] buffer = "0000755A".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests octal parsing with invalid non-octal digit
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidDigit_throwsException() {
        byte[] buffer = "0000855 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests defect where tar entry contains multiple trailing NUL bytes (COMPRESS-17)
    @Test
    public void testParseOctal_multipleTrailingNulBytes_returnsCorrectValue() {
        byte[] buffer = new byte[] { '0', '1', '4', '5', ' ', 0, 0, 0 };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0145L, value);
    }

    // Tests parseOctalOrBinary with standard octal input
    @Test
    public void testParseOctalOrBinary_octalValue_returnsParsedValue() {
        byte[] buffer = "0000777 \0".getBytes();
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(0777L, value);
    }

    // Tests parseOctalOrBinary with 8-byte positive binary number
    @Test
    public void testParseOctalOrBinary_positiveBinary8Bytes_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 1, 0 };
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(256L, value);
    }

    // Tests parseOctalOrBinary with 8-byte negative binary number
    @Test
    public void testParseOctalOrBinary_negativeBinary8Bytes_returnsCorrectValue() {
        byte[] buffer = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                     (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe };
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, value);
    }

    // Tests parseOctalOrBinary with BigInteger-length (12 bytes) positive binary
    @Test
    public void testParseOctalOrBinary_positiveBinary12Bytes_returnsCorrectValue() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[11] = 0x2A;
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(42L, value);
    }

    // Tests parseOctalOrBinary with BigInteger-length (12 bytes) negative binary
    @Test
    public void testParseOctalOrBinary_negativeBinary12Bytes_returnsCorrectValue() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0xff;
        for (int i = 1; i < 11; i++) {
            buffer[i] = (byte) 0xff;
        }
        buffer[11] = (byte) 0xfe;
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, value);
    }

    // Tests parseBoolean behavior
    @Test
    public void testParseBoolean_byteValues_returnsExpectedBoolean() {
        byte[] buffer = new byte[] { 1, 0 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
    }

    // Tests parseName and formatNameBytes round-trip
    @Test
    public void testParseNameAndFormatNameBytes_validName_roundTripsCorrectly() {
        byte[] buffer = new byte[100];
        String name = "test/entry/path.txt";
        int offset = TarUtils.formatNameBytes(name, buffer, 0, buffer.length);
        assertEquals(100, offset);

        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals(name, parsed);
    }

    // Tests formatNameBytes when name is longer than buffer length
    @Test
    public void testFormatNameBytes_truncatedWhenLongerThanLength() {
        byte[] buffer = new byte[5];
        TarUtils.formatNameBytes("abcdefgh", buffer, 0, 5);
        String parsed = TarUtils.parseName(buffer, 0, 5);
        assertEquals("abcde", parsed);
    }

    // Tests parseName with empty or all-NUL buffer
    @Test
    public void testParseName_allNulBuffer_returnsEmptyString() {
        byte[] buffer = new byte[10];
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", parsed);
    }

    // Tests formatUnsignedOctalString overflow throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_overflow_throwsException() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(07777L, buffer, 0, 2);
    }

    // Tests formatOctalBytes formatting with trailing space and NUL
    @Test
    public void testFormatOctalBytes_validValue_formatsCorrectly() {
        byte[] buffer = new byte[8];
        int end = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, end);
        assertEquals("0000755 ", new String(buffer, 0, 7));
        assertEquals(0, buffer[7]);
    }

    // Tests formatLongOctalBytes formatting with trailing space
    @Test
    public void testFormatLongOctalBytes_validValue_formatsCorrectly() {
        byte[] buffer = new byte[8];
        int end = TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, end);
        assertEquals("0000755 ", new String(buffer));
    }

    // Tests formatLongOctalOrBinaryBytes for binary format on large/negative values
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeValue_formatsBinary() {
        byte[] buffer = new byte[8];
        int end = TarUtils.formatLongOctalOrBinaryBytes(-1L, buffer, 0, buffer.length);
        assertEquals(8, end);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-1L, parsed);
    }

    // Tests formatCheckSumOctalBytes, computeCheckSum and verifyCheckSum
    @Test
    public void testChecksumMethods_validHeader_computesAndVerifiesCorrectly() {
        byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i % 128);
        }
        // Fill checksum field with spaces initially as per TAR specification
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }
}