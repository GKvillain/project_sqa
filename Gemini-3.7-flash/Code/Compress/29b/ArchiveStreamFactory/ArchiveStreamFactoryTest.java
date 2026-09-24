package org.apache.commons.compress.archivers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArchiveStreamFactoryTest {

    // Tests default constructor initializes entryEncoding to null
    @Test
    public void testConstructor_default_entryEncodingIsNull() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        assertNull(factory.getEntryEncoding());
    }

    // Tests constructor with specific encoding sets entryEncoding correctly
    @Test
    public void testConstructor_withEncoding_setsEntryEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        assertEquals("UTF-8", factory.getEntryEncoding());
    }

    // Tests setEntryEncoding on instance created with default constructor
    @Test
    public void testSetEntryEncoding_defaultConstructor_updatesEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.setEntryEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", factory.getEntryEncoding());
    }

    // Tests setEntryEncoding throws IllegalStateException when encoding was set in constructor
    @Test(expected = IllegalStateException.class)
    public void testSetEntryEncoding_encodingSetByConstructor_throwsIllegalStateException() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        factory.setEntryEncoding("ISO-8859-1");
    }

    // Tests createArchiveInputStream with null archiver name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullArchiverName_throwsIllegalArgumentException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveInputStream with null input stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullInputStream_throwsIllegalArgumentException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    // Tests createArchiveInputStream creates correct streams for supported archiver types with encoding
    @Test
    public void testCreateArchiveInputStream_supportedArchiversWithEncoding_returnsCorrectStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");

        ArchiveInputStream ar = factory.createArchiveInputStream(ArchiveStreamFactory.AR, new ByteArrayInputStream(new byte[0]));
        assertTrue(ar instanceof ArArchiveInputStream);

        ArchiveInputStream zip = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, new ByteArrayInputStream(new byte[0]));
        assertTrue(zip instanceof ZipArchiveInputStream);

        ArchiveInputStream tar = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, new ByteArrayInputStream(new byte[0]));
        assertTrue(tar instanceof TarArchiveInputStream);

        ArchiveInputStream jar = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, new ByteArrayInputStream(new byte[0]));
        assertTrue(jar instanceof JarArchiveInputStream);

        ArchiveInputStream cpio = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, new ByteArrayInputStream(new byte[0]));
        assertTrue(cpio instanceof CpioArchiveInputStream);
    }

    // Tests createArchiveInputStream creates correct streams for supported archivers without encoding
    @Test
    public void testCreateArchiveInputStream_supportedArchiversWithoutEncoding_returnsCorrectStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();

        ArchiveInputStream ar = factory.createArchiveInputStream(ArchiveStreamFactory.AR, new ByteArrayInputStream(new byte[0]));
        assertTrue(ar instanceof ArArchiveInputStream);

        ArchiveInputStream zip = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, new ByteArrayInputStream(new byte[0]));
        assertTrue(zip instanceof ZipArchiveInputStream);

        ArchiveInputStream tar = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, new ByteArrayInputStream(new byte[0]));
        assertTrue(tar instanceof TarArchiveInputStream);

        ArchiveInputStream jar = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, new ByteArrayInputStream(new byte[0]));
        assertTrue(jar instanceof JarArchiveInputStream);

        ArchiveInputStream cpio = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, new ByteArrayInputStream(new byte[0]));
        assertTrue(cpio instanceof CpioArchiveInputStream);
    }

    // Tests createArchiveInputStream with SEVEN_Z archiver name throws StreamingNotSupportedException
    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveInputStream_sevenZ_throwsStreamingNotSupportedException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(ArchiveStreamFactory.SEVEN_Z, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveInputStream with unknown archiver name throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_unknownArchiver_throwsArchiveException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream("unknown_format", new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveOutputStream with null archiver name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullArchiverName_throwsIllegalArgumentException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with null output stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullOutputStream_throwsIllegalArgumentException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    // Tests createArchiveOutputStream creates correct streams for supported archivers with encoding
    @Test
    public void testCreateArchiveOutputStream_supportedArchiversWithEncoding_returnsCorrectStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");

        ArchiveOutputStream ar = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, new ByteArrayOutputStream());
        assertTrue(ar instanceof ArArchiveOutputStream);

        ArchiveOutputStream zip = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, new ByteArrayOutputStream());
        assertTrue(zip instanceof ZipArchiveOutputStream);
        assertEquals("UTF-8", ((ZipArchiveOutputStream) zip).getEncoding());

        ArchiveOutputStream tar = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, new ByteArrayOutputStream());
        assertTrue(tar instanceof TarArchiveOutputStream);

        ArchiveOutputStream jar = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, new ByteArrayOutputStream());
        assertTrue(jar instanceof JarArchiveOutputStream);

        ArchiveOutputStream cpio = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, new ByteArrayOutputStream());
        assertTrue(cpio instanceof CpioArchiveOutputStream);
    }

    // Tests createArchiveOutputStream creates correct streams without encoding
    @Test
    public void testCreateArchiveOutputStream_supportedArchiversWithoutEncoding_returnsCorrectStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();

        ArchiveOutputStream zip = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, new ByteArrayOutputStream());
        assertTrue(zip instanceof ZipArchiveOutputStream);

        ArchiveOutputStream tar = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, new ByteArrayOutputStream());
        assertTrue(tar instanceof TarArchiveOutputStream);

        ArchiveOutputStream cpio = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, new ByteArrayOutputStream());
        assertTrue(cpio instanceof CpioArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with SEVEN_Z archiver name throws StreamingNotSupportedException
    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveOutputStream_sevenZ_throwsStreamingNotSupportedException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(ArchiveStreamFactory.SEVEN_Z, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with unsupported output archivers throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_arj_throwsArchiveException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(ArchiveStreamFactory.ARJ, new ByteArrayOutputStream());
    }

    // Tests autodetection with null stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamAutoDetect_nullStream_throwsIllegalArgumentException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream((InputStream) null);
    }

    // Tests autodetection with stream that does not support mark throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamAutoDetect_unmarkableStream_throwsIllegalArgumentException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream unmarkableStream = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createArchiveInputStream(unmarkableStream);
    }

    // Tests autodetection on unrecognized signature throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamAutoDetect_unknownSignature_throwsArchiveException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] emptyData = new byte[512];
        factory.createArchiveInputStream(new ByteArrayInputStream(emptyData));
    }

    // Tests autodetection for Zip format with and without encoding
    @Test
    public void testCreateArchiveInputStreamAutoDetect_zipSignature_returnsZipStream() throws Exception {
        byte[] zipHeader = new byte[] { 0x50, 0x4b, 0x03, 0x04, 0, 0, 0, 0, 0, 0, 0, 0 };
        ArchiveStreamFactory factoryDefault = new ArchiveStreamFactory();
        ArchiveInputStream in1 = factoryDefault.createArchiveInputStream(new ByteArrayInputStream(zipHeader));
        assertTrue(in1 instanceof ZipArchiveInputStream);

        ArchiveStreamFactory factoryEncoded = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream in2 = factoryEncoded.createArchiveInputStream(new ByteArrayInputStream(zipHeader));
        assertTrue(in2 instanceof ZipArchiveInputStream);
    }

    // Tests autodetection for Ar format
    @Test
    public void testCreateArchiveInputStreamAutoDetect_arSignature_returnsArStream() throws Exception {
        byte[] arHeader = new byte[] { '!', '<', 'a', 'r', 'c', 'h', '>', '\n', 0, 0, 0, 0 };
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(arHeader));
        assertTrue(in instanceof ArArchiveInputStream);
    }

    // Tests autodetection for 7z format throws StreamingNotSupportedException
    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveInputStreamAutoDetect_sevenZSignature_throwsStreamingNotSupportedException() throws Exception {
        byte[] sevenZHeader = new byte[] { '7', 'z', (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C, 0, 0, 0, 0, 0, 0 };
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(new ByteArrayInputStream(sevenZHeader));
    }

    // Tests createArchiveInputStream for ARJ format with and without encoding
    @Test
    public void testCreateArchiveInputStream_arj_returnsArjStream() throws Exception {
        byte[] arjData = new byte[] { 0x60, (byte) 0xea, 0, 0 };

        ArchiveStreamFactory factoryDefault = new ArchiveStreamFactory();
        ArchiveInputStream in1 = factoryDefault.createArchiveInputStream(ArchiveStreamFactory.ARJ, new ByteArrayInputStream(arjData));
        assertTrue(in1 instanceof ArjArchiveInputStream);

        ArchiveStreamFactory factoryEncoded = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream in2 = factoryEncoded.createArchiveInputStream(ArchiveStreamFactory.ARJ, new ByteArrayInputStream(arjData));
        assertTrue(in2 instanceof ArjArchiveInputStream);
    }

    // Tests createArchiveInputStream for DUMP format with and without encoding
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_dumpWithoutEncoding_attemptsDumpStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_dumpWithEncoding_attemptsDumpStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveOutputStream for AR and JAR without encoding
    @Test
    public void testCreateArchiveOutputStream_arAndJarWithoutEncoding_returnsCorrectStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();

        ArchiveOutputStream ar = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, new ByteArrayOutputStream());
        assertTrue(ar instanceof ArArchiveOutputStream);

        ArchiveOutputStream jar = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, new ByteArrayOutputStream());
        assertTrue(jar instanceof JarArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with DUMP archiver throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_dump_throwsArchiveException() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
    }

    // Tests autodetection for CPIO format with and without encoding
    @Test
    public void testCreateArchiveInputStreamAutoDetect_cpioSignature_returnsCpioStream() throws Exception {
        byte[] cpioHeader = new byte[] { 0x71, (byte) 0xc7, 0, 0, 0, 0 };

        ArchiveStreamFactory factoryDefault = new ArchiveStreamFactory();
        ArchiveInputStream in1 = factoryDefault.createArchiveInputStream(new ByteArrayInputStream(cpioHeader));
        assertTrue(in1 instanceof CpioArchiveInputStream);

        ArchiveStreamFactory factoryEncoded = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream in2 = factoryEncoded.createArchiveInputStream(new ByteArrayInputStream(cpioHeader));
        assertTrue(in2 instanceof CpioArchiveInputStream);
    }

    // Tests autodetection for ARJ format with and without encoding
    @Test
    public void testCreateArchiveInputStreamAutoDetect_arjSignature_returnsArjStream() throws Exception {
        byte[] arjHeader = new byte[] { 0x60, (byte) 0xea, 0, 0 };

        ArchiveStreamFactory factoryDefault = new ArchiveStreamFactory();
        ArchiveInputStream in1 = factoryDefault.createArchiveInputStream(new ByteArrayInputStream(arjHeader));
        assertTrue(in1 instanceof ArjArchiveInputStream);

        ArchiveStreamFactory factoryEncoded = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream in2 = factoryEncoded.createArchiveInputStream(new ByteArrayInputStream(arjHeader));
        assertTrue(in2 instanceof ArjArchiveInputStream);
    }

    // Tests autodetection for TAR format with and without encoding
    @Test
    public void testCreateArchiveInputStreamAutoDetect_tarSignature_returnsTarStream() throws Exception {
        byte[] tarHeader = new byte[512];
        tarHeader[257] = 'u';
        tarHeader[258] = 's';
        tarHeader[259] = 't';
        tarHeader[260] = 'a';
        tarHeader[261] = 'r';
        tarHeader[262] = 0;

        ArchiveStreamFactory factoryDefault = new ArchiveStreamFactory();
        ArchiveInputStream in1 = factoryDefault.createArchiveInputStream(new ByteArrayInputStream(tarHeader));
        assertTrue(in1 instanceof TarArchiveInputStream);

        ArchiveStreamFactory factoryEncoded = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream in2 = factoryEncoded.createArchiveInputStream(new ByteArrayInputStream(tarHeader));
        assertTrue(in2 instanceof TarArchiveInputStream);
    }

    // Tests autodetection for DUMP format matches signature and invokes Dump stream creation
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamAutoDetect_dumpSignature_invokesDumpCreation() throws Exception {
        byte[] dumpHeader = new byte[32];
        dumpHeader[24] = 0x54;
        dumpHeader[25] = 0x19;
        dumpHeader[26] = 0x01;
        dumpHeader[27] = 0x00;

        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(new ByteArrayInputStream(dumpHeader));
    }
}