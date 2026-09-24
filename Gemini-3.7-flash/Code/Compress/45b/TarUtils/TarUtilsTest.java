package org.apache.commons.compress.archivers.tar;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TarUtilsTest {

    // Tests normal octal parsing with leading/trailing spaces and trailing NUL
    @Test
    public void testParseOctal_validOctal_returnsCorrectValue() {
        final byte[] buffer = " 0755 \0".getBytes();
        final long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, result);
    }

    // Tests buffer with leading NUL returns 0
    @Test
    public void testParseOctal_leadingNull_returnsZero() {
        final byte[] buffer = new byte[] {0, '1', '2', '3', ' ', 0};
        final long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // Tests parsing octal with length less than 2 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_lengthLessThanTwo_throwsIllegalArgumentException() {
        final byte[] buffer = new byte[] {'7'};
        TarUtils.parseOctal(buffer, 0, 1);
    }

    // Tests parsing octal with invalid character throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctal_invalidCharacter_throwsIllegalArgumentException() {
        final byte[] buffer = " 0785 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    // Tests parseOctalOrBinary when high bit is not set (delegates to octal)
    @Test
    public void testParseOctalOrBinary_standardOctal_returnsParsedValue() {
        final byte[] buffer = " 0100 \0".getBytes();
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(0100L, result);
    }

    // Tests parseOctalOrBinary with positive binary value under 9 bytes
    @Test
    public void testParseOctalOrBinary_positiveBinaryLong_returnsParsedValue() {
        final byte[] buffer = new byte[] {
            (byte) 0x80, 0, 0, 0, 0, 0, 0x01, 0x00
        };
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(256L, result);
    }

    // Tests parseOctalOrBinary with negative binary value under 9 bytes
    @Test
    public void testParseOctalOrBinary_negativeBinaryLong_returnsParsedValue() {
        final byte[] buffer = new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe
        };
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, result);
    }

    // Tests parseOctalOrBinary with binary length >= 9 using BigInteger
    @Test
    public void testParseOctalOrBinary_binaryBigInteger_returnsParsedValue() {
        final byte[] buffer = new byte[] {
            (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x02, 0x00
        };
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(512L, result);
    }

    // Tests parseBoolean returns true when byte is 1, false otherwise
    @Test
    public void testParseBoolean_validInput_returnsCorrectBoolean() {
        final byte[] buffer = new byte[] {1, 0};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
    }

    // Tests parseName and formatNameBytes round-trip
    @Test
    public void testParseNameAndFormatName_roundTrip_matches() {
        final String name = "test/path/file.txt";
        final byte[] buffer = new byte[30];
        final int offset = TarUtils.formatNameBytes(name, buffer, 0, buffer.length);
        assertEquals(30, offset);

        final String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals(name, parsed);
    }

    // Tests formatNameBytes when name is longer than length truncates correctly
    @Test
    public void testFormatNameBytes_truncated_matchesExpected() {
        final String name = "long_file_name_that_exceeds_buffer";
        final byte[] buffer = new byte[10];
        TarUtils.formatNameBytes(name, buffer, 0, buffer.length);
        final String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("long_file_", parsed);
    }

    // Tests formatUnsignedOctalString overflow throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalString_overflow_throwsException() {
        final byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(07777L, buffer, 0, buffer.length);
    }

    // Tests formatOctalBytes appends trailing space and NUL
    @Test
    public void testFormatOctalBytes_validInput_formatsCorrectly() {
        final byte[] buffer = new byte[8];
        final int updatedOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, updatedOffset);
        assertEquals(0755L, TarUtils.parseOctal(buffer, 0, buffer.length));
        assertEquals(' ', buffer[buffer.length - 2]);
        assertEquals(0, buffer[buffer.length - 1]);
    }

    // Tests formatLongOctalBytes formats value followed by a trailing space
    @Test
    public void testFormatLongOctalBytes_validInput_formatsCorrectly() {
        final byte[] buffer = new byte[8];
        final int updatedOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(8, updatedOffset);
        assertEquals(' ', buffer[buffer.length - 1]);
        assertEquals(0755L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    // Tests formatCheckSumOctalBytes appends NUL and then space
    @Test
    public void testFormatCheckSumOctalBytes_validInput_formatsCorrectly() {
        final byte[] buffer = new byte[8];
        final int updatedOffset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(8, updatedOffset);
        assertEquals(0, buffer[buffer.length - 2]);
        assertEquals((byte) ' ', buffer[buffer.length - 1]);
    }

    // Tests formatLongOctalOrBinaryBytes fitting in octal representation
    @Test
    public void testFormatLongOctalOrBinaryBytes_fitsInOctal_formatsAsOctal() {
        final byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(100L, buffer, 0, 8);
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(100L, result);
    }

    // Tests formatLongOctalOrBinaryBytes for binary format with length 8 (Defects4J bug regression test)
    @Test
    public void testFormatLongOctalOrBinaryBytes_8BytesBinary_roundTrip() {
        final byte[] buffer = new byte[8];
        final long largeValue = TarConstants.MAXID + 1024L;
        TarUtils.formatLongOctalOrBinaryBytes(largeValue, buffer, 0, 8);
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(largeValue, result);
    }

    // Tests formatLongOctalOrBinaryBytes for binary format with length 12
    @Test
    public void testFormatLongOctalOrBinaryBytes_12BytesBinary_roundTrip() {
        final byte[] buffer = new byte[12];
        final long largeValue = TarConstants.MAXSIZE + 1024L;
        TarUtils.formatLongOctalOrBinaryBytes(largeValue, buffer, 0, 12);
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        assertEquals(largeValue, result);
    }

    // Tests formatLongOctalOrBinaryBytes with negative value
    @Test
    public void testFormatLongOctalOrBinaryBytes_negativeValue_roundTrip() {
        final byte[] buffer = new byte[8];
        final long negativeValue = -12345L;
        TarUtils.formatLongOctalOrBinaryBytes(negativeValue, buffer, 0, 8);
        final long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(negativeValue, result);
    }

    // Tests computeCheckSum and verifyCheckSum with a valid tar header
    @Test
    public void testComputeAndVerifyCheckSum_validHeader_returnsTrue() {
        final byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i % 128);
        }
        // Fill checksum field with spaces to calculate
        for (int i = 0; i < TarConstants.CHKSUMLEN; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = ' ';
        }
        final long checkSum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(checkSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    // Tests verifyCheckSum with invalid checksum returns false
    @Test
    public void testVerifyCheckSum_corruptedHeader_returnsFalse() {
        final byte[] header = new byte[512];
        TarUtils.formatCheckSumOctalBytes(1234L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        header[0] = (byte) 0xFF;
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}