package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.junit.Test;

public class ZipArchiveEntryTest {

    // Tests constructor with null name
    @Test
    public void testConstructorString_nullName_createsEntry() {
        ZipArchiveEntry entry = new ZipArchiveEntry((String) null);
        assertNotNull(entry);
        assertEquals(null, entry.getName());
    }

    // Tests constructor with empty string name
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorString_emptyName_throwsException() {
        new ZipArchiveEntry("");
    }

    // Tests constructor with valid name
    @Test
    public void testConstructorString_validName_createsEntry() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
    }

    // Tests constructor from ZipEntry with extra data
    @Test
    public void testConstructorZipEntry_withExtraData_parsesExtraFields() throws ZipException {
        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry("test.txt");
        zipEntry.setMethod(ZipEntry.DEFLATED);
        zipEntry.setExtra(new byte[] {0, 0, 0, 0}); // empty extra field, not parseable
        ZipArchiveEntry entry = new ZipArchiveEntry(zipEntry);
        assertEquals("test.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    // Tests constructor from ZipEntry with null extra data
    @Test
    public void testConstructorZipEntry_nullExtra_setsExtra() throws ZipException {
        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry("test.txt");
        zipEntry.setMethod(ZipEntry.STORED);
        ZipArchiveEntry entry = new ZipArchiveEntry(zipEntry);
        assertEquals("test.txt", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    // Tests constructor from ZipArchiveEntry
    @Test
    public void testConstructorZipArchiveEntry_copiesFields() throws ZipException {
        ZipArchiveEntry original = new ZipArchiveEntry("test.txt");
        original.setInternalAttributes(1);
        original.setExternalAttributes(2L);
        original.setMethod(ZipEntry.DEFLATED);
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getMethod(), copy.getMethod());
        assertEquals(original.getInternalAttributes(), copy.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), copy.getExternalAttributes());
    }

    // Tests constructor from File and entryName for directory
    @Test
    public void testConstructorFileEntryName_directory_appendsSlash() {
        java.io.File dir = new java.io.File("mydir");
        ZipArchiveEntry entry = new ZipArchiveEntry(dir, "mydir");
        assertEquals("mydir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    // Tests clone method
    @Test
    public void testClone_returnsCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        entry.setInternalAttributes(3);
        entry.setExternalAttributes(4L);
        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        assertEquals(entry.getName(), cloned.getName());
        assertEquals(entry.getMethod(), cloned.getMethod());
        assertEquals(entry.getInternalAttributes(), cloned.getInternalAttributes());
        assertEquals(entry.getExternalAttributes(), cloned.getExternalAttributes());
    }

    // Tests isSupportedCompressionMethod with STORED
    @Test
    public void testIsSupportedCompressionMethod_stored_returnsTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.STORED);
        assertTrue(entry.isSupportedCompressionMethod());
    }

    // Tests isSupportedCompressionMethod with DEFLATED
    @Test
    public void testIsSupportedCompressionMethod_deflated_returnsTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        assertTrue(entry.isSupportedCompressionMethod());
    }

    // Tests isSupportedCompressionMethod with unsupported method
    @Test
    public void testIsSupportedCompressionMethod_unsupported_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(8); // unknown method
        assertFalse(entry.isSupportedCompressionMethod());
    }

    // Tests isSupportedCompressionMethod with default method (-1)
    @Test
    public void testIsSupportedCompressionMethod_default_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(-1, entry.getMethod());
        assertFalse(entry.isSupportedCompressionMethod());
    }

    // Tests setMethod with negative value
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negativeValue_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(-1);
    }

    // Tests setMethod with zero
    @Test
    public void testSetMethod_zeroValue_setsMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(0);
        assertEquals(0, entry.getMethod());
    }

    // Tests setUnixMode and getUnixMode
    @Test
    public void testSetUnixMode_validMode_setsPlatformAndAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        int mode = entry.getUnixMode();
        assertEquals(0755, mode & 0777); // check permission bits
    }

    // Tests getUnixMode when platform is not UNIX
    @Test
    public void testGetUnixMode_platformFAT_returnsZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        assertEquals(0, entry.getUnixMode());
    }

    // Tests setExtraFields with null array
    @Test
    public void testSetExtraFields_nullArray_clearsFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setExtraFields(null);
        assertEquals(0, entry.getExtraFields().length);
    }

    // Tests getExtraFields when extraFields is null
    @Test
    public void testGetExtraFields_nullExtraFields_returnsEmptyArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        // extraFields is null initially
        ZipExtraField[] fields = entry.getExtraFields();
        assertNotNull(fields);
        assertEquals(0, fields.length);
    }

    // Tests addExtraField when extraFields is null
    @Test
    public void testAddExtraField_initialNull_createsMap() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ZipShort headerId = new ZipShort(0x0001);
        ZipExtraField field = new UnparseableExtraFieldData();
        // set header id via reflection? Not needed, just test behavior
        // Instead, use a concrete extra field implementation if possible, but assume test behavior
        // Since we cannot import other extra fields easily, we test that addExtraField does not throw
        // and extraFields becomes non-null
        // We can use a simple mock-like approach with anonymous class
        ZipExtraField mockField = new ZipExtraField() {
            public ZipShort getHeaderId() { return headerId; }
            public ZipShort getLocalFileDataLength() { return new ZipShort(0); }
            public ZipShort getCentralDirectoryLength() { return new ZipShort(0); }
            public byte[] getLocalFileDataData() { return new byte[0]; }
            public byte[] getCentralDirectoryData() { return new byte[0]; }
            public void parseFromLocalFileData(byte[] data, int offset, int length) {}
            public void parseFromCentralDirectoryData(byte[] data, int offset, int length) {}
        };
        entry.addExtraField(mockField);
        assertNotNull(entry.getExtraField(headerId));
    }

    // Tests removeExtraField with existing field
    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraField_nonexistent_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ZipShort headerId = new ZipShort(0x0001);
        entry.removeExtraField(headerId);
    }

    // Tests isDirectory with name ending with /
    @Test
    public void testIsDirectory_nameEndsWithSlash_returnsTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
    }

    // Tests isDirectory with name not ending with /
    @Test
    public void testIsDirectory_nameNoSlash_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        assertFalse(entry.isDirectory());
    }

    // Tests equals with same name
    @Test
    public void testEquals_sameName_returnsTrue() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test.txt");
        assertTrue(entry1.equals(entry2));
    }

    // Tests equals with different name
    @Test
    public void testEquals_differentName_returnsFalse() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("other.txt");
        assertFalse(entry1.equals(entry2));
    }

    // Tests equals with null
    @Test
    public void testEquals_null_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertFalse(entry.equals(null));
    }

    // Tests hashCode consistency with name
    @Test
    public void testHashCode_consistentWithName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt".hashCode(), entry.hashCode());
    }

    // ========== Additional tests for uncovered methods ==========

    // Tests setComment and getComment
    @Test
    public void testSetCommentAndGetComment() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setComment("a comment");
        assertEquals("a comment", entry.getComment());
    }

    // Tests setSize and getSize
    @Test
    public void testSetSizeAndGetSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(12345L);
        assertEquals(12345L, entry.getSize());
    }

    // Tests setCompressedSize and getCompressedSize
    @Test
    public void testSetCompressedSizeAndGetCompressedSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setCompressedSize(6789L);
        assertEquals(6789L, entry.getCompressedSize());
    }

    // Tests setCrc and getCrc
    @Test
    public void testSetCrcAndGetCrc() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setCrc(0x12345678L);
        assertEquals(0x12345678L, entry.getCrc());
    }

    // Tests setTime and getTime (deprecated but still works)
    @Test
    public void testSetTimeAndGetTime() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        long time = 1234567890000L;
        entry.setTime(time);
        assertEquals(time, entry.getTime());
    }

    // Tests setLastModifiedTime and getLastModifiedTime
    @Test
    public void testSetLastModifiedTimeAndGetLastModifiedTime() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        java.nio.file.attribute.FileTime fileTime = java.nio.file.attribute.FileTime.fromMillis(1234567890000L);
        entry.setLastModifiedTime(fileTime);
        assertEquals(fileTime, entry.getLastModifiedTime());
    }

    // Tests setGeneralPurposeBit and getGeneralPurposeBit
    @Test
    public void testSetGeneralPurposeBitAndGetGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForName(true);
        entry.setGeneralPurposeBit(gpb);
        assertSame(gpb, entry.getGeneralPurposeBit());
    }

    // Tests setLocalHeaderOffset and getLocalHeaderOffset
    @Test
    public void testSetLocalHeaderOffsetAndGetLocalHeaderOffset() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setLocalHeaderOffset(12345L);
        assertEquals(12345L, entry.getLocalHeaderOffset());
    }

    // Tests removeExtraField with existing field
    @Test
    public void testRemoveExtraField_existingField_removes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ZipShort headerId = new ZipShort(0x0001);
        ZipExtraField mockField = new ZipExtraField() {
            public ZipShort getHeaderId() { return headerId; }
            public ZipShort getLocalFileDataLength() { return new ZipShort(0); }
            public ZipShort getCentralDirectoryLength() { return new ZipShort(0); }
            public byte[] getLocalFileDataData() { return new byte[0]; }
            public byte[] getCentralDirectoryData() { return new byte[0]; }
            public void parseFromLocalFileData(byte[] data, int offset, int length) {}
            public void parseFromCentralDirectoryData(byte[] data, int offset, int length) {}
        };
        entry.addExtraField(mockField);
        entry.removeExtraField(headerId);
        assertNull(entry.getExtraField(headerId));
    }

    // Tests addAsFirstExtraField
    @Test
    public void testAddAsFirstExtraField_insertsFirst() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ZipShort headerId1 = new ZipShort(0x0001);
        ZipShort headerId2 = new ZipShort(0x0002);
        ZipExtraField field1 = createMockExtraField(headerId1);
        ZipExtraField field2 = createMockExtraField(headerId2);
        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);
        // field2 should be first; retrieve all fields and check order
        ZipExtraField[] fields = entry.getExtraFields();
        assertSame(field2, fields[0]);
        assertSame(field1, fields[1]);
    }

    // Helper method to create mock extra field
    private ZipExtraField createMockExtraField(final ZipShort headerId) {
        return new ZipExtraField() {
            public ZipShort getHeaderId() { return headerId; }
            public ZipShort getLocalFileDataLength() { return new ZipShort(0); }
            public ZipShort getCentralDirectoryLength() { return new ZipShort(0); }
            public byte[] getLocalFileDataData() { return new byte[0]; }
            public byte[] getCentralDirectoryData() { return new byte[0]; }
            public void parseFromLocalFileData(byte[] data, int offset, int length) {}
            public void parseFromCentralDirectoryData(byte[] data, int offset, int length) {}
        };
    }
}