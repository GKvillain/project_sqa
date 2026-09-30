package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

/**
 * JUnit 4 test class for TarArchiveOutputStream.
 * Targets Defects4J bug 29b.
 */
public class TarArchiveOutputStreamTest {

    private static final String LONG_NAME = "thisIsAVeryLongFileNameThatExceedsTheTraditionalTarNameLengthOf100CharactersSoItWillTriggerTheLongNameHandlingInTarArchiveOutputStream"
            + "AndMustBeHandledByGnuOrPosixExtensionsOrTruncated";

    // ----- Helper methods -----
    private TarArchiveOutputStream createOutputStream() {
        return new TarArchiveOutputStream(new ByteArrayOutputStream());
    }

    private TarArchiveOutputStream createOutputStream(int blockSize, int recordSize) {
        return new TarArchiveOutputStream(new ByteArrayOutputStream(), blockSize, recordSize);
    }

    private TarArchiveOutputStream createOutputStreamWithEncoding(String encoding) {
        return new TarArchiveOutputStream(new ByteArrayOutputStream(), encoding);
    }

    private TarArchiveEntry createEntry(String name, long size) {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        return entry;
    }

    // ----- Normal cases -----

    @Test
    public void testPutArchiveEntry_normalEntry_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("normal.txt", 10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[10]);
        tos.closeArchiveEntry();
        // no exception means success
        tos.close();
    }

    @Test
    public void testWrite_correctSize_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("test.dat", 20);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test
    public void testFinish_normal_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("a.txt", 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();
    }

    // ----- Boundary cases: long file names -----

    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longName_error_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        TarArchiveEntry entry = createEntry(LONG_NAME, 0);
        tos.putArchiveEntry(entry);
        // should throw RuntimeException for too long name
    }

    @Test
    public void testPutArchiveEntry_longName_truncate_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        TarArchiveEntry entry = createEntry(LONG_NAME, 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        // no exception
    }

    @Test
    public void testPutArchiveEntry_longName_gnu_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry entry = createEntry(LONG_NAME, 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test
    public void testPutArchiveEntry_longName_posix() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        TarArchiveEntry entry = createEntry(LONG_NAME, 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // ----- Big number modes -----

    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_bigNumber_error_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        // size > TarConstants.MAXSIZE (which is 077777777777L = 8589934591?)
        TarArchiveEntry entry = createEntry("big", TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testPutArchiveEntry_bigNumber_star_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = createEntry("big_star", TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test
    public void testPutArchiveEntry_bigNumber_posix_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = createEntry("big_posix", TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // ----- PAX headers for non-ASCII names -----

    @Test
    public void testPutArchiveEntry_nonAsciiName_paxHeadersAdded() throws IOException {
        TarArchiveOutputStream tos = createOutputStreamWithEncoding("UTF-8");
        tos.setAddPaxHeadersForNonAsciiNames(true);
        // use a name with non-ASCII characters (e.g. é)
        TarArchiveEntry entry = new TarArchiveEntry("f\u00e9e.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // ----- Exception paths: closeArchiveEntry -----

    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        // no putArchiveEntry called
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_dataLessThanSize_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("incomplete.dat", 100);
        tos.putArchiveEntry(entry);
        // write only 50 bytes
        tos.write(new byte[50]);
        tos.closeArchiveEntry(); // should throw
    }

    @Test(expected = IOException.class)
    public void testWrite_exceedsSize_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("small.dat", 10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[20]); // exceeds header size
    }

    // ----- Edge: finish without closing entry -----

    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("unclosed.dat", 10);
        tos.putArchiveEntry(entry);
        // not closing entry
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throws() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("finished.dat", 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.finish(); // second finish should throw
    }

    // ----- Record assembly boundary -----

    @Test
    public void testWrite_assemblyBoundary_correct() throws IOException {
        int recordSize = 512;
        TarArchiveOutputStream tos = createOutputStream(1024, recordSize);
        TarArchiveEntry entry = createEntry("assembly", recordSize * 2);
        tos.putArchiveEntry(entry);
        // write a chunk smaller than recordSize, then more
        byte[] partial = new byte[100];
        tos.write(partial);
        byte[] rest = new byte[recordSize * 2 - 100];
        tos.write(rest);
        tos.closeArchiveEntry();
        tos.close();
    }

    // ----- Multiple entries -----

    @Test
    public void testPutArchiveEntry_multipleEntries_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        for (int i = 0; i < 5; i++) {
            TarArchiveEntry entry = createEntry("file" + i + ".txt", 1);
            tos.putArchiveEntry(entry);
            tos.write(new byte[1]);
            tos.closeArchiveEntry();
        }
        tos.close();
    }

    // ========== NEW TEST CASES (to improve coverage) ==========

    // --- Encoding without PAX headers for non-ASCII names ---
    @Test
    public void testPutArchiveEntry_nonAsciiName_noPax_utf8() throws IOException {
        // Use UTF-8 encoding but disable PAX header addition for non-ASCII names
        TarArchiveOutputStream tos = createOutputStreamWithEncoding("UTF-8");
        tos.setAddPaxHeadersForNonAsciiNames(false);
        TarArchiveEntry entry = new TarArchiveEntry("f\u00e9e.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        // no exception expected; non-ASCII name may still be encoded as UTF-8 in header
    }

    // --- write(byte[], int, int) with offset and length ---
    @Test
    public void testWrite_withOffset_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("offset.dat", 10);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[20];
        // write only bytes 5..14 (10 bytes)
        tos.write(data, 5, 10);
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- getBytesWritten() method ---
    @Test
    public void testGetBytesWritten_afterWrite() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("count.dat", 8);
        tos.putArchiveEntry(entry);
        tos.write(new byte[4]);
        long before = tos.getBytesWritten();
        tos.write(new byte[4]);
        long after = tos.getBytesWritten();
        tos.closeArchiveEntry();
        tos.close();
        // At least 8 bytes written (header + data), before=some, after=before+4
        assertTrue("getBytesWritten should increase after write", before > 0);
        assertEquals("Expected 4 more bytes written", before + 4, after);
    }

    // --- close() after close should not throw ---
    @Test
    public void testClose_idempotent() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("idempotent.txt", 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        tos.close(); // second close should be safe
    }

    // --- flush() method ---
    @Test
    public void testFlush_afterWrite_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry("flush.dat", 512);
        tos.putArchiveEntry(entry);
        tos.write(new byte[512]);
        tos.flush(); // should not throw
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- Long name with fallback to PAX enabled ---
    @Test
    public void testPutArchiveEntry_longName_fallbackToPax() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.setFallbackToPax(true); // should cause fallback to POSIX extension
        TarArchiveEntry entry = createEntry(LONG_NAME, 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- Big number mode with BIGNUMBER_WARN (should not throw) ---
    @Test
    public void testPutArchiveEntry_bigNumber_warn_success() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_WARN);
        TarArchiveEntry entry = createEntry("big_warn", TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- Directory entry ---
    @Test
    public void testPutArchiveEntry_directoryEntry() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry dir = new TarArchiveEntry("mydir/");
        dir.setSize(0); // directory size is 0
        dir.setMode(040755); // typical dir permissions
        tos.putArchiveEntry(dir);
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- Entry with symbolic link ---
    @Test
    public void testPutArchiveEntry_symlinkEntry() throws IOException {
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry link = new TarArchiveEntry("mylink");
        link.setLinkName("target");
        link.setSize(0);
        tos.putArchiveEntry(link);
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- Constructor with null encoding (default) ---
    @Test
    public void testConstructor_nullEncoding() throws IOException {
        TarArchiveOutputStream tos = new TarArchiveOutputStream(new ByteArrayOutputStream(), (String) null);
        TarArchiveEntry entry = createEntry("defaultEncoding.txt", 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // --- Name exactly 100 characters (boundary) ---
    @Test
    public void testPutArchiveEntry_nameExact100Chars() throws IOException {
        String name = "a";
        for (int i = 0; i < 99; i++) {
            name += "b";
        }
        assertEquals(100, name.length());
        TarArchiveOutputStream tos = createOutputStream();
        TarArchiveEntry entry = createEntry(name, 0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }
}