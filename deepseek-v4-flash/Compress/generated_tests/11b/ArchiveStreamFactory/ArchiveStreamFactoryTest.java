import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArchiveStreamFactoryTest {

    private final ArchiveStreamFactory factory = new ArchiveStreamFactory();

    // ========== createArchiveInputStream(String, InputStream) tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullArchiverName_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_nullInputStream_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveInputStream(ArchiveStreamFactory.AR, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_unknownArchiver_throwsArchiveException() throws Exception {
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateArchiveInputStream_ar_returnsArArchiveInputStream() throws Exception {
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.AR, new ByteArrayInputStream(new byte[0]));
        assertTrue("Expected ArArchiveInputStream", ais instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_zip_returnsZipArchiveInputStream() throws Exception {
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, new ByteArrayInputStream(new byte[0]));
        assertTrue("Expected ZipArchiveInputStream", ais instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_tar_returnsTarArchiveInputStream() throws Exception {
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, new ByteArrayInputStream(new byte[0]));
        assertTrue("Expected TarArchiveInputStream", ais instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_jar_returnsJarArchiveInputStream() throws Exception {
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, new ByteArrayInputStream(new byte[0]));
        assertTrue("Expected JarArchiveInputStream", ais instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_cpio_returnsCpioArchiveInputStream() throws Exception {
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, new ByteArrayInputStream(new byte[0]));
        assertTrue("Expected CpioArchiveInputStream", ais instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_dump_returnsDumpArchiveInputStream() throws Exception {
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, new ByteArrayInputStream(new byte[0]));
        assertTrue("Expected DumpArchiveInputStream", ais instanceof DumpArchiveInputStream);
    }

    // ========== createArchiveOutputStream(String, OutputStream) tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullArchiverName_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveOutputStream(null, new java.io.ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStream_nullOutputStream_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.AR, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStream_unknownArchiver_throwsArchiveException() throws Exception {
        factory.createArchiveOutputStream("unknown", new java.io.ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveOutputStream_ar_returnsArArchiveOutputStream() throws Exception {
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, new java.io.ByteArrayOutputStream());
        assertTrue("Expected ArArchiveOutputStream", aos instanceof ArArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStream_zip_returnsZipArchiveOutputStream() throws Exception {
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, new java.io.ByteArrayOutputStream());
        assertTrue("Expected ZipArchiveOutputStream", aos instanceof ZipArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStream_tar_returnsTarArchiveOutputStream() throws Exception {
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, new java.io.ByteArrayOutputStream());
        assertTrue("Expected TarArchiveOutputStream", aos instanceof TarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStream_jar_returnsJarArchiveOutputStream() throws Exception {
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, new java.io.ByteArrayOutputStream());
        assertTrue("Expected JarArchiveOutputStream", aos instanceof JarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStream_cpio_returnsCpioArchiveOutputStream() throws Exception {
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, new java.io.ByteArrayOutputStream());
        assertTrue("Expected CpioArchiveOutputStream", aos instanceof CpioArchiveOutputStream);
    }

    // NEW TEST: createArchiveOutputStream for dump
    @Test
    public void testCreateArchiveOutputStream_dump_returnsDumpArchiveOutputStream() throws Exception {
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new java.io.ByteArrayOutputStream());
        assertTrue("Expected DumpArchiveOutputStream", aos instanceof DumpArchiveOutputStream);
    }

    // ========== createArchiveInputStream(InputStream) auto-detect tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_autoDetect_nullStream_throwsIllegalArgumentException() throws Exception {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStream_autoDetect_markNotSupported_throwsIllegalArgumentException() throws Exception {
        // Stream without mark support
        InputStream noMarkStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createArchiveInputStream(noMarkStream);
    }

    @Test
    public void testCreateArchiveInputStream_autoDetect_zipSignature_returnsZipArchiveInputStream() throws Exception {
        // ZIP local file header signature: PK\003\004 (0x50,0x4b,0x03,0x04)
        byte[] zipSig = new byte[] {0x50, 0x4b, 0x03, 0x04};
        InputStream in = new ByteArrayInputStream(zipSig);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue("Expected ZipArchiveInputStream", ais instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_autoDetect_arSignature_returnsArArchiveInputStream() throws Exception {
        // AR signature: "!<arch>\n"
        byte[] arSig = "!<arch>\n".getBytes();
        InputStream in = new ByteArrayInputStream(arSig);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue("Expected ArArchiveInputStream", ais instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_autoDetect_cpioSignature_returnsCpioArchiveInputStream() throws Exception {
        // CPIO signature: "070707" (old POSIX) or "070701" (new)
        // Using "070707"
        byte[] cpioSig = "070707".getBytes();
        InputStream in = new ByteArrayInputStream(cpioSig);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue("Expected CpioArchiveInputStream", ais instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStream_autoDetect_tarSignature_returnsTarArchiveInputStream() throws Exception {
        // TAR signature: 512 bytes with "ustar\0" at offset 257
        byte[] tarSig = new byte[512];
        // Fill zero first
        System.arraycopy("ustar\0".getBytes(), 0, tarSig, 257, 6);
        InputStream in = new ByteArrayInputStream(tarSig);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue("Expected TarArchiveInputStream", ais instanceof TarArchiveInputStream);
    }

    // NEW TEST: auto-detect dump signature
    @Test
    public void testCreateArchiveInputStream_autoDetect_dumpSignature_returnsDumpArchiveInputStream() throws Exception {
        // Dump magic: "NFS4" at offset 0, need at least 32 bytes total
        byte[] dumpSig = new byte[32];
        System.arraycopy("NFS4".getBytes(), 0, dumpSig, 0, 4);
        InputStream in = new ByteArrayInputStream(dumpSig);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertTrue("Expected DumpArchiveInputStream", ais instanceof DumpArchiveInputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autoDetect_noSignature_throwsArchiveException() throws Exception {
        // Stream with no recognizable signature
        byte[] unknown = new byte[] {0x00, 0x01, 0x02, 0x03};
        InputStream in = new ByteArrayInputStream(unknown);
        factory.createArchiveInputStream(in);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autoDetect_ioException_throwsArchiveException() throws Exception {
        // Stream that throws IOException during reset
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
            @Override
            public boolean markSupported() {
                return true;
            }
            @Override
            public void mark(int readlimit) {
                // noop
            }
            @Override
            public void reset() throws IOException {
                throw new IOException("simulated");
            }
        };
        factory.createArchiveInputStream(failingStream);
    }

    // Additional test for the tar fallback path (COMPRESS-117 related)
    // This test creates a stream with bytes that do not match the tar magic
    // but TarArchiveInputStream can still parse a valid entry.
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStream_autoDetect_nonUstarTar_throwsArchiveException() throws Exception {
        // Create a tar header without "ustar\0" magic (old tar).
        // Such a header may still be parsable, but matches() returns false.
        // In the buggy version (11), the fallback try-catch may fail to detect it,
        // resulting in ArchiveException. This test verifies that the factory
        // cannot detect such tar (expected behavior for the buggy code path).
        byte[] tarHeader = new byte[512];
        // Filename set to "test" at offset 0
        System.arraycopy("test\0".getBytes(), 0, tarHeader, 0, 5);
        // Set size field (octal) at offset 124 (124-135): 00000000000
        // No magic set; rest zero.
        InputStream in = new ByteArrayInputStream(tarHeader);
        factory.createArchiveInputStream(in);
    }
}