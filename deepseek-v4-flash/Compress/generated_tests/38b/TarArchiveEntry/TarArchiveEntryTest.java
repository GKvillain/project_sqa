package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class TarArchiveEntryTest {

    // Tests basic constructor with a file name
    @Test
    public void testConstructorNameFile() {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertEquals(TarArchiveEntry.DEFAULT_FILE_MODE, entry.getMode());
        assertTrue(entry.isFile());
        assertFalse(entry.isDirectory());
        assertEquals(0, entry.getSize());
        assertTrue(entry.getModTime().getTime() > 0);
        assertEquals("", entry.getUserName());
        assertEquals("", entry.getGroupName());
    }

    // Tests constructor with a directory name ending with '/'
    @Test
    public void testConstructorNameDir() {
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        assertEquals("mydir/", entry.getName());
        assertEquals(TarArchiveEntry.DEFAULT_DIR_MODE, entry.getMode());
        assertTrue(entry.isDirectory());
        assertFalse(entry.isFile());
    }

    // Tests that leading slashes are removed by default
    @Test
    public void testConstructorNameLeadingSlashRemoved() {
        TarArchiveEntry entry = new TarArchiveEntry("/foo/bar.txt");
        assertEquals("foo/bar.txt", entry.getName());
    }

    // Tests that leading slashes are preserved when flag is set
    @Test
    public void testConstructorNameLeadingSlashPreserved() {
        TarArchiveEntry entry = new TarArchiveEntry("/foo/bar.txt", true);
        assertEquals("/foo/bar.txt", entry.getName());
    }

    // Tests constructor with name and link flag (symbolic link)
    @Test
    public void testConstructorNameWithLinkFlag() {
        TarArchiveEntry entry = new TarArchiveEntry("mylink", TarArchiveEntry.LF_SYMLINK);
        assertEquals("mylink", entry.getName());
        assertTrue(entry.isSymbolicLink());
        assertFalse(entry.isFile());
        assertFalse(entry.isDirectory());
    }

    // Tests constructor with a regular file (with content)
    @Test
    public void testConstructorFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        try {
            // Write content to set size
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write("Hello, world!".getBytes());
            }
            TarArchiveEntry entry = new TarArchiveEntry(tempFile);
            assertTrue(entry.getName().contains("test"));
            assertTrue(entry.isFile());
            assertFalse(entry.isDirectory());
            assertEquals(13, entry.getSize());
            assertTrue(entry.getModTime().getTime() <= System.currentTimeMillis() + 1000);
        } finally {
            tempFile.delete();
        }
    }

    // Tests constructor with a directory
    @Test
    public void testConstructorDirectory() throws IOException {
        File tempDir = File.createTempFile("testdir", "");
        tempDir.delete();
        File dir = new File(tempDir.getAbsolutePath());
        dir.mkdir();
        try {
            TarArchiveEntry entry = new TarArchiveEntry(dir);
            assertTrue(entry.isDirectory());
            assertFalse(entry.isFile());
            assertTrue(entry.getName().endsWith("/"));
        } finally {
            dir.delete();
        }
    }

    // Tests constructor with a file and a custom name
    @Test
    public void testConstructorFileCustomName() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        try {
            TarArchiveEntry entry = new TarArchiveEntry(tempFile, "custom/path/file.txt");
            assertEquals("custom/path/file.txt", entry.getName());
            assertTrue(entry.isFile());
        } finally {
            tempFile.delete();
        }
    }

    // Tests setName normalisation (leading slash removal, backslash conversion)
    @Test
    public void testSetNameNormalization() {
        TarArchiveEntry entry = new TarArchiveEntry("old");
        entry.setName("/new/path/with/leading/slash");
        assertEquals("new/path/with/leading/slash", entry.getName());
        entry.setName("back\\slash/path");
        assertEquals("back/slash/path", entry.getName());
    }

    // Tests setSize with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetSizeNegativeThrowsException() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setSize(-1);
    }

    // Tests setSize with valid value
    @Test
    public void testSetSizeValid() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setSize(100);
        assertEquals(100, entry.getSize());
    }

    // Tests setDevMajor with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDevMajorNegativeThrowsException() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setDevMajor(-1);
    }

    // Tests setDevMinor with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDevMinorNegativeThrowsException() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setDevMinor(-1);
    }

    // Tests isDirectory for entry with name ending with '/'
    @Test
    public void testIsDirectoryWithNameSlash() {
        TarArchiveEntry entry = new TarArchiveEntry("somedir/");
        assertTrue(entry.isDirectory());
    }

    // Tests isDirectory for entry with LF_DIR but no trailing slash
    @Test
    public void testIsDirectoryWithLinkFlag() {
        TarArchiveEntry entry = new TarArchiveEntry("somedir", TarArchiveEntry.LF_DIR);
        assertTrue(entry.isDirectory());
    }

    // Tests isFile for entry with LF_NORMAL link flag
    @Test
    public void testIsFileWithLinkFlag() {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt", TarArchiveEntry.LF_NORMAL);
        assertTrue(entry.isFile());
        assertFalse(entry.isDirectory());
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        TarArchiveEntry entry1 = new TarArchiveEntry("file.txt");
        TarArchiveEntry entry2 = new TarArchiveEntry("file.txt");
        TarArchiveEntry entry3 = new TarArchiveEntry("other.txt");
        assertTrue(entry1.equals(entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("string"));
    }

    // Tests isDescendent
    @Test
    public void testIsDescendent() {
        TarArchiveEntry parent = new TarArchiveEntry("parent/");
        TarArchiveEntry child = new TarArchiveEntry("parent/child");
        assertTrue(parent.isDescendent(child));
        assertFalse(child.isDescendent(parent));
    }

    // Tests setModTime with Date
    @Test
    public void testSetModTime() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        Date now = new Date();
        entry.setModTime(now);
        assertEquals(now.getTime() / 1000, entry.getModTime().getTime() / 1000);
    }

    // Tests setModTime with long (millis)
    @Test
    public void testSetModTimeLong() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        long millis = 1000000000L;
        entry.setModTime(millis);
        assertEquals(millis / 1000, entry.getModTime().getTime() / 1000);
    }

    // Tests that setName respects preserveLeadingSlashes flag
    @Test
    public void testPreserveLeadingSlashesInSetName() {
        TarArchiveEntry entry = new TarArchiveEntry("base", true);
        entry.setName("/absolute/path");
        assertEquals("/absolute/path", entry.getName());
    }

    // Tests round-trip writeEntryHeader / parseTarHeader with common fields
    @Test
    public void testRoundTripHeaderParse() throws IOException {
        TarArchiveEntry original = new TarArchiveEntry("testfile.txt");
        original.setMode(0644);
        original.setUserId(1000);
        original.setGroupId(100);
        original.setSize(2048);
        original.setModTime(1000000000L);
        original.setUserName("auser");
        original.setGroupName("agroup");
        original.setDevMajor(1);
        original.setDevMinor(2);

        byte[] header = new byte[512];
        original.writeEntryHeader(header);

        TarArchiveEntry parsed = new TarArchiveEntry(header);
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getMode(), parsed.getMode());
        assertEquals(original.getLongUserId(), parsed.getLongUserId());
        assertEquals(original.getLongGroupId(), parsed.getLongGroupId());
        assertEquals(original.getSize(), parsed.getSize());
        assertEquals(original.getModTime().getTime() / 1000, parsed.getModTime().getTime() / 1000);
        assertEquals(original.getUserName(), parsed.getUserName());
        assertEquals(original.getGroupName(), parsed.getGroupName());
        assertEquals(original.getDevMajor(), parsed.getDevMajor());
        assertEquals(original.getDevMinor(), parsed.getDevMinor());
        assertTrue(parsed.isCheckSumOK());
    }

    // Tests parsing of GNU format header (set via LF_GNUTYPE_LONGNAME)
    @Test
    public void testGnuFormatParsing() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("gnufile", TarArchiveEntry.LF_GNUTYPE_LONGNAME);
        byte[] header = new byte[512];
        entry.writeEntryHeader(header);
        TarArchiveEntry parsed = new TarArchiveEntry(header);
        assertEquals("gnufile", parsed.getName());
        assertTrue(parsed.isGNULongNameEntry());
        assertFalse(parsed.isGNULongLinkEntry());
        assertTrue(parsed.isCheckSumOK());
    }

    // Tests that isCheckSumOK() returns false for a corrupt header
    @Test
    public void testParseHeaderInvalidChecksum() {
        byte[] header = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.writeEntryHeader(header);
        // Corrupt the name field so the checksum changes
        header[0] = (byte) 'X';
        TarArchiveEntry parsed = new TarArchiveEntry(header);
        assertFalse(parsed.isCheckSumOK());
    }

    // Tests getDirectoryEntries returns empty array when file is null
    @Test
    public void testGetDirectoryEntriesEmptyWhenFileNull() {
        TarArchiveEntry entry = new TarArchiveEntry("somefile");
        assertNotNull(entry.getDirectoryEntries());
        assertEquals(0, entry.getDirectoryEntries().length);
    }

    // Tests isOldGNUSparse for an entry with LF_GNUTYPE_SPARSE
    @Test
    public void testIsOldGNUSparse() {
        TarArchiveEntry entry = new TarArchiveEntry("sparse", TarArchiveEntry.LF_GNUTYPE_SPARSE);
        assertTrue(entry.isOldGNUSparse());
        assertTrue(entry.isGNUSparse());
        assertFalse(entry.isPaxGNUSparse());
        assertFalse(entry.isStarSparse());
        assertTrue(entry.isSparse());
    }
}