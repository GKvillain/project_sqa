package org.apache.commons.compress.archivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Before;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    private ArchiveStreamFactory factory;

    @Before
    public void setUp() {
        factory = new ArchiveStreamFactory();
    }

    // Tests createArchiveInputStream with valid archiver names
    @Test
    public void testCreateArchiveInputStream_validNames_returnsCorrectStream() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);

        assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.AR, in) instanceof ArArchiveInputStream);
        assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, in) instanceof ZipArchiveInputStream);
        assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.TAR, in) instanceof TarArchiveInputStream);
        assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.JAR, in) instanceof JarArchiveInputStream);
        assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, in) instanceof CpioArchiveInputStream);
        assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, in) instanceof DumpArchiveInputStream);
    }

    // Tests createArchiveInputStream with null archiver name
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullArchiverName_throwsException() throws Exception {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveInputStream with null input stream
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullInputStream_throwsException() throws Exception {
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    // Tests createArchiveInputStream with unknown archiver name
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_unknownArchiverName_throwsException() throws Exception {
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveOutputStream with valid archiver names
    @Test
    public void testCreateArchiveOutputStream_validNames_returnsCorrectStream() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.AR, out) instanceof ArArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, out) instanceof ZipArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, out) instanceof TarArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out) instanceof JarArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, out) instanceof CpioArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with unsupported dump format
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_dumpFormat_throwsException() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with null archiver name
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullArchiverName_throwsException() throws Exception {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with null output stream
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullOutputStream_throwsException() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    // Tests createArchiveOutputStream with unknown archiver name
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_unknownArchiverName_throwsException() throws Exception {
        factory.createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    // Tests createArchiveInputStream autodetection with null stream
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_autodetectNullStream_throwsException() throws Exception {
        factory.createArchiveInputStream((InputStream) null);
    }

    // Tests createArchiveInputStream autodetection when stream does not support mark
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_markNotSupported_throwsException() throws Exception {
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

    // Tests autodetecting ZIP archive input stream
    @Test
    public void testCreateArchiveInputStream_autodetectZip_returnsZipInputStream() throws Exception {
        byte[] zipHeader = new byte[] { 'P', 'K', 3, 4, 0, 0, 0, 0, 0, 0, 0, 0 };
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(zipHeader));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    // Tests autodetecting AR archive input stream
    @Test
    public void testCreateArchiveInputStream_autodetectAr_returnsArInputStream() throws Exception {
        byte[] arHeader = "!<arch>\n".getBytes("US-ASCII");
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(arHeader));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue(ais instanceof ArArchiveInputStream);
    }

    // Tests autodetecting CPIO archive input stream
    @Test
    public void testCreateArchiveInputStream_autodetectCpio_returnsCpioInputStream() throws Exception {
        byte[] cpioHeader = "070701".getBytes("US-ASCII");
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(cpioHeader));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue(ais instanceof CpioArchiveInputStream);
    }

    // Tests createArchiveInputStream autodetection on empty or non-archive stream throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autodetectUnknownFormat_throwsException() throws Exception {
        byte[] unknownData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(unknownData));
        factory.createArchiveInputStream(in);
    }

    // Tests autodetection does not falsely recognize 512 bytes of zeros or arbitrary non-TAR data as TAR
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autodetectNonTar512Bytes_throwsException() throws Exception {
        byte[] nonTarData = new byte[512];
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(nonTarData));
        factory.createArchiveInputStream(in);
    }
}