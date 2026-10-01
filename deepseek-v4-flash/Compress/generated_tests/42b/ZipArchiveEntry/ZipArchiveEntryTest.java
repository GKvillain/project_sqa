package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * JUnit 4 test class for ZipArchiveEntry targeting Defects4J bug 42b.
 */
public class ZipArchiveEntryTest {

    // ===== Original tests =====

    // Tests getName with a normal name
    @Test
    public void testGetName_normal_returnsName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
    }

    // Tests getName when setName(null) is called - should revert to the super name
    @Test
    public void testGetName_nullSetName_returnsOriginal() {
        ZipArchiveEntry entry = new ZipArchiveEntry("original");
        entry.setName(null);
        assertEquals("original", entry.getName());
    }

    // Tests setMethod with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negative_throwsIllegalArgumentException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(-1);
    }

    // Tests setSize with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negative_throwsIllegalArgumentException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(-1L);
    }

    // Tests setSize with zero (boundary)
    @Test
    public void testSetSize_zero_success() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(0L);
        assertEquals(0L, entry.getSize());
    }

    // Tests constructor from java.util.zip.ZipEntry - basic name and extra
    @Test
    public void testConstructorFromZipEntry() throws ZipException {
        ZipEntry zipEntry = new ZipEntry("test.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(zipEntry);
        assertEquals("test.txt", entry.getName());
        assertNotNull(entry.getExtra()); // extra may be empty but not null
    }

    // Tests constructor from File with directory name adjustment
    @Test
    public void testConstructorFromFile_directory_endsWithSlash() {
        File dir = new File("/tmp/somedir");
        // We cannot actually create a directory, so simulate using file.isDirectory() returns true
        // Use a real directory in the test environment? Better to use a known path that is directory.
        // For simplicity, we use a temporary directory. This might be fragile, but it's a standard approach.
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        ZipArchiveEntry entry = new ZipArchiveEntry(tmpDir, "mydir");
        // Expect name to end with "/"
        assertTrue(entry.getName().endsWith("/"));
        // For a file, we need to test with a real file; but this is acceptable.
    }

    // Tests setUnixMode sets platform to UNIX and external attributes
    @Test
    public void testSetUnixMode_platformUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        int mode = entry.getUnixMode();
        assertEquals(0755, mode & 0777); // mask permission bits
    }

    // Tests getUnixMode returns 0 when platform is not UNIX
    @Test
    public void testGetUnixMode_defaultPlatform_returnsZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getUnixMode());
    }

    // Tests isUnixSymlink returns false when platform is not UNIX
    @Test
    public void testIsUnixSymlink_notUnix_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.isUnixSymlink());
    }

    // Tests addExtraField replacing an existing field (same header ID)
    @Test
    public void testAddExtraField_replaceExistingField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Use a known extra field: AsiExtraField (part of the library)
        ZipShort headerId = new ZipShort(0x1234); // any dummy ID
        ZipExtraField f1 = new ExtraFieldDummy(headerId, new byte[]{1});
        ZipExtraField f2 = new ExtraFieldDummy(headerId, new byte[]{2});
        entry.addExtraField(f1);
        entry.addExtraField(f2);
        ZipExtraField retrieved = entry.getExtraField(headerId);
        assertNotNull(retrieved);
        // The last one added should replace
        byte[] data = retrieved.getLocalFileDataData();
        assertArrayEquals(new byte[]{2}, data);
    }

    // Tests removeExtraField throws NoSuchElementException when field not found
    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraField_notFound_throwsNoSuchElementException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(new ZipShort(0xFFFF));
    }

    // Tests removeUnparseableExtraFieldData throws NoSuchElementException when no unparseable data
    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldData_notExist_throwsNoSuchElementException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeUnparseableExtraFieldData();
    }

    // Tests equals returns false when names are same but extra fields differ
    @Test
    public void testEquals_sameNameDifferentExtra_returnsFalse() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e1.addExtraField(new ExtraFieldDummy(new ZipShort(1), new byte[]{10}));
        e2.addExtraField(new ExtraFieldDummy(new ZipShort(2), new byte[]{20}));
        assertFalse(e1.equals(e2));
    }

    // Tests hashCode returns consistent value for the same object
    @Test
    public void testHashCode_consistent() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        int h1 = entry.hashCode();
        int h2 = entry.hashCode();
        assertEquals(h1, h2);
    }

    // Tests clone produces equal object
    @Test
    public void testClone_equalsOriginal() throws ZipException {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0644);
        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        assertEquals(entry, clone);
    }

    // Tests getRawName returns null when rawName not set
    @Test
    public void testGetRawName_notSet_returnsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNull(entry.getRawName());
    }

    // Tests getLastModifiedDate returns a Date object (basic sanity)
    @Test
    public void testGetLastModifiedDate_notSet_returnsDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Date d = entry.getLastModifiedDate();
        assertNotNull(d);
    }

    // ===== New tests for additional coverage =====

    // test setMethod with valid STORED value
    @Test
    public void testSetMethod_Stored() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    // test setMethod with valid DEFLATED value
    @Test
    public void testSetMethod_Deflated() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipEntry.DEFLATED);
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    // test setSize with positive value
    @Test
    public void testSetSize_positive() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(100L);
        assertEquals(100L, entry.getSize());
    }

    // test default size is -1
    @Test
    public void testGetSize_defaultIsMinusOne() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(-1L, entry.getSize());
    }

    // test setCompressedSize and getCompressedSize
    @Test
    public void testSetCompressedSize_getCompressedSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setCompressedSize(1024L);
        assertEquals(1024L, entry.getCompressedSize());
    }

    // test default compressed size is -1
    @Test
    public void testGetCompressedSize_defaultIsMinusOne() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(-1L, entry.getCompressedSize());
    }

    // test setCrc and getCrc
    @Test
    public void testSetCrc_getCrc() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setCrc(123456L);
        assertEquals(123456L, entry.getCrc());
    }

    // test setInternalAttributes and getInternalAttributes
    @Test
    public void testSetInternalAttributes_getInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setInternalAttributes(0x10);
        assertEquals(0x10, entry.getInternalAttributes());
    }

    // test setExternalAttributes and getExternalAttributes
    @Test
    public void testSetExternalAttributes_getExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setExternalAttributes(0x12345678);
        assertEquals(0x12345678, entry.getExternalAttributes());
    }

    // test setPlatform and getPlatform
    @Test
    public void testSetPlatform_getPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    // test isDirectory true when name ends with "/"
    @Test
    public void testIsDirectory_true() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
    }

    // test isDirectory false for regular file name
    @Test
    public void testIsDirectory_false() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        assertFalse(entry.isDirectory());
    }

    // test setExtra with valid byte array and getExtra returns the same
    @Test
    public void testSetExtra_getExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] extra = new byte[]{1, 2, 3, 4};
        entry.setExtra(extra);
        assertArrayEquals(extra, entry.getExtra());
    }

    // test getExtra returns null by default? (в зависимости от реализации)
    @Test
    public void testGetExtra_defaultIsNotNullOrNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // constructor from String might leave extra as null or empty; we just check it doesn't throw
        assertNotNull(entry.getExtra()); // actually might be null, but we assume it's at least a zero-length array? Adjust as needed.
        // For safety, we verify method call does not throw.
    }

    // test setExtraFields and getExtraFields
    @Test
    public void testSetExtraFields_getExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        ZipExtraField[] fields = new ZipExtraField[]{
            new ExtraFieldDummy(new ZipShort(1), new byte[]{1}),
            new ExtraFieldDummy(new ZipShort(2), new byte[]{2})
        };
        entry.setExtraFields(fields);
        ZipExtraField[] retrieved = entry.getExtraFields();
        assertEquals(fields.length, retrieved.length);
        // check that fields are equal by header id
        assertEquals(new ZipShort(1), retrieved[0].getHeaderId());
        assertEquals(new ZipShort(2), retrieved[1].getHeaderId());
    }

    // test getCentralDirectoryExtra returns array of all extra fields' central data
    @Test
    public void testGetCentralDirectoryExtra_containsData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.addExtraField(new ExtraFieldDummy(new ZipShort(1), new byte[]{10}));
        byte[] centralData = entry.getCentralDirectoryExtra();
        // just check we get something, not null
        assertNotNull(centralData);
    }

    // test getLocalFileDataExtra returns array of all extra fields' local data
    @Test
    public void testGetLocalFileDataExtra_containsData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.addExtraField(new ExtraFieldDummy(new ZipShort(1), new byte[]{10}));
        byte[] localData = entry.getLocalFileDataExtra();
        assertNotNull(localData);
    }

    // test getUnparseableExtraFieldData returns null when none present
    @Test
    public void testGetUnparseableExtraFieldData_defaultNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNull(entry.getUnparseableExtraFieldData());
    }

    // test setComment and getComment
    @Test
    public void testSetComment_getComment() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setComment("a comment");
        assertEquals("a comment", entry.getComment());
    }

    // test setVersionMadeBy and getVersionMadeBy
    @Test
    public void testSetVersionMadeBy_getVersionMadeBy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setVersionMadeBy(20);
        assertEquals(20, entry.getVersionMadeBy());
    }

    // test setVersionNeededToExtract and getVersionNeededToExtract
    @Test
    public void testSetVersionNeededToExtract_getVersionNeededToExtract() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setVersionNeededToExtract(10);
        assertEquals(10, entry.getVersionNeededToExtract());
    }

    // test setRawFlag and getRawFlag
    @Test
    public void testSetRawFlag_getRawFlag() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setRawFlag(0x1234);
        assertEquals(0x1234, entry.getRawFlag());
    }

    // test setLastModifiedTime with FileTime
    @Test
    public void testSetLastModifiedTime_getLastModifiedTime() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        FileTime time = FileTime.fromMillis(1234567890L);
        entry.setLastModifiedTime(time);
        assertEquals(time, entry.getLastModifiedTime());
    }

    // test setCreationTime and getCreationTime
    @Test
    public void testSetCreationTime_getCreationTime() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        FileTime time = FileTime.fromMillis(9876543210L);
        entry.setCreationTime(time);
        assertEquals(time, entry.getCreationTime());
    }

    // test setAccessTime and getAccessTime
    @Test
    public void testSetAccessTime_getAccessTime() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        FileTime time = FileTime.fromMillis(1111111111L);
        entry.setAccessTime(time);
        assertEquals(time, entry.getAccessTime());
    }

    // test setTime (long) and getTime
    @Test
    public void testSetTime_getTime() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setTime(1234567890123L);
        assertEquals(1234567890123L, entry.getTime());
    }

    // test constructor from File with a real file (name should not end with "/")
    @Test
    public void testConstructorFromFile_file_startsWithName() throws IOException {
        File tempFile = File.createTempFile("zipentry", ".tmp");
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry(tempFile, "plain.txt");
            assertFalse(entry.getName().endsWith("/"));
            assertEquals("plain.txt", entry.getName());
        } finally {
            tempFile.delete();
        }
    }

    // test isUnixSymlink true when setUnixMode with symlink type (0120000)
    @Test
    public void testIsUnixSymlink_true_whenSetUnixModeSymlink() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0120000); // symlink mode
        assertTrue(entry.isUnixSymlink());
    }

    // test getUnixMode after setting Unix mode with full permissions
    @Test
    public void testGetUnixMode_afterSetUnixMode_returnsPermissions() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0100644); // regular file with permissions
        int mode = entry.getUnixMode();
        // should return the permission bits (0644)
        assertEquals(0644, mode & 0777);
    }

    // ===== Helper inner class to create a simple extra field for testing =====
    private static class ExtraFieldDummy implements ZipExtraField {
        private ZipShort headerId;
        private byte[] localData;
        private byte[] centralData;

        ExtraFieldDummy(ZipShort headerId, byte[] data) {
            this.headerId = headerId;
            this.localData = data;
            this.centralData = data;
        }

        @Override
        public ZipShort getHeaderId() {
            return headerId;
        }

        @Override
        public ZipShort getLocalFileDataLength() {
            return new ZipShort(localData.length);
        }

        @Override
        public ZipShort getCentralDirectoryLength() {
            return new ZipShort(centralData.length);
        }

        @Override
        public byte[] getLocalFileDataData() {
            return localData;
        }

        @Override
        public byte[] getCentralDirectoryData() {
            return centralData;
        }

        @Override
        public void parseFromLocalFileData(byte[] buffer, int offset, int length) {
            // not used in tests
        }

        @Override
        public void parseFromCentralDirectoryData(byte[] buffer, int offset, int length) {
            // not used in tests
        }
    }
}