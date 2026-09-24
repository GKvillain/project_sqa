package org.apache.commons.compress.compressors.deflate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeflateCompressorInputStreamTest {

    private byte[] compress(byte[] data, boolean withHeader) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, !withHeader);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        } finally {
            deflater.end();
        }
        return baos.toByteArray();
    }

    // Tests default constructor and single byte read with valid zlib header
    @Test
    public void testRead_singleByteWithDefaultConstructor_returnsCorrectData() throws IOException {
        byte[] original = "Hello World".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            assertArrayEquals(original, out.toByteArray());
            assertEquals(original.length, in.getBytesRead());
        }
    }

    // Tests buffer read with offset and length
    @Test
    public void testRead_bufferWithDefaultConstructor_returnsCorrectData() throws IOException {
        byte[] original = "Deflate compressor input stream test data with some length".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buffer = new byte[16];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            assertArrayEquals(original, out.toByteArray());
            assertEquals(original.length, in.getBytesRead());
        }
    }

    // Tests decompression with raw deflate stream without zlib header
    @Test
    public void testRead_rawDeflateWithoutZlibHeader_decompressesSuccessfully() throws IOException {
        byte[] original = "Raw Deflate Data Without Zlib Header".getBytes("UTF-8");
        byte[] compressed = compress(original, false);

        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(false);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed), params)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[32];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            assertArrayEquals(original, out.toByteArray());
            assertEquals(original.length, in.getBytesRead());
        }
    }

    // Tests decompression with explicit zlib header in DeflateParameters
    @Test
    public void testRead_explicitWithZlibHeader_decompressesSuccessfully() throws IOException {
        byte[] original = "Explicit Zlib Header Parameters".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed), params)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            assertArrayEquals(original, out.toByteArray());
            assertEquals(original.length, in.getBytesRead());
        }
    }

    // Tests skip functionality
    @Test
    public void testSkip_validBytes_skipsExpectedAmount() throws IOException {
        byte[] original = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            long skipped = in.skip(10);
            assertEquals(10L, skipped);

            byte[] buffer = new byte[6];
            int bytesRead = in.read(buffer, 0, buffer.length);
            assertEquals(6, bytesRead);
            assertEquals("ABCDEF", new String(buffer, 0, bytesRead, "UTF-8"));
        }
    }

    // Tests available method
    @Test
    public void testAvailable_normalStream_returnsAvailableValue() throws IOException {
        byte[] original = "Test available() method".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            int available = in.available();
            assertTrue(available >= 0);
            int b = in.read();
            assertTrue(b != -1);
        }
    }

    // Tests empty compressed input
    @Test
    public void testRead_emptyInput_returnsEofImmediately() throws IOException {
        byte[] original = new byte[0];
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            int b = in.read();
            assertEquals(-1, b);
            assertEquals(0L, in.getBytesRead());
        }
    }

    // Tests reading after EOF
    @Test
    public void testRead_afterEof_returnsMinusOne() throws IOException {
        byte[] original = new byte[] { 42 };
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            assertEquals(42, in.read());
            assertEquals(-1, in.read());
            assertEquals(-1, in.read());
            byte[] buf = new byte[10];
            assertEquals(-1, in.read(buf, 0, buf.length));
        }
    }

    // Tests closing stream and subsequent operations throwing exception
    @Test(expected = IOException.class)
    public void testClose_closedStream_throwsIOExceptionOnRead() throws IOException {
        byte[] original = "Data to close".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        in.close();
        in.read();
    }

    // Tests corrupted / invalid data stream throwing IOException
    @Test(expected = IOException.class)
    public void testRead_corruptedData_throwsIOException() throws IOException {
        byte[] corruptedData = new byte[] { 0x12, 0x34, 0x56, 0x78 };
        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(corruptedData))) {
            in.read();
        }
    }

    // Tests static matches method for magic bytes
    @Test
    public void testMatches() {
        byte[] magic01 = new byte[] { 0x78, 0x01, 0x00, 0x00 };
        byte[] magic5e = new byte[] { 0x78, 0x5e, 0x00, 0x00 };
        byte[] magic9c = new byte[] { 0x78, (byte) 0x9c, 0x00, 0x00 };
        byte[] magicda = new byte[] { 0x78, (byte) 0xda, 0x00, 0x00 };

        assertTrue(DeflateCompressorInputStream.matches(magic01, 4));
        assertTrue(DeflateCompressorInputStream.matches(magic5e, 4));
        assertTrue(DeflateCompressorInputStream.matches(magic9c, 4));
        assertTrue(DeflateCompressorInputStream.matches(magicda, 4));

        assertFalse(DeflateCompressorInputStream.matches(magic01, 3));
        assertFalse(DeflateCompressorInputStream.matches(new byte[] { 0x77, 0x01, 0x00, 0x00 }, 4));
        assertFalse(DeflateCompressorInputStream.matches(new byte[] { 0x78, 0x00, 0x00, 0x00 }, 4));
    }

    // Tests DeflateParameters default settings and getter
    @Test
    public void testDeflateParameters_defaultAndGetter() {
        DeflateParameters params = new DeflateParameters();
        assertTrue(params.withZlibHeader());
        params.setWithZlibHeader(false);
        assertFalse(params.withZlibHeader());
    }

    // Tests skip beyond EOF
    @Test
    public void testSkip_moreThanAvailable_skipsRemaining() throws IOException {
        byte[] original = "Short".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            long skipped = in.skip(100);
            assertEquals(5L, skipped);
            assertEquals(-1, in.read());
        }
    }

    // Tests read 0 bytes into buffer
    @Test
    public void testRead_zeroBytes_returnsZero() throws IOException {
        byte[] original = "Test".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buf = new byte[10];
            int read = in.read(buf, 0, 0);
            assertEquals(0, read);
        }
    }
}