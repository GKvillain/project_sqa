package org.apache.commons.compress.archivers.sevenz;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * JUnit 4 test for Coders (Defects4J bug 23b).
 */
public class CodersTest {

    // helper to create a Coder with given method and properties
    private static Coder createCoder(SevenZMethod method, byte[] properties) {
        Coder coder = new Coder();
        coder.decompressionMethodId = method.getId();
        coder.properties = properties;
        return coder;
    }

    // Tests that COPY decoder returns the same input stream
    @Test
    public void testAddDecoder_copy_returnsSameInputStream() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Coder coder = createCoder(SevenZMethod.COPY, new byte[0]);
        InputStream result = Coders.addDecoder(input, coder, null);
        assertSame("Should be the same object", input, result);
    }

    // Tests that unknown method ID throws IOException
    @Test(expected = IOException.class)
    public void testAddDecoder_unknownMethodId_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = new byte[]{0, 0, 0, 0};
        coder.properties = new byte[0];
        Coders.addDecoder(input, coder, null);
    }

    // Tests that COPY encoder returns the same output stream
    @Test
    public void testAddEncoder_copy_returnsSameOutputStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStream result = Coders.addEncoder(output, SevenZMethod.COPY, null);
        assertSame(output, result);
    }

    // Tests that DEFLATE encoder returns a DeflaterOutputStream
    @Test
    public void testAddEncoder_deflate_returnsDeflaterOutputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream result = Coders.addEncoder(out, SevenZMethod.DEFLATE, null);
        assertNotNull(result);
        assertTrue("Result should be a DeflaterOutputStream", result instanceof DeflaterOutputStream);
    }

    // Tests that BZIP2 encoder returns a BZip2CompressorOutputStream
    @Test
    public void testAddEncoder_bzip2_returnsBZip2CompressorOutputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream result = Coders.addEncoder(out, SevenZMethod.BZIP2, null);
        assertNotNull(result);
        assertTrue("Result should be a BZip2CompressorOutputStream", result instanceof BZip2CompressorOutputStream);
    }

    // Tests that LZMA encoder throws UnsupportedOperationException (encoding not supported)
    @Test(expected = UnsupportedOperationException.class)
    public void testAddEncoder_lzma_throwsUnsupportedOperationException() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        Coders.addEncoder(out, SevenZMethod.LZMA, null);
    }

    // Tests that LZMA2 encoder throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAddEncoder_lzma2_throwsUnsupportedOperationException() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        Coders.addEncoder(out, SevenZMethod.LZMA2, null);
    }

    // Tests that LZMA decoder returns a stream with valid (zero) properties
    @Test
    public void testAddDecoder_lzma_validProperties_returnsNotNull() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = createCoder(SevenZMethod.LZMA, new byte[5]);
        InputStream result = Coders.addDecoder(input, coder, null);
        assertNotNull(result);
    }

    // Tests that LZMA decoder throws when dictionary size is > 4GiB (unsigned).
    // This test exposes the bug: bytes are not masked to 0xFF, so dictSize becomes -1,
    // bypassing the check and no IOException is thrown. The test expects IOException,
    // so it will fail (error) on the buggy code.
    @Test(expected = IOException.class)
    public void testAddDecoder_lzma_dictSizeTooLarge_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // properties[1..4] = 0xFF each -> dictSize = -1 (sign extended), which is not > DICT_SIZE_MAX
        coder.properties = new byte[]{0, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        Coders.addDecoder(input, coder, null);
    }

    // Tests roundtrip encode/decode with DEFLATE
    @Test
    public void testAddEncoderAndDecoder_deflate_roundTrip() throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        OutputStream encoder = Coders.addEncoder(compressed, SevenZMethod.DEFLATE, null);
        byte[] original = "Test data for deflate".getBytes("UTF-8");
        encoder.write(original);
        encoder.close();

        ByteArrayInputStream in = new ByteArrayInputStream(compressed.toByteArray());
        Coder coder = createCoder(SevenZMethod.DEFLATE, new byte[0]);
        InputStream decoder = Coders.addDecoder(in, coder, null);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = decoder.read(buffer)) != -1) {
            result.write(buffer, 0, readBytes);
        }
        decoder.close();
        assertArrayEquals("Roundtripped data should match original", original, result.toByteArray());
    }

    // Tests roundtrip encode/decode with BZIP2
    @Test
    public void testAddEncoderAndDecoder_bzip2_roundTrip() throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        OutputStream encoder = Coders.addEncoder(compressed, SevenZMethod.BZIP2, null);
        byte[] original = "Test data for bzip2".getBytes("UTF-8");
        encoder.write(original);
        encoder.close();

        ByteArrayInputStream in = new ByteArrayInputStream(compressed.toByteArray());
        Coder coder = createCoder(SevenZMethod.BZIP2, new byte[0]);
        InputStream decoder = Coders.addDecoder(in, coder, null);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = decoder.read(buffer)) != -1) {
            result.write(buffer, 0, readBytes);
        }
        decoder.close();
        assertArrayEquals("Roundtripped data should match original", original, result.toByteArray());
    }

    // Tests that AES decoder throws IOException when password is null
    @Test(expected = IOException.class)
    public void testAddDecoder_aes256Sha256_nullPassword_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = createCoder(SevenZMethod.AES256SHA256, new byte[2]);
        Coders.addDecoder(input, coder, null);
    }

    // Tests that AES decoder throws when properties length is only 1 (missing index 1).
    // The buggy code accesses coder.properties[1] before length check, causing
    // ArrayIndexOutOfBoundsException instead of IOException. This test expects IOException,
    // so it will detect the defect.
    @Test(expected = IOException.class)
    public void testAddDecoder_aes256Sha256_shortProperties_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[1]; // too short, code will access index 1 before length check
        Coders.addDecoder(input, coder, new byte[]{1});
    }

    // Tests that AES decoder throws when salt+IV length exceeds properties
    @Test(expected = IOException.class)
    public void testAddDecoder_aes256Sha256_insufficientProperties_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        // properties length = 2, but saltSize=1, ivSize=1 -> 2+1+1=4 > 2
        coder.properties = new byte[]{0, 0x11};
        Coders.addDecoder(input, coder, new byte[]{1});
    }

    // Tests that AES decoder returns a stream with valid properties and non-null password
    @Test
    public void testAddDecoder_aes256Sha256_validProperties_returnsInputStream() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[2]; // saltSize=0, ivSize=0, numCyclesPower=0
        InputStream result = Coders.addDecoder(input, coder, new byte[]{1, 2, 3});
        assertNotNull(result);
    }

    // Tests AES decoder when numCyclesPower == 63 (direct key derivation)
    @Test
    public void testAddDecoder_aes256Sha256_numCyclesPower63_returnsInputStream() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[]{(byte)0x3f, 0}; // numCyclesPower=63
        InputStream result = Coders.addDecoder(input, coder, new byte[]{1, 2, 3});
        assertNotNull(result);
    }

    // Tests AES decoder with a small numCyclesPower (loop executed a few times)
    @Test
    public void testAddDecoder_aes256Sha256_smallNumCyclesPower_returnsInputStream() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[]{0x01, 0}; // numCyclesPower=1
        InputStream result = Coders.addDecoder(input, coder, new byte[]{1, 2, 3});
        assertNotNull(result);
    }

    // === NEW TESTS TO IMPROVE COVERAGE ===

    // Tests LZMA decoder with null properties (should throw IOException)
    @Test(expected = IOException.class)
    public void testAddDecoder_lzma_nullProperties_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        coder.properties = null;
        Coders.addDecoder(input, coder, null);
    }

    // Tests LZMA decoder with properties length < 5 (should throw IOException)
    @Test(expected = IOException.class)
    public void testAddDecoder_lzma_shortProperties_throwsIOException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        coder.properties = new byte[4];
        Coders.addDecoder(input, coder, null);
    }

    // Tests LZMA decoder with valid properties that have LZMA compression level bit set (0x80)
    @Test
    public void testAddDecoder_lzma_withCompressionLevelBit() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // lc=3, lp=0, pb=2, dictSize=1MB (0x00100000)
        coder.properties = new byte[]{(byte)0x5D, 0x00, 0x10, 0x00, 0x00};
        InputStream result = Coders.addDecoder(input, coder, null);
        assertNotNull(result);
    }

    // Tests BZIP2 decoder returns non-null input stream
    @Test
    public void testAddDecoder_bzip2_returnsNotNull() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = createCoder(SevenZMethod.BZIP2, new byte[0]);
        InputStream result = Coders.addDecoder(input, coder, null);
        assertNotNull(result);
    }

    // Tests DEFLATE decoder returns non-null input stream
    @Test
    public void testAddDecoder_deflate_returnsNotNull() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = createCoder(SevenZMethod.DEFLATE, new byte[0]);
        InputStream result = Coders.addDecoder(input, coder, null);
        assertNotNull(result);
    }

    // Tests LZMA2 decoder throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAddDecoder_lzma2_throwsUnsupportedOperationException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = createCoder(SevenZMethod.LZMA2, new byte[0]);
        Coders.addDecoder(input, coder, null);
    }

    // Tests AES decoder with zero-length password array (should still work)
    @Test
    public void testAddDecoder_aes256Sha256_emptyPassword_returnsInputStream() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[2];
        InputStream result = Coders.addDecoder(input, coder, new byte[0]);
        assertNotNull(result);
    }

    // Tests AES decoder with saltSize > 0 and matching properties length
    @Test
    public void testAddDecoder_aes256Sha256_withSalt_returnsInputStream() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        // properties[0] = saltSize=1, ivSize=0, numCyclesPower=3
        coder.properties = new byte[]{0x03, 0x00, 0x01}; // 3 bytes: 1 salt + 1 cycle power byte? Actually format: byte0 = (numCyclesPower << 1) | ivSize; byte1 = saltSize
        // But test just needs saltSize+ivSize <= properties.length -2
        // Let's use format: byte0 = cyclePower, byte1 = ivSize, rest alternately salt and IV
        // Simpler: saltSize=1, ivSize=0, total needed = 2+1=3 <=3
        coder.properties = new byte[]{0x01, 0x00, 0x01}; // cyclePower=0, ivSize=0, saltSize=1 (actual salt byte at index 2)
        InputStream result = Coders.addDecoder(input, coder, new byte[]{1, 2, 3});
        assertNotNull(result);
    }
}