package org.apache.commons.compress.archivers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArchiveStreamFactoryTest {

    private ArchiveStreamFactory factory;

    @Before
    public void setUp() {
        factory = new ArchiveStreamFactory();
    }

    // Tests createArchiveInputStream with valid format names
    @Test
    public void testCreateArchiveInputStream_validNames_returnsCorrectStreamTypes() throws Exception {
        byte[] dummyData = new byte[0];

        ArchiveInputStream arIn = factory.createArchiveInputStream(ArchiveStreamFactory.AR, new ByteArrayInputStream(dummyData));
        assertTrue(arIn instanceof ArArchiveInputStream);

        ArchiveInputStream zipIn = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, new ByteArrayInputStream(dummyData));
        assertTrue(zipIn instanceof ZipArchiveInputStream);

        ArchiveInputStream tarIn = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, new ByteArrayInputStream(dummyData));
        assertTrue(tarIn instanceof TarArchiveInputStream);

        ArchiveInputStream jarIn = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, new ByteArrayInputStream(dummyData));
        assertTrue(jarIn instanceof JarArchiveInputStream);

        ArchiveInputStream cpioIn = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, new ByteArrayInputStream(dummyData));
        assertTrue(cpioIn instanceof CpioArchiveInputStream);

        ArchiveInputStream dumpIn = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, new ByteArrayInputStream(dummyData));
        assertTrue(dumpIn instanceof DumpArchiveInputStream);
    }

    // Tests createArchiveInputStream case insensitivity
    @Test
    public void testCreateArchiveInputStream_caseInsensitiveName_returnsStream() throws Exception {
        ArchiveInputStream zipIn = factory.createArchiveInputStream("zIp", new ByteArrayInputStream(new byte[0]));
        assertNotNull(zipIn);
        assertTrue(zipIn instanceof ZipArchiveInputStream);
    }

    // Tests createArchiveInputStream with null name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullArchiverName_throwsException() throws Exception {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveInputStream with null input stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullInputStream_throwsException() throws Exception {
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    // Tests createArchiveInputStream with unknown format name throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_unknownName_throwsArchiveException() throws Exception {
        factory.createArchiveInputStream("unknownFormat", new ByteArrayInputStream(new byte[0]));
    }

    // Tests createArchiveOutputStream with valid format names
    @Test
    public void testCreateArchiveOutputStream_validNames_returnsCorrectStreamTypes() throws Exception {
        ArchiveOutputStream arOut = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, new ByteArrayOutputStream());
        assertTrue(arOut instanceof ArArchiveOutputStream);

        ArchiveOutputStream zipOut = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, new ByteArrayOutputStream());
        assertTrue(zipOut instanceof ZipArchiveOutputStream);

        ArchiveOutputStream tarOut = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, new ByteArrayOutputStream());
        assertTrue(tarOut instanceof TarArchiveOutputStream);

        ArchiveOutputStream jarOut = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, new ByteArrayOutputStream());
        assertTrue(jarOut instanceof JarArchiveOutputStream);

        ArchiveOutputStream cpioOut = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, new ByteArrayOutputStream());
        assertTrue(cpioOut instanceof CpioArchiveOutputStream);
    }

    // Tests createArchiveOutputStream with DUMP name throws ArchiveException as DUMP only supports reading
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_dumpName_throwsArchiveException() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with null name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullArchiverName_throwsException() throws Exception {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    // Tests createArchiveOutputStream with null output stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullOutputStream_throwsException() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    // Tests createArchiveOutputStream with unknown format name throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_unknownName_throwsArchiveException() throws Exception {
        factory.createArchiveOutputStream("invalidFormat", new ByteArrayOutputStream());
    }

    // Tests autodetect createArchiveInputStream with null stream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_autodetectNullStream_throwsException() throws Exception {
        factory.createArchiveInputStream((InputStream) null);
    }

    // Tests autodetect createArchiveInputStream with stream not supporting mark throws IllegalArgumentException
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

    // Tests autodetect createArchiveInputStream for Zip signature
    @Test
    public void testCreateArchiveInputStream_autodetectZipSignature_returnsZipStream() throws Exception {
        byte[] zipSignature = new byte[] { 'P', 'K', 3, 4, 0, 0, 0, 0, 0, 0, 0, 0 };
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(zipSignature));
        assertNotNull(in);
        assertTrue(in instanceof ZipArchiveInputStream);
    }

    // Tests autodetect createArchiveInputStream for Ar signature
    @Test
    public void testCreateArchiveInputStream_autodetectArSignature_returnsArStream() throws Exception {
        byte[] arSignature = new byte[] { '!', '<', 'a', 'r', 'c', 'h', '>', '\n', 0, 0, 0, 0 };
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(arSignature));
        assertNotNull(in);
        assertTrue(in instanceof ArArchiveInputStream);
    }

    // Tests autodetect createArchiveInputStream for Cpio signature
    @Test
    public void testCreateArchiveInputStream_autodetectCpioSignature_returnsCpioStream() throws Exception {
        byte[] cpioSignature = new byte[] { '0', '7', '0', '7', '0', '1', 0, 0, 0, 0, 0, 0 };
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioSignature));
        assertNotNull(in);
        assertTrue(in instanceof CpioArchiveInputStream);
    }

    // Tests autodetect createArchiveInputStream for non-archive stream throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autodetectNonArchiveStream_throwsArchiveException() throws Exception {
        byte[] nonArchiveData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        factory.createArchiveInputStream(new ByteArrayInputStream(nonArchiveData));
    }

    // Tests autodetect createArchiveInputStream for empty stream throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autodetectEmptyStream_throwsArchiveException() throws Exception {
        factory.createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }
}