package org.apache.commons.compress.archivers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    // Tests createArchiveInputStream with null archiver name - expects IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullArchiverName_throwsIllegalArgumentException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveInputStream with null input stream - expects IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullInputStream_throwsIllegalArgumentException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream("zip", null);
    }

    // Tests createArchiveInputStream with valid ZIP archiver name and input stream
    @Test
    public void testCreateArchiveInputStream_zip_returnsZipArchiveInputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream result = factory.createArchiveInputStream("zip", in);
        assertNotNull(result);
        assertTrue(result instanceof ZipArchiveInputStream);
    }

    // Tests createArchiveInputStream with valid TAR archiver name and input stream
    @Test
    public void testCreateArchiveInputStream_tar_returnsTarArchiveInputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream result = factory.createArchiveInputStream("tar", in);
        assertNotNull(result);
        assertTrue(result instanceof TarArchiveInputStream);
    }

    // Tests createArchiveInputStream with valid JAR archiver name and input stream
    @Test
    public void testCreateArchiveInputStream_jar_returnsJarArchiveInputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream result = factory.createArchiveInputStream("jar", in);
        assertNotNull(result);
        assertTrue(result instanceof JarArchiveInputStream);
    }

    // Tests createArchiveInputStream with valid CPIO archiver name and input stream
    @Test
    public void testCreateArchiveInputStream_cpio_returnsCpioArchiveInputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream result = factory.createArchiveInputStream("cpio", in);
        assertNotNull(result);
        assertTrue(result instanceof CpioArchiveInputStream);
    }

    // Tests createArchiveInputStream with unknown archiver name - expects ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_unknownArchiverName_throwsArchiveException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream("unknown", in);
    }

    // Tests createArchiveInputStream with SEVEN_Z archiver name - expects StreamingNotSupportedException
    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveInputStream_sevenZ_throwsStreamingNotSupportedException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream("7z", in);
    }

    // Tests createArchiveOutputStream with null archiver name - expects IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullArchiverName_throwsIllegalArgumentException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with null output stream - expects IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullOutputStream_throwsIllegalArgumentException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream("zip", null);
    }

    // Tests createArchiveOutputStream with valid ZIP archiver name and output stream
    @Test
    public void testCreateArchiveOutputStream_zip_returnsZipArchiveOutputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream result = factory.createArchiveOutputStream("zip", out);
        assertNotNull(result);
        assertTrue(result instanceof ZipArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with valid TAR archiver name and output stream
    @Test
    public void testCreateArchiveOutputStream_tar_returnsTarArchiveOutputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream result = factory.createArchiveOutputStream("tar", out);
        assertNotNull(result);
        assertTrue(result instanceof TarArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with valid JAR archiver name and output stream
    @Test
    public void testCreateArchiveOutputStream_jar_returnsJarArchiveOutputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream result = factory.createArchiveOutputStream("jar", out);
        assertNotNull(result);
        assertTrue(result instanceof JarArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with valid CPIO archiver name and output stream
    @Test
    public void testCreateArchiveOutputStream_cpio_returnsCpioArchiveOutputStream() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream result = factory.createArchiveOutputStream("cpio", out);
        assertNotNull(result);
        assertTrue(result instanceof CpioArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with unknown archiver name - expects ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_unknownArchiverName_throwsArchiveException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream("unknown", out);
    }

    // Tests createArchiveOutputStream with SEVEN_Z archiver name - expects StreamingNotSupportedException
    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveOutputStream_sevenZ_throwsStreamingNotSupportedException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream("7z", out);
    }

    // Tests createArchiveInputStream (auto-detect) with null stream - expects IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_autoDetect_nullStream_throwsIllegalArgumentException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream((InputStream) null);
    }

    // Tests createArchiveInputStream (auto-detect) with stream that does not support mark - expects IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_autoDetect_noMarkSupport_throwsIllegalArgumentException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createArchiveInputStream(in);
    }

    // Tests createArchiveInputStream (auto-detect) with empty buffer - expects ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autoDetect_emptyStream_throwsArchiveException() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream(in);
    }

    // Tests getEntryEncoding returns null when using default constructor
    @Test
    public void testGetEntryEncoding_defaultConstructor_returnsNull() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        assertNull(factory.getEntryEncoding());
    }

    // Tests getEntryEncoding returns encoding set via constructor
    @Test
    public void testGetEntryEncoding_constructorWithEncoding_returnsEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        assertEquals("UTF-8", factory.getEntryEncoding());
    }
}