package org.apache.commons.compress.archivers.tar;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarArchiveEntryTest {

    // Tests that a PAX header entry ending with slash is not recognized as a directory
    @Test
    public void testIsDirectory_paxHeaderEndingWithSlash_returnsFalse() {
        final TarArchiveEntry entry = new TarArchiveEntry("pax_header/", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        assertTrue(entry.isPaxHeader());
        assertFalse(entry.isDirectory());
    }

    // Tests that a Global PAX header entry ending with slash is not recognized as a directory
    @Test
    public void testIsDirectory_globalPaxHeaderEndingWithSlash_returnsFalse() {
        final TarArchiveEntry entry = new TarArchiveEntry("global_pax_header/", TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);
        assertTrue(entry.isGlobalPaxHeader());
        assertFalse(entry.isDirectory());
    }

    // Tests normal directory detection by name suffix
    @Test
    public void testIsDirectory_nameEndingWithSlash_returnsTrue() {
        final TarArchiveEntry entry = new TarArchiveEntry("foo/bar/");
        assertTrue(entry.isDirectory());
        assertFalse(entry.isFile());
        assertEquals(TarConstants.DEFAULT_DIR_MODE, entry.getMode());
        assertEquals(TarConstants.LF_DIR, entry.getLinkName().isEmpty() ? TarConstants.LF_DIR : 0);
    }

    // Tests normal file detection
    @Test
    public void testIsFile_regularName_returnsTrue() {
        final TarArchiveEntry entry = new TarArchiveEntry("foo/bar.txt");
        assertFalse(entry.isDirectory());
        assertTrue(entry.isFile());
        assertEquals(TarConstants.DEFAULT_FILE_MODE, entry.getMode());
    }

    // Tests file types: symbolic link, hard link, character device, block device, fifo
    @Test
    public void testFileTypes_variousLinkFlags_returnsCorrectType() {
        final TarArchiveEntry symlink = new TarArchiveEntry("symlink", TarConstants.LF_SYMLINK);
        assertTrue(symlink.isSymbolicLink());
        assertFalse(symlink.isFile());

        final TarArchiveEntry link = new TarArchiveEntry("link", TarConstants.LF_LINK);
        assertTrue(link.isLink());

        final TarArchiveEntry chr = new TarArchiveEntry("chr", TarConstants.LF_CHR);
        assertTrue(chr.isCharacterDevice());

        final TarArchiveEntry blk = new TarArchiveEntry("blk", TarConstants.LF_BLK);
        assertTrue(blk.isBlockDevice());

        final TarArchiveEntry fifo = new TarArchiveEntry("fifo", TarConstants.LF_FIFO);
        assertTrue(fifo.isFIFO());
    }

    // Tests GNU long name and long link flags
    @Test
    public void testGnuEntries_longNameAndLink_identifiedCorrectly() {
        final TarArchiveEntry longName = new TarArchiveEntry("longname", TarConstants.LF_GNUTYPE_LONGNAME);
        assertTrue(longName.isGNULongNameEntry());

        final TarArchiveEntry longLink = new TarArchiveEntry("longlink", TarConstants.LF_GNUTYPE_LONGLINK);
        assertTrue(longLink.isGNULongLinkEntry());
    }

    // Tests sparse flags and sparse helper methods
    @Test
    public void testSparse_oldGnuAndPaxSparse_identifiedCorrectly() {
        final TarArchiveEntry oldGnu = new TarArchiveEntry("sparse", TarConstants.LF_GNUTYPE_SPARSE);
        assertTrue(oldGnu.isOldGNUSparse());
        assertTrue(oldGnu.isGNUSparse());
        assertTrue(oldGnu.isSparse());

        final TarArchiveEntry paxSparse = new TarArchiveEntry("pax_sparse");
        final Map<String, String> sparseHeaders = new HashMap<String, String>();
        sparseHeaders.put("GNU.sparse.size", "1024");
        sparseHeaders.put("GNU.sparse.name", "sparse_file");
        paxSparse.fillGNUSparse0xData(sparseHeaders);
        assertTrue(paxSparse.isPaxGNUSparse());
        assertTrue(paxSparse.isGNUSparse());
        assertTrue(paxSparse.isSparse());
        assertEquals(1024, paxSparse.getRealSize());
        assertEquals("sparse_file", paxSparse.getName());

        final TarArchiveEntry starSparse = new TarArchiveEntry("star_sparse");
        final Map<String, String> starHeaders = new HashMap<String, String>();
        starHeaders.put("SCHILY.realsize", "2048");
        starSparse.fillStarSparseData(starHeaders);
        assertTrue(starSparse.isStarSparse());
        assertTrue(starSparse.isSparse());
        assertEquals(2048, starSparse.getRealSize());
    }

    // Tests leading slash normalization
    @Test
    public void testNormalizeFileName_leadingSlashes_strippedByDefault() {
        final TarArchiveEntry entryWithoutLeading = new TarArchiveEntry("///foo/bar.txt");
        assertEquals("foo/bar.txt", entryWithoutLeading.getName());

        final TarArchiveEntry entryPreserved = new TarArchiveEntry("/foo/bar.txt", true);
        assertEquals("/foo/bar.txt", entryPreserved.getName());
    }

    // Tests negative size argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negativeValue_throwsException() {
        final TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setSize(-1);
    }

    // Tests negative major device number throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDevMajor_negativeValue_throwsException() {
        final TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setDevMajor(-1);
    }

    // Tests negative minor device number throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDevMinor_negativeValue_throwsException() {
        final TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.setDevMinor(-1);
    }

    // Tests setters and getters for entry properties
    @Test
    public void testSettersAndGetters_validValues_returnsCorrectValues() {
        final TarArchiveEntry entry = new TarArchiveEntry("initial");
        entry.setName("newName");
        assertEquals("newName", entry.getName());

        entry.setMode(0644);
        assertEquals(0644, entry.getMode());

        entry.setLinkName("targetLink");
        assertEquals("targetLink", entry.getLinkName());

        entry.setUserId(1001);
        assertEquals(1001, entry.getUserId());
        assertEquals(1001L, entry.getLongUserId());

        entry.setGroupId(2002);
        assertEquals(2002, entry.getGroupId());
        assertEquals(2002L, entry.getLongGroupId());

        entry.setIds(10, 20);
        assertEquals(10, entry.getUserId());
        assertEquals(20, entry.getGroupId());

        entry.setUserName("testUser");
        assertEquals("testUser", entry.getUserName());

        entry.setGroupName("testGroup");
        assertEquals("testGroup", entry.getGroupName());

        entry.setNames("user2", "group2");
        assertEquals("user2", entry.getUserName());
        assertEquals("group2", entry.getGroupName());

        entry.setSize(4096L);
        assertEquals(4096L, entry.getSize());

        entry.setDevMajor(5);
        assertEquals(5, entry.getDevMajor());

        entry.setDevMinor(9);
        assertEquals(9, entry.getDevMinor());

        final Date now = new Date(1000000000L);
        entry.setModTime(now);
        assertEquals(now.getTime() / 1000 * 1000, entry.getModTime().getTime());
        assertEquals(entry.getModTime(), entry.getLastModifiedDate());

        entry.setModTime(2000000000L);
        assertEquals(2000000000L / 1000 * 1000, entry.getModTime().getTime());
    }

    // Tests equals, hashCode, and isDescendent methods
    @Test
    public void testEqualsAndHashCodeAndIsDescendent_validEntries_returnsExpectedResults() {
        final TarArchiveEntry entry1 = new TarArchiveEntry("dir/file.txt");
        final TarArchiveEntry entry2 = new TarArchiveEntry("dir/file.txt");
        final TarArchiveEntry entry3 = new TarArchiveEntry("dir/other.txt");
        final TarArchiveEntry parent = new TarArchiveEntry("dir/");

        assertTrue(entry1.equals(entry2));
        assertTrue(entry1.equals((Object) entry2));
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals((Object) null));
        assertFalse(entry1.equals("nonEntryObject"));

        assertEquals(entry1.hashCode(), entry2.hashCode());

        assertTrue(parent.isDescendent(entry1));
        assertFalse(entry1.isDescendent(parent));
        assertFalse(entry1.isDescendent(null));
    }

    // Tests serialization and parsing roundtrip of header bytes
    @Test
    public void testWriteAndParseTarHeader_standardEntry_preservesAttributes() {
        final TarArchiveEntry entry = new TarArchiveEntry("testdir/testfile.txt");
        entry.setMode(0755);
        entry.setUserId(500);
        entry.setGroupId(600);
        entry.setSize(12345L);
        entry.setModTime(1400000000000L);
        entry.setUserName("alice");
        entry.setGroupName("staff");

        final byte[] header = new byte[512];
        entry.writeEntryHeader(header);

        final TarArchiveEntry parsedEntry = new TarArchiveEntry(header);
        assertEquals("testdir/testfile.txt", parsedEntry.getName());
        assertEquals(0755, parsedEntry.getMode());
        assertEquals(500, parsedEntry.getUserId());
        assertEquals(600, parsedEntry.getGroupId());
        assertEquals(12345L, parsedEntry.getSize());
        assertEquals(1400000000000L / 1000 * 1000, parsedEntry.getModTime().getTime());
        assertEquals("alice", parsedEntry.getUserName());
        assertEquals("staff", parsedEntry.getGroupName());
    }

    // Tests file-based constructor and getDirectoryEntries
    @Test
    public void testFileConstructor_tempFile_populatesAttributesCorrectly() throws IOException {
        final File tempFile = File.createTempFile("tartest", ".tmp");
        tempFile.deleteOnExit();

        final TarArchiveEntry entry = new TarArchiveEntry(tempFile);
        assertNotNull(entry.getFile());
        assertTrue(entry.isFile());
        assertFalse(entry.isDirectory());
        assertEquals(tempFile.length(), entry.getSize());
        assertEquals(0, entry.getDirectoryEntries().length);

        final File tempDir = tempFile.getParentFile();
        if (tempDir != null && tempDir.isDirectory()) {
            final TarArchiveEntry dirEntry = new TarArchiveEntry(tempDir);
            assertTrue(dirEntry.isDirectory());
            assertTrue(dirEntry.getName().endsWith("/"));
            final TarArchiveEntry[] children = dirEntry.getDirectoryEntries();
            assertNotNull(children);
        }
    }

    // Tests File constructor with explicit entry name
    @Test
    public void testFileConstructor_withExplicitEntryName_usesProvidedName() throws IOException {
        final File tempFile = File.createTempFile("tartest_explicit", ".tmp");
        tempFile.deleteOnExit();

        final TarArchiveEntry entry = new TarArchiveEntry(tempFile, "custom/path/file.txt");
        assertEquals("custom/path/file.txt", entry.getName());
        assertEquals(tempFile.length(), entry.getSize());
        assertTrue(entry.isFile());
    }

    // Tests constructor with link flag and preserveLeadingSlashes flag
    @Test
    public void testConstructor_withLinkFlagAndPreserveLeadingSlashes_preservesSlash() {
        final TarArchiveEntry entry = new TarArchiveEntry("/symlink", TarConstants.LF_SYMLINK, true);
        assertEquals("/symlink", entry.getName());
        assertTrue(entry.isSymbolicLink());

        final TarArchiveEntry entryStripped = new TarArchiveEntry("/symlink", TarConstants.LF_SYMLINK, false);
        assertEquals("symlink", entryStripped.getName());
        assertTrue(entryStripped.isSymbolicLink());
    }

    // Tests extra PAX header management methods
    @Test
    public void testExtraPaxHeaders_addGetClear_worksCorrectly() {
        final TarArchiveEntry entry = new TarArchiveEntry("pax_test.txt");
        entry.addPaxHeader("path", "custom_path");
        entry.addPaxHeader("comment", "test comment");

        assertEquals("custom_path", entry.getExtraPaxHeader("path"));
        assertEquals("test comment", entry.getExtraPaxHeader("comment"));
        assertEquals(2, entry.getExtraPaxHeaders().size());

        entry.clearExtraPaxHeaders();
        assertNull(entry.getExtraPaxHeader("path"));
        assertEquals(0, entry.getExtraPaxHeaders().size());
    }

    // Tests GNU sparse 1.x header parsing
    @Test
    public void testSparse_gnuSparse1xHeaders_parsedCorrectly() {
        final TarArchiveEntry entry = new TarArchiveEntry("gnu1x_sparse");
        final Map<String, String> sparseHeaders = new HashMap<String, String>();
        sparseHeaders.put("GNU.sparse.realsize", "8192");
        sparseHeaders.put("GNU.sparse.major", "1");
        sparseHeaders.put("GNU.sparse.minor", "0");
        entry.fillGNUSparse1xData(sparseHeaders);

        assertTrue(entry.isPaxGNUSparse());
        assertTrue(entry.isGNUSparse());
        assertTrue(entry.isSparse());
        assertEquals(8192L, entry.getRealSize());
    }

    // Tests long user ID and group ID setters
    @Test
    public void testLongUserIdAndGroupId_largeValues_storedCorrectly() {
        final TarArchiveEntry entry = new TarArchiveEntry("user_group_test");
        entry.setUserId(4294967296L);
        entry.setGroupId(8589934592L);

        assertEquals(4294967296L, entry.getLongUserId());
        assertEquals(8589934592L, entry.getLongGroupId());
    }
}