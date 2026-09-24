package org.apache.commons.compress.compressors;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompressorStreamFactoryTest {

    // Tests default constructor initialization
    @Test
    public void testConstructor_default_setsDecompressConcatenatedToFalse() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        assertFalse(factory.getDecompressConcatenated());
    }

    // Tests constructor with boolean parameter
    @Test
    public void testConstructor_booleanParam_setsDecompressConcatenatedCorrectly() {
        CompressorStreamFactory factoryTrue = new CompressorStreamFactory(true);
        assertTrue(factoryTrue.getDecompressConcatenated());

        CompressorStreamFactory factoryFalse = new CompressorStreamFactory(false);
        assertFalse(factoryFalse.getDecompressConcatenated());
    }

    // Tests setDecompressConcatenated allows updating when default constructor is used
    @Test
    public void testSetDecompressConcatenated_afterDefaultConstructor_updatesValue() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.setDecompressConcatenated(true);
        assertTrue(factory.getDecompressConcatenated());
        factory.setDecompressConcatenated(false);
        assertFalse(factory.getDecompressConcatenated());
    }

    // Tests setDecompressConcatenated throws IllegalStateException when boolean constructor is used
    @Test(expected = IllegalStateException.class)
    public void testSetDecompressConcatenated_afterBooleanConstructor_throwsException() {
        CompressorStreamFactory factory = new CompressorStreamFactory(true);
        factory.setDecompressConcatenated(false);
    }

    // Tests createCompressorInputStream with null stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_nullStream_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream((InputStream) null);
    }

    // Tests createCompressorInputStream with unmarkable stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_unmarkableStream_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        InputStream unmarkable = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createCompressorInputStream(unmarkable);
    }

    // Tests createCompressorInputStream with unknown signature throws CompressorException
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_unknownSignature_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        factory.createCompressorInputStream(in);
    }

    // Tests createCompressorInputStream autodetecting BZIP2 format
    @Test
    public void testCreateCompressorInputStream_autodetectBzip2_returnsBZip2InputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] bzip2Header = new byte[]{'B', 'Z', 'h', '9', '1', 'A', 'Y', '&', 'S', 'Y'};
        InputStream in = new ByteArrayInputStream(bzip2Header);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue(cis instanceof BZip2CompressorInputStream);
        cis.close();
    }

    // Tests createCompressorInputStream autodetecting GZIP format
    @Test
    public void testCreateCompressorInputStream_autodetectGzip_returnsGzipInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] gzipHeader = new byte[]{(byte) 0x1f, (byte) 0x8b, 0x08, 0x00, 0, 0, 0, 0, 0, 0};
        InputStream in = new ByteArrayInputStream(gzipHeader);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue(cis instanceof GzipCompressorInputStream);
        cis.close();
    }

    // Tests createCompressorInputStream autodetecting Framed Snappy format
    @Test
    public void testCreateCompressorInputStream_autodetectFramedSnappy_returnsFramedSnappyInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] snappyHeader = new byte[]{(byte) 0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59};
        InputStream in = new ByteArrayInputStream(snappyHeader);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue(cis instanceof FramedSnappyCompressorInputStream);
        cis.close();
    }

    // Tests createCompressorInputStream by name with null name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_nullName_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createCompressorInputStream by name with null stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStream_nullNamedStream_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream(CompressorStreamFactory.GZIP, null);
    }

    // Tests createCompressorInputStream with unsupported compressor name throws CompressorException
    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStream_unknownName_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream("unknown-format", new ByteArrayInputStream(new byte[0]));
    }

    // Tests createCompressorInputStream creating Snappy Raw stream
    @Test
    public void testCreateCompressorInputStream_snappyRaw_returnsSnappyCompressorInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        CompressorInputStream cis = factory.createCompressorInputStream(
                CompressorStreamFactory.SNAPPY_RAW, new ByteArrayInputStream(new byte[]{0}));
        assertNotNull(cis);
        assertTrue(cis instanceof SnappyCompressorInputStream);
        cis.close();
    }

    // Tests createCompressorInputStream creating Deflate stream
    @Test
    public void testCreateCompressorInputStream_deflate_returnsDeflateCompressorInputStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        CompressorInputStream cis = factory.createCompressorInputStream(
                CompressorStreamFactory.DEFLATE, new ByteArrayInputStream(new byte[0]));
        assertNotNull(cis);
        assertTrue(cis instanceof DeflateCompressorInputStream);
        cis.close();
    }

    // Tests createCompressorOutputStream with null name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStream_nullName_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream(null, new ByteArrayOutputStream());
    }

    // Tests createCompressorOutputStream with null stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStream_nullStream_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream(CompressorStreamFactory.BZIP2, null);
    }

    // Tests createCompressorOutputStream with unsupported compressor name throws CompressorException
    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStream_unknownName_throwsException() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream("unknown-compressor", new ByteArrayOutputStream());
    }

    // Tests createCompressorOutputStream for supported formats
    @Test
    public void testCreateCompressorOutputStream_supportedFormats_returnsCorrectStreams() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();

        CompressorOutputStream bzipOut = factory.createCompressorOutputStream(
                CompressorStreamFactory.BZIP2, new ByteArrayOutputStream());
        assertNotNull(bzipOut);
        assertTrue(bzipOut instanceof BZip2CompressorOutputStream);
        bzipOut.close();

        CompressorOutputStream gzipOut = factory.createCompressorOutputStream(
                CompressorStreamFactory.GZIP, new ByteArrayOutputStream());
        assertNotNull(gzipOut);
        assertTrue(gzipOut instanceof GzipCompressorOutputStream);
        gzipOut.close();

        CompressorOutputStream pack200Out = factory.createCompressorOutputStream(
                CompressorStreamFactory.PACK200, new ByteArrayOutputStream());
        assertNotNull(pack200Out);
        assertTrue(pack200Out instanceof Pack200CompressorOutputStream);
        pack200Out.close();

        CompressorOutputStream deflateOut = factory.createCompressorOutputStream(
                CompressorStreamFactory.DEFLATE, new ByteArrayOutputStream());
        assertNotNull(deflateOut);
        assertTrue(deflateOut instanceof DeflateCompressorOutputStream);
        deflateOut.close();
    }
}