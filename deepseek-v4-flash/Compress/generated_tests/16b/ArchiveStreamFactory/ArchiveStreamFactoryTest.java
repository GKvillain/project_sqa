package org.apache.commons.compress.archivers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    private static byte[] zipBytes;
    private static byte[] tarBytes;
    private static byte[] arBytes;
    private static byte[] cpioBytes;
    private static byte[] dumpBytes;
    private static byte[] tarInvalidBytes;

    @BeforeClass
    public static void setUp() throws IOException {
        zipBytes = createZipBytes();
        tarBytes = createTarBytes();
        arBytes = createArBytes();
        cpioBytes = createCpioBytes();
        dumpBytes = createDumpBytes();
        tarInvalidBytes = createTarInvalidBytes(tarBytes);
    }

    private static byte[] createZipBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            zos.putArchiveEntry(entry);
            zos.closeArchiveEntry();
        }
        return bos.toByteArray();
    }

    private static byte[] createTarBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(bos)) {
            TarArchiveEntry entry = new TarArchiveEntry("test.txt");
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
        }
        return bos.toByteArray();
    }

    private static byte[] createArBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ArArchiveOutputStream aos = new ArArchiveOutputStream(bos)) {
            ArArchiveEntry entry = new ArArchiveEntry("test", 0);
            aos.putArchiveEntry(entry);
            aos.closeArchiveEntry();
        }
        return bos.toByteArray();
    }

    private static byte[] createCpioBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(bos)) {
            CpioArchiveEntry entry = new CpioArchiveEntry("test.txt");
            entry.setSize(0);
            cos.putArchiveEntry(entry);
            cos.closeArchiveEntry();
        }
        return bos.toByteArray();
    }

    private static byte[] createDumpBytes() {
        byte[] b = new byte[32];
        b[0] = 0x01;
        b[1] = 0x15;
        return b;
    }

    private static byte[] createTarInvalidBytes(byte[] valid) {
        byte[] invalid = valid.clone();
        for (int i = 148; i < 156; i++) {
            invalid[i] = ' ';
        }
        return invalid;
    }

    private final ArchiveStreamFactory factory = new ArchiveStreamFactory();

    // ---------- createArchiveInputStream(String, InputStream) ----------
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullArchiverName_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullInputStream_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveInputStream("zip", null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_unknownArchiverName_throwsArchiveException() throws Exception {
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateArchiveInputStream_ar_returnsArArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream("ar", new ByteArrayInputStream(arBytes))) {
            assertTrue(ais instanceof ArArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_zip_returnsZipArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream("zip", new ByteArrayInputStream(zipBytes))) {
            assertTrue(ais instanceof ZipArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_tar_returnsTarArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream("tar", new ByteArrayInputStream(tarBytes))) {
            assertTrue(ais instanceof TarArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_jar_returnsJarArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream("jar", new ByteArrayInputStream(zipBytes))) {
            assertTrue(ais instanceof JarArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_cpio_returnsCpioArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream("cpio", new ByteArrayInputStream(cpioBytes))) {
            assertTrue(ais instanceof CpioArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_dump_returnsDumpArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream("dump", new ByteArrayInputStream(dumpBytes))) {
            assertTrue(ais instanceof DumpArchiveInputStream);
        }
    }

    // ---------- createArchiveInputStream(InputStream) ----------
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullStream_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_noMarkSupport_throwsIllegalArgumentException() throws Exception {
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
        factory.createArchiveInputStream(noMark);
    }

    @Test
    public void testCreateArchiveInputStream_autodetectZip_returnsZipArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            assertTrue(ais instanceof ZipArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_autodetectTar_returnsTarArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            assertTrue(ais instanceof TarArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_autodetectAr_returnsArArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream(new ByteArrayInputStream(arBytes))) {
            assertTrue(ais instanceof ArArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_autodetectCpio_returnsCpioArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream(new ByteArrayInputStream(cpioBytes))) {
            assertTrue(ais instanceof CpioArchiveInputStream);
        }
    }

    @Test
    public void testCreateArchiveInputStream_autodetectDump_returnsDumpArchiveInputStream() throws Exception {
        try (ArchiveInputStream ais = factory.createArchiveInputStream(new ByteArrayInputStream(dumpBytes))) {
            assertTrue(ais instanceof DumpArchiveInputStream);
        }
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autodetectTarInvalidChecksum_throwsArchiveException() throws Exception {
        factory.createArchiveInputStream(new ByteArrayInputStream(tarInvalidBytes));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autodetectUnknown_throwsArchiveException() throws Exception {
        byte[] unknown = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        factory.createArchiveInputStream(new ByteArrayInputStream(unknown));
    }

    // ---------- createArchiveOutputStream(String, OutputStream) ----------
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullArchiverName_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullOutputStream_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveOutputStream("zip", null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_unknownArchiverName_throwsArchiveException() throws Exception {
        factory.createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveOutputStream_ar_returnsArArchiveOutputStream() throws Exception {
        try (ArchiveOutputStream aos = factory.createArchiveOutputStream("ar", new ByteArrayOutputStream())) {
            assertTrue(aos instanceof ArArchiveOutputStream);
        }
    }

    @Test
    public void testCreateArchiveOutputStream_zip_returnsZipArchiveOutputStream() throws Exception {
        try (ArchiveOutputStream aos = factory.createArchiveOutputStream("zip", new ByteArrayOutputStream())) {
            assertTrue(aos instanceof ZipArchiveOutputStream);
        }
    }

    @Test
    public void testCreateArchiveOutputStream_tar_returnsTarArchiveOutputStream() throws Exception {
        try (ArchiveOutputStream aos = factory.createArchiveOutputStream("tar", new ByteArrayOutputStream())) {
            assertTrue(aos instanceof TarArchiveOutputStream);
        }
    }

    @Test
    public void testCreateArchiveOutputStream_jar_returnsJarArchiveOutputStream() throws Exception {
        try (ArchiveOutputStream aos = factory.createArchiveOutputStream("jar", new ByteArrayOutputStream())) {
            assertTrue(aos instanceof JarArchiveOutputStream);
        }
    }

    @Test
    public void testCreateArchiveOutputStream_cpio_returnsCpioArchiveOutputStream() throws Exception {
        try (ArchiveOutputStream aos = factory.createArchiveOutputStream("cpio", new ByteArrayOutputStream())) {
            assertTrue(aos instanceof CpioArchiveOutputStream);
        }
    }
}