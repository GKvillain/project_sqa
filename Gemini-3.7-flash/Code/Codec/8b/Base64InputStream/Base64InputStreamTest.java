package org.apache.commons.codec.binary;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Base64InputStreamTest {

    // Tests markSupported returns false
    @Test
    public void testMarkSupported_default_returnsFalse() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64In = new Base64InputStream(in);
        assertFalse(b64In.markSupported());
    }

    // Tests read with null byte array throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRead_nullBuffer_throwsNullPointerException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(null, 0, 1);
    }

    // Tests read with negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], -1, 1);
    }

    // Tests read with negative length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], 0, -1);
    }

    // Tests read with offset greater than buffer length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetGreaterThanBufferLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], 11, 1);
    }

    // Tests read with offset plus length greater than buffer length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLengthGreaterThanBufferLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], 6, 5);
    }

    // Tests read with length zero returns zero
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64In = new Base64InputStream(in);
        int result = b64In.read(new byte[10], 0, 0);
        assertEquals(0, result);
    }

    // Tests single byte read at EOF returns -1
    @Test
    public void testRead_emptyStream_returnsMinusOne() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64In = new Base64InputStream(in);
        assertEquals(-1, b64In.read());
    }

    // Tests single byte read decoding properly and handling unsigned byte conversion
    @Test
    public void testRead_singleByteDecode_returnsCorrectBytes() throws IOException {
        // "gA==" decodes to single byte 0x80 (128 unsigned)
        byte[] encoded = StringUtils.getBytesUtf8("gA==");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(encoded), false);
        int b = b64In.read();
        assertEquals(128, b);
        assertEquals(-1, b64In.read());
    }

    // Tests single byte read encoding
    @Test
    public void testRead_singleByteEncode_returnsCorrectBytes() throws IOException {
        byte[] raw = StringUtils.getBytesUtf8("f");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(raw), true, 0, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = b64In.read()) != -1) {
            out.write(b);
        }
        assertEquals("Zg==", StringUtils.newStringUtf8(out.toByteArray()));
    }

    // Tests decoding stream into byte array
    @Test
    public void testRead_decodeByteArray_returnsDecodedData() throws IOException {
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(encoded));
        byte[] result = new byte[11];
        int totalRead = 0;
        int read;
        while ((read = b64In.read(result, totalRead, result.length - totalRead)) != -1) {
            totalRead += read;
            if (totalRead == result.length) {
                break;
            }
        }
        assertEquals(11, totalRead);
        assertEquals("Hello World", StringUtils.newStringUtf8(result));
    }

    // Tests encoding stream into byte array
    @Test
    public void testRead_encodeByteArray_returnsEncodedData() throws IOException {
        byte[] raw = StringUtils.getBytesUtf8("Hello World");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(raw), true, 0, null);
        byte[] result = new byte[16];
        int read = b64In.read(result, 0, result.length);
        assertEquals(16, read);
        assertEquals("SGVsbG8gV29ybGQ=", StringUtils.newStringUtf8(result));
    }

    // Tests encoding into a buffer slice with offset and length less than buffer size (Tests CODEC-8 defect)
    @Test
    public void testRead_encodeByteArrayWithOffset_returnsEncodedData() throws IOException {
        byte[] raw = StringUtils.getBytesUtf8("Hello World");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(raw), true, 0, null);
        byte[] buffer = new byte[30];
        int offset = 5;
        int len = 16;
        int read = b64In.read(buffer, offset, len);
        assertEquals(16, read);
        byte[] actual = Arrays.copyOfRange(buffer, offset, offset + read);
        assertEquals("SGVsbG8gV29ybGQ=", StringUtils.newStringUtf8(actual));
    }

    // Tests decoding into a buffer slice with offset and length less than buffer size (Tests CODEC-8 defect)
    @Test
    public void testRead_decodeByteArrayWithOffset_returnsDecodedData() throws IOException {
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(encoded), false);
        byte[] buffer = new byte[30];
        int offset = 5;
        int len = 11;
        int read = b64In.read(buffer, offset, len);
        assertEquals(11, read);
        byte[] actual = Arrays.copyOfRange(buffer, offset, offset + read);
        assertEquals("Hello World", StringUtils.newStringUtf8(actual));
    }

    // Tests encoding with custom line length and line separator
    @Test
    public void testRead_encodeCustomLineLengthAndSeparator_returnsFormattedData() throws IOException {
        byte[] raw = StringUtils.getBytesUtf8("12345678");
        byte[] separator = new byte[]{'\n'};
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(raw), true, 4, separator);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4];
        int read;
        while ((read = b64In.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, read);
        }
        assertEquals("MTIz\nNDU2\nNzg=\n", StringUtils.newStringUtf8(out.toByteArray()));
    }

    // Tests read(byte[]) array overload at EOF returns -1
    @Test
    public void testRead_emptyStreamArray_returnsMinusOne() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64In = new Base64InputStream(in);
        byte[] buf = new byte[10];
        assertEquals(-1, b64In.read(buf, 0, buf.length));
    }

    // Tests 2-argument constructor with doEncode=true default line parameters
    @Test
    public void testConstructor_twoArgsEncodeTrue() throws IOException {
        byte[] raw = StringUtils.getBytesUtf8("Hello World");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(raw), true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int read;
        while ((read = b64In.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, read);
        }
        assertEquals("SGVsbG8gV29ybGQ=\r\n", StringUtils.newStringUtf8(out.toByteArray()));
    }

    // Tests reading repeatedly after EOF consistently returns -1
    @Test
    public void testRead_afterEof_returnsMinusOne() throws IOException {
        byte[] encoded = StringUtils.getBytesUtf8("Zg==");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(encoded), false);
        assertEquals('f', b64In.read());
        assertEquals(-1, b64In.read());
        assertEquals(-1, b64In.read());

        byte[] buf = new byte[5];
        assertEquals(-1, b64In.read(buf, 0, buf.length));
    }

    // Tests decoding input containing whitespace and CRLF separators
    @Test
    public void testRead_decodeWithWhitespaceAndLineSeparators() throws IOException {
        byte[] encoded = StringUtils.getBytesUtf8("SGVs\r\nbG8g\n V29y\t bGQ=");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(encoded), false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4];
        int read;
        while ((read = b64In.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, read);
        }
        assertEquals("Hello World", StringUtils.newStringUtf8(out.toByteArray()));
    }

    // Tests large round-trip encode and decode streaming
    @Test
    public void testRead_largeDataRoundTrip() throws IOException {
        byte[] original = new byte[8192];
        new Random(42).nextBytes(original);

        Base64InputStream encoderIn = new Base64InputStream(new ByteArrayInputStream(original), true, 76, new byte[]{'\r', '\n'});
        ByteArrayOutputStream encodedOut = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int read;
        while ((read = encoderIn.read(buf, 0, buf.length)) != -1) {
            encodedOut.write(buf, 0, read);
        }

        Base64InputStream decoderIn = new Base64InputStream(new ByteArrayInputStream(encodedOut.toByteArray()), false);
        ByteArrayOutputStream decodedOut = new ByteArrayOutputStream();
        while ((read = decoderIn.read(buf, 0, buf.length)) != -1) {
            decodedOut.write(buf, 0, read);
        }

        assertArrayEquals(original, decodedOut.toByteArray());
    }

    // Tests read(byte[]) standard InputStream overload
    @Test
    public void testRead_byteArrayOverload() throws IOException {
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8=");
        Base64InputStream b64In = new Base64InputStream(new ByteArrayInputStream(encoded));
        byte[] buf = new byte[5];
        int read = b64In.read(buf);
        assertEquals(5, read);
        assertEquals("Hello", StringUtils.newStringUtf8(buf));
    }
}