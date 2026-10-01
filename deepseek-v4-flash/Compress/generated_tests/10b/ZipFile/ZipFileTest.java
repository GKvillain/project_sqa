package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.CRC32;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipFileTest {

    private File tempZip;
    private ZipFile zipFile;

    @Before
    public void setUp() throws IOException {
        tempZip = File.createTempFile("junit", ".zip");
        tempZip.deleteOnExit();
    }

    @After
    public void tearDown() throws IOException {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                // ignore
            }
            zipFile = null;
        }
        if (tempZip.exists()) {
            tempZip.delete();
        }
    }

    // ----- helper methods -----

    private void addStoredEntry(ZipArchiveOutputStream zos, String name, String content) throws IOException {
        byte[] data = content.getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipArchiveEntry.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
    }

    private void addDeflatedEntry(ZipArchiveOutputStream zos, String name, String content) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        zos.putArchiveEntry(entry);
        zos.write(content.getBytes("UTF-8"));
        zos.closeArchiveEntry();
    }

    private void addEntryWithUnicodeExtraField(ZipArchiveOutputStream zos,
                                                String unicodeName,
                                                String encodedName,
                                                String encoding) throws IOException {
        byte[] encodedBytes = encodedName.getBytes(encoding);
        ZipArchiveEntry entry = new ZipArchiveEntry(encodedName);
        GeneralPurposeBit gp = new GeneralPurposeBit();
        gp.useUTF8ForNames(false);
        entry.setGeneralPurposeBit(gp);
        UnicodePathExtraField ufield = new UnicodePathExtraField(unicodeName, encodedBytes);
        entry.addExtraField(ufield);
        // use stored entry for simplicity
        byte[] content = "test content".getBytes("UTF-8");
        entry.setMethod(ZipArchiveEntry.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
    }

    private String readEntryContent(ZipFile zf, ZipArchiveEntry entry) throws IOException {
        InputStream is = zf.getInputStream(entry);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        is.close();
        return baos.toString("UTF-8");
    }

    // ----- tests -----

    // Tests normal opening of a zip with multiple entries
    @Test
    public void testOpen_normalZip_entriesRead() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "hello.txt", "Hello World");
            addDeflatedEntry(zos, "sub/dir/file.dat", "binary data");
        }
        zipFile = new ZipFile(tempZip);
        Enumeration<ZipArchiveEntry> e = zipFile.getEntries();
        int count = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            count++;
        }
        assertEquals(2, count);
        assertNotNull(zipFile.getEntry("hello.txt"));
        assertNotNull(zipFile.getEntry("sub/dir/file.dat"));
    }

    // Tests reading a stored entry's content
    @Test
    public void testGetInputStream_storedEntry_returnsContent() throws IOException {
        final String content = "Stored content";
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addStoredEntry(zos, "stored.txt", content);
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry entry = zipFile.getEntry("stored.txt");
        assertNotNull(entry);
        assertEquals(content, readEntryContent(zipFile, entry));
    }

    // Tests reading a deflated entry's content
    @Test
    public void testGetInputStream_deflatedEntry_returnsContent() throws IOException {
        final String content = "Deflated content with some repeated bytes aaaaa bbbbb ccccc";
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "deflated.txt", content);
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry entry = zipFile.getEntry("deflated.txt");
        assertNotNull(entry);
        assertEquals(content, readEntryContent(zipFile, entry));
    }

    // Tests getEntry returns the correct entry
    @Test
    public void testGetEntry_existingEntry_returnsEntry() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "test.txt", "data");
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry entry = zipFile.getEntry("test.txt");
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
    }

    // Tests getEntry returns null for non-existing name
    @Test
    public void testGetEntry_nonExistingEntry_returnsNull() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "exists.txt", "data");
        }
        zipFile = new ZipFile(tempZip);
        assertNull(zipFile.getEntry("nonexistent.txt"));
    }

    // Tests getInputStream returns null for entry not belonging to this zip
    @Test
    public void testGetInputStream_nonexistingEntry_returnsNull() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "a.txt", "a");
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry fake = new ZipArchiveEntry("fake.txt");
        assertNull(zipFile.getInputStream(fake));
    }

    // Tests that reading after close throws IOException
    @Test(expected = IOException.class)
    public void testClose_closedFile_throwsException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "test.txt", "data");
        }
        zipFile = new ZipFile(tempZip);
        zipFile.close();
        // getInputStream will attempt to seek the closed RandomAccessFile
        ZipArchiveEntry entry = zipFile.getEntry("test.txt");
        assertNotNull(entry);
        zipFile.getInputStream(entry); // should throw IOException
    }

    // Tests closeQuietly with null does not throw
    @Test
    public void testCloseQuietly_null_noException() {
        ZipFile.closeQuietly(null); // should not throw
    }

    // Tests closeQuietly closes the file
    @Test
    public void testCloseQuietly_openFile_closesSuccessfully() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "test.txt", "data");
        }
        zipFile = new ZipFile(tempZip);
        ZipFile.closeQuietly(zipFile);
        // after close, reading should throw IOException
        try {
            zipFile.getInputStream(zipFile.getEntry("test.txt"));
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    // Tests reading a zip entry that uses Zip64 extension (Zip64Mode.Always)
    @Test
    public void testZip64Entry_readsCorrectly() throws IOException {
        final String content = "Zip64 test content";
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            zos.setUseZip64(Zip64Mode.Always);
            addDeflatedEntry(zos, "zip64.txt", content);
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry entry = zipFile.getEntry("zip64.txt");
        assertNotNull(entry);
        assertEquals(content, readEntryContent(zipFile, entry));
    }

    // Tests entry with Unicode name (UTF-8 flag set)
    @Test
    public void testEntryWithUnicodeName_usingUTF8Flag_readsCorrectly() throws IOException {
        final String unicodeName = "ファイル.txt";
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, unicodeName, "content");
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry entry = zipFile.getEntry(unicodeName);
        assertNotNull("Unicode entry should be found", entry);
        assertEquals(unicodeName, entry.getName());
    }

    // Tests entry with Unicode extra field (no UTF-8 flag) should resolve to unicode name
    @Test
    public void testEntryWithUnicodeExtraField_usesUnicodeField() throws IOException {
        final String unicodeName = "名前.txt";
        final String encodedName = "name.txt";
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addEntryWithUnicodeExtraField(zos, unicodeName, encodedName, "Cp437");
        }
        // open with Cp437 encoding; useUnicodeExtraFields defaults to true
        zipFile = new ZipFile(tempZip, "Cp437");
        // The entry should be accessible via its unicode name
        ZipArchiveEntry entry = zipFile.getEntry(unicodeName);
        assertNotNull("Entry should be found by unicode name", entry);
        assertEquals(unicodeName, entry.getName());
        // The original encoded name should not be present
        assertNull("Old encoded name should not exist", zipFile.getEntry(encodedName));
    }

    // Tests getEntriesInPhysicalOrder returns entries sorted by offset
    @Test
    public void testGetEntriesInPhysicalOrder_entriesReturnedInOrder() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "first.txt", "first");
            addDeflatedEntry(zos, "second.txt", "second");
            addDeflatedEntry(zos, "third.txt", "third");
        }
        zipFile = new ZipFile(tempZip);
        Enumeration<ZipArchiveEntry> phys = zipFile.getEntriesInPhysicalOrder();
        assertTrue(phys.hasMoreElements());
        assertEquals("first.txt", phys.nextElement().getName());
        assertEquals("second.txt", phys.nextElement().getName());
        assertEquals("third.txt", phys.nextElement().getName());
        assertFalse(phys.hasMoreElements());
    }

    // Tests opening an empty zip (no entries)
    @Test
    public void testEmptyZipFile_noEntries() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(tempZip, "rw")) {
            // write valid empty EOCD
            byte[] eocd = new byte[] {
                0x50, 0x4b, 0x05, 0x06, // signature
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            };
            raf.write(eocd);
        }
        zipFile = new ZipFile(tempZip);
        assertFalse(zipFile.getEntries().hasMoreElements());
    }

    // Tests that a zip without central directory throws ZipException
    @Test(expected = ZipException.class)
    public void testCorruptZipWithoutCentralDirectory_throwsException() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(tempZip, "rw")) {
            // write local file header only
            raf.write(ZipArchiveOutputStream.LFH_SIG);
            // write blank header (26 bytes) + filename
            byte[] header = new byte[26];
            raf.write(header);
            raf.write("test.txt".getBytes());
        }
        new ZipFile(tempZip); // should throw ZipException
    }

    // Tests constructor with specified encoding returns that encoding
    @Test
    public void testConstructor_withEncoding_specifiedEncoding() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "a.txt", "a");
        }
        zipFile = new ZipFile(tempZip, "ISO-8859-1");
        assertEquals("ISO-8859-1", zipFile.getEncoding());
    }

    // Tests default encoding is UTF-8
    @Test
    public void testGetEncoding_returnsDefaultUtf8() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "a.txt", "a");
        }
        zipFile = new ZipFile(tempZip);
        assertEquals("UTF-8", zipFile.getEncoding());
    }

    // ----- New tests for uncovered areas -----

    // Tests constructor accepting a String path
    @Test
    public void testConstructorWithStringPath() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            addDeflatedEntry(zos, "pathTest.txt", "via string path");
        }
        zipFile = new ZipFile(tempZip.getAbsolutePath());  // String constructor
        assertNotNull(zipFile.getEntry("pathTest.txt"));
        assertEquals("via string path", readEntryContent(zipFile, zipFile.getEntry("pathTest.txt")));
    }

    // Tests getEntry with a name encoded in Cp437 (no UTF-8 flag, no extra field)
    @Test
    public void testGetEntryWithCp437NameWithoutUnicodeFlag() throws IOException {
        final String entryName = "ÄÖÜ.txt";  // characters representable in Cp437
        // Create zip using Cp437 encoding to avoid UTF-8 flag
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip, "Cp437")) {
            addDeflatedEntry(zos, entryName, "cp437 content");
        }
        // Open with same encoding
        zipFile = new ZipFile(tempZip, "Cp437");
        ZipArchiveEntry entry = zipFile.getEntry(entryName);
        assertNotNull("Entry with Cp437-encoded name should be found", entry);
        assertEquals(entryName, entry.getName());
    }

    // Tests getInputStream returns null for a directory entry
    @Test
    public void testGetInputStreamForDirectoryEntry_returnsNull() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZip)) {
            // add a directory entry (name ends with '/', content empty)
            addStoredEntry(zos, "mydir/", "");
        }
        zipFile = new ZipFile(tempZip);
        ZipArchiveEntry dirEntry = zipFile.getEntry("mydir/");
        assertNotNull("Directory entry should exist", dirEntry);
        assertNull("getInputStream for directory should return null",
                    zipFile.getInputStream(dirEntry));
    }
}