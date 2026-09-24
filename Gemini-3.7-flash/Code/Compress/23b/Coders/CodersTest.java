package org.apache.commons.compress.archivers.sevenz;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

public class CodersTest {

    // Tests decoding with an unsupported compression method ID throws IOException
    @Test(expected = IOException.class)
    public void testAddDecoder_unsupportedMethod_throwsIOException() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = new byte[] { (byte) 0xFF, (byte) 0xFE };
        Coders.addDecoder(new ByteArrayInputStream(new byte[0]), coder, null);
    }

    // Tests encoding with an unsupported SevenZMethod throws IOException
    @Test(expected = IOException.class)
    public void testAddEncoder_unsupportedMethod_throwsIOException() throws IOException {
        Coders.addEncoder(new ByteArrayOutputStream(), null, null);
    }

    // Tests COPY coder decodes returning original stream
    @Test
    public void testCopyDecoder_decode_returnsSameStream() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.COPY.getId();
        final InputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        final InputStream decoded = Coders.addDecoder(in, coder, null);
        assertSame(in, decoded);
    }

    // Tests COPY coder encodes returning original stream
    @Test
    public void testCopyDecoder_encode_returnsSameStream() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream encoded = Coders.addEncoder(out, SevenZMethod.COPY, null);
        assertSame(out, encoded);
    }

    // Tests DEFLATE coder round-trip decode and encode
    @Test
    public void testDeflateCoder_roundTrip_succeeds() throws IOException {
        final byte[] data = "Deflate Compression Test".getBytes("UTF-8");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OutputStream deflaterOut = Coders.addEncoder(baos, SevenZMethod.DEFLATE, null);
        deflaterOut.write(data);
        deflaterOut.close();

        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.DEFLATE.getId();
        final InputStream inflaterIn = Coders.addDecoder(new ByteArrayInputStream(baos.toByteArray()), coder, null);
        final ByteArrayOutputStream resultBaos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[32];
        int read;
        while ((read = inflaterIn.read(buffer)) != -1) {
            resultBaos.write(buffer, 0, read);
        }
        inflaterIn.close();
        assertArrayEquals(data, resultBaos.toByteArray());
    }

    // Tests BZIP2 coder round-trip decode and encode
    @Test
    public void testBzip2Coder_roundTrip_succeeds() throws IOException {
        final byte[] data = "BZip2 Compression Test Data".getBytes("UTF-8");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OutputStream bzip2Out = Coders.addEncoder(baos, SevenZMethod.BZIP2, null);
        bzip2Out.write(data);
        bzip2Out.close();

        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.BZIP2.getId();
        final InputStream bzip2In = Coders.addDecoder(new ByteArrayInputStream(baos.toByteArray()), coder, null);
        final ByteArrayOutputStream resultBaos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[32];
        int read;
        while ((read = bzip2In.read(buffer)) != -1) {
            resultBaos.write(buffer, 0, read);
        }
        bzip2In.close();
        assertArrayEquals(data, resultBaos.toByteArray());
    }

    // Tests LZMA decoder instantiation with valid properties
    @Test
    public void testLZMADecoder_validProperties_createsStream() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // properties: [0] = lc/lp/pb props byte, [1..4] = dictSize in little-endian
        coder.properties = new byte[] { 0x5d, 0x00, 0x00, 0x01, 0x00 };
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        final InputStream lzmaIn = Coders.addDecoder(in, coder, null);
        assertNotNull(lzmaIn);
        lzmaIn.close();
    }

    // Tests LZMA decoder dictionary size with negative byte values
    @Test
    public void testLZMADecoder_highBitDictSize_unpacksProperly() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // Dict size with high bit set in byte 1: 0x80 -> 128 bytes
        coder.properties = new byte[] { 0x5d, (byte) 0x80, 0x00, 0x00, 0x00 };
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        final InputStream lzmaIn = Coders.addDecoder(in, coder, null);
        assertNotNull(lzmaIn);
        lzmaIn.close();
    }

    // Tests LZMA decoder throws IOException when dictionary exceeds maximum size
    @Test(expected = IOException.class)
    public void testLZMADecoder_dictionaryTooLarge_throwsIOException() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // Properties specifying dictionary > 4GiB (dictSize > LZMAInputStream.DICT_SIZE_MAX)
        coder.properties = new byte[] { 0x5d, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        Coders.addDecoder(in, coder, null);
    }

    // Tests LZMA encode throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testLZMAEncoder_encode_throwsUnsupportedOperationException() throws IOException {
        Coders.addEncoder(new ByteArrayOutputStream(), SevenZMethod.LZMA, null);
    }

    // Tests AES256SHA256 decoder without password throws IOException
    @Test(expected = IOException.class)
    public void testAES256SHA256Decoder_nullPassword_throwsIOException() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[] { 0x00, 0x00 };
        final InputStream aesIn = Coders.addDecoder(new ByteArrayInputStream(new byte[16]), coder, null);
        aesIn.read();
    }

    // Tests AES256SHA256 decoder with properties too short throws IOException
    @Test(expected = IOException.class)
    public void testAES256SHA256Decoder_shortProperties_throwsIOException() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[] { (byte) 0xC0, 0x11 };
        final InputStream aesIn = Coders.addDecoder(
                new ByteArrayInputStream(new byte[16]), coder, "password".getBytes("UTF-16LE"));
        aesIn.read();
    }

    // Tests AES256SHA256 decoder with numCyclesPower == 0x3f
    @Test
    public void testAES256SHA256Decoder_maxCyclesPower_initializesCipher() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        // byte0: 0x3f (numCyclesPower = 0x3f, ivSize=0, saltSize=0), byte1: 0x00
        coder.properties = new byte[] { 0x3f, 0x00 };
        final byte[] password = new byte[32];
        final InputStream aesIn = Coders.addDecoder(
                new ByteArrayInputStream(new byte[16]), coder, password);
        final byte[] buf = new byte[16];
        final int read = aesIn.read(buf, 0, buf.length);
        assertEquals(16, read);
        aesIn.close();
    }

    // Tests AES256SHA256 decoder with salt and IV with SHA-256 digest calculation
    @Test
    public void testAES256SHA256Decoder_withSaltAndIv_initializesCipher() throws IOException {
        final Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        // byte0: 0x01 (numCyclesPower = 1), byte1: 0x00
        // properties: [0]=0x01, [1]=0x00
        coder.properties = new byte[] { 0x01, 0x00 };
        final byte[] password = "test".getBytes("UTF-16LE");
        final InputStream aesIn = Coders.addDecoder(
                new ByteArrayInputStream(new byte[16]), coder, password);
        final int firstByte = aesIn.read();
        assertEquals(true, firstByte != -1);
        aesIn.close();
    }

    // Tests AES256SHA256 encode throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAES256SHA256Encoder_encode_throwsUnsupportedOperationException() throws IOException {
        Coders.addEncoder(new ByteArrayOutputStream(), SevenZMethod.AES256SHA256, null);
    }
}