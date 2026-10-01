package org.apache.commons.compress.compressors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.junit.Test;

public class CompressorStreamFactoryTest {

    // Tests null input stream for auto-detect method
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_nullInputStream_throwsIllegalArgumentException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream((InputStream) null);
    }

    // Tests input stream without mark support for auto-detect method
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_noMarkSupport_throwsIllegalArgumentException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        InputStream noMark = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createCompressorInputStream(noMark);
    }

    // Tests auto-detect with GZIP stream
    @Test
    public void testCreateCompressorInputStream_autoDetectGzip_returnsGzipInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos)) {
            gzos.write("test".getBytes("UTF-8"));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (CompressorInputStream cis = factory.createCompressorInputStream(bis)) {
            assertNotNull(cis);
            assertTrue(cis instanceof GzipCompressorInputStream);
        }
    }

    // Tests auto-detect with BZIP2 stream
    @Test
    public void testCreateCompressorInputStream_autoDetectBZip2_returnsBZip2InputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(bos)) {
            bzos.write("test".getBytes("UTF-8"));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (CompressorInputStream cis = factory.createCompressorInputStream(bis)) {
            assertNotNull(cis);
            assertTrue(cis instanceof BZip2CompressorInputStream);
        }
    }

    // Tests auto-detect with unknown stream signature
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_unknownSignature_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] unknown = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04};
        ByteArrayInputStream bis = new ByteArrayInputStream(unknown);
        factory.createCompressorInputStream(bis);
    }

    // Tests auto-detect with empty input stream
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_emptyStream_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        factory.createCompressorInputStream(bis);
    }

    // Tests null name for named factory method
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_nullName_throwsIllegalArgumentException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream((String) null, new ByteArrayInputStream(new byte[0]));
    }

    // Tests null input stream for named factory method
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_nullNamedInputStream_throwsIllegalArgumentException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream(CompressorStreamFactory.GZIP, (InputStream) null);
    }

    // Tests creating GZIP input stream by name
    @Test
    public void testCreateCompressorInputStream_namedGzip_returnsGzipInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos)) {
            gzos.write("test".getBytes("UTF-8"));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (CompressorInputStream cis = factory.createCompressorInputStream(CompressorStreamFactory.GZIP, bis)) {
            assertNotNull(cis);
            assertTrue(cis instanceof GzipCompressorInputStream);
        }
    }

    // Tests creating BZIP2 input stream by name
    @Test
    public void testCreateCompressorInputStream_namedBZip2_returnsBZip2InputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(bos)) {
            bzos.write("test".getBytes("UTF-8"));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (CompressorInputStream cis = factory.createCompressorInputStream(CompressorStreamFactory.BZIP2, bis)) {
            assertNotNull(cis);
            assertTrue(cis instanceof BZip2CompressorInputStream);
        }
    }

    // Tests invalid name for named input stream factory
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_invalidName_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        factory.createCompressorInputStream("invalid", bis);
    }

    // Tests null name for output stream factory
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStream_nullName_throwsIllegalArgumentException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream(null, new ByteArrayOutputStream());
    }

    // Tests null output stream for output stream factory
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStream_nullOutputStream_throwsIllegalArgumentException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, (OutputStream) null);
    }

    // Tests creating GZIP output stream by name
    @Test
    public void testCreateCompressorOutputStream_namedGzip_returnsGzipOutputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, bos)) {
            assertNotNull(cos);
            assertTrue(cos instanceof GzipCompressorOutputStream);
        }
    }

    // Tests creating BZIP2 output stream by name
    @Test
    public void testCreateCompressorOutputStream_namedBZip2_returnsBZip2OutputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.BZIP2, bos)) {
            assertNotNull(cos);
            assertTrue(cos instanceof BZip2CompressorOutputStream);
        }
    }

    // Tests invalid name for output stream factory
    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStream_invalidName_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream("invalid", new ByteArrayOutputStream());
    }

    // Tests constructor with boolean and getDecompressConcatenated
    @Test
    public void testConstructor_withTrue_decompressConcatenatedIsTrue() {
        CompressorStreamFactory factory = new CompressorStreamFactory(true);
        assertTrue(factory.getDecompressConcatenated());
    }

    // Tests constructor with boolean false and getDecompressConcatenated
    @Test
    public void testConstructor_withFalse_decompressConcatenatedIsFalse() {
        CompressorStreamFactory factory = new CompressorStreamFactory(false);
        assertFalse(factory.getDecompressConcatenated());
    }

    // Tests default constructor and getDecompressConcatenated
    @Test
    public void testConstructor_default_decompressConcatenatedIsFalse() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        assertFalse(factory.getDecompressConcatenated());
    }

    // Tests setDecompressConcatenated when constructor with boolean was used
    @Test(expected = IllegalStateException.class)
    public void testSetDecompressConcatenated_afterConstructorWithBoolean_throwsIllegalStateException() {
        CompressorStreamFactory factory = new CompressorStreamFactory(true);
        factory.setDecompressConcatenated(false);
    }

    // Tests creating XZ input stream by name
    @Test
    public void testCreateCompressorInputStream_namedXz_returnsXZInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XZCompressorOutputStream xzos = new XZCompressorOutputStream(bos)) {
            xzos.write("test".getBytes("UTF-8"));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (CompressorInputStream cis = factory.createCompressorInputStream(CompressorStreamFactory.XZ, bis)) {
            assertNotNull(cis);
            assertTrue(cis instanceof XZCompressorInputStream);
        }
    }

    // Tests creating XZ output stream by name
    @Test
    public void testCreateCompressorOutputStream_namedXz_returnsXZOutputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.XZ, bos)) {
            assertNotNull(cos);
            assertTrue(cos instanceof XZCompressorOutputStream);
        }
    }

    // ===== NEW TEST CASES FOR MISSING COVERAGE =====

    // Tests createCompressorInputStream with GzipParameters
    @Test
    public void testCreateCompressorInputStream_withGzipParameters_returnsGzipInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GzipParameters params = new GzipParameters();
        params.setCompressionLevel(5);
        try (GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos, params)) {
            gzos.write("test".getBytes("UTF-8"));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (CompressorInputStream cis = factory.createCompressorInputStream(CompressorStreamFactory.GZIP, bis)) {
            assertNotNull(cis);
            assertTrue(cis instanceof GzipCompressorInputStream);
        }
    }

    // Tests createCompressorOutputStream with GzipParameters
    @Test
    public void testCreateCompressorOutputStream_withGzipParameters_returnsGzipOutputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GzipParameters params = new GzipParameters();
        params.setFilename("test.gz");
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, bos, params)) {
            assertNotNull(cos);
            assertTrue(cos instanceof GzipCompressorOutputStream);
        }
    }

    // Tests auto-detect with stream that has exactly only the signature bytes (no actual data)
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_signatureOnlyStream_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        // BZip2 signature is 2 bytes: 0x42, 0x5A
        byte[] sigOnly = new byte[] {0x42, 0x5A};
        ByteArrayInputStream bis = new ByteArrayInputStream(sigOnly);
        factory.createCompressorInputStream(bis);
    }

    // Tests setDecompressConcatenated before any stream creation (should succeed)
    @Test
    public void testSetDecompressConcatenated_beforeCreation_succeeds() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.setDecompressConcatenated(true);
        assertTrue(factory.getDecompressConcatenated());
    }

    // Tests setDecompressConcatenated after creating a stream (should throw IllegalStateException)
    @Test(expected = IllegalStateException.class)
    public void testSetDecompressConcatenated_afterStreamCreation_throwsIllegalStateException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, bos)) {
            // stream created, now try to set
            factory.setDecompressConcatenated(true);
        }
    }

    // Tests auto-detect with very short stream (1 byte only) - should fail
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_veryShortStream_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] {0x1F});
        factory.createCompressorInputStream(bis);
    }

    // Tests createCompressorInputStream with lowercase name (case sensitivity)
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_caseSensitiveName_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        factory.createCompressorInputStream("gzip", bis);
    }

    // Tests createCompressorOutputStream with lowercase name (case sensitivity)
    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStream_caseSensitiveName_throwsCompressorException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream("bzip2", new ByteArrayOutputStream());
    }
}