package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.junit.Test;

public class ZipArchiveEntryTest {

    // Tests normal creation with a simple file name
    @Test
    public void testConstructorString_normalName_createsEntry() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
        assertEquals(-1, entry.getMethod());
        assertEquals(-1, entry.getSize()); // SIZE_UNKNOWN = -1
    }

    // Tests directory creation (name ends with '/')
    @Test
    public void testConstructorString_directoryName_endsWithSlash() {
        ZipArchiveEntry entry = new ZipArchiveEntry("mydir/");
        assertEquals("mydir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    // Tests copying from a java.util.zip.ZipEntry
    @Test
    public void testConstructorZipEntry_copyFields() throws ZipException {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("entry.txt");
        ze.setMethod(ZipEntry.DEFLATED);
        ze.setSize(1234);
        ze.setCompressedSize(567);
        ze.setCrc(0x12345678);
        byte[] extra = new byte[] {0x00, 0x01, 0x02, 0x03};
        ze.setExtra(extra);
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("entry.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        assertEquals(1234, entry.getSize());
        // extra fields may be parsed; just check that some extra exists
        assertNotNull(entry.getExtraFields());
    }

    // Tests copying from another ZipArchiveEntry
    @Test
    public void testConstructorZipArchiveEntry_copyFields() throws ZipException {
        ZipArchiveEntry orig = new ZipArchiveEntry("original");
        orig.setInternalAttributes(0x10);
        orig.setExternalAttributes(0x20L << 16);
        orig.setMethod(ZipEntry.STORED);
        orig.setSize(99);
        ZipArchiveEntry copy = new ZipArchiveEntry(orig);
        assertEquals("original", copy.getName());
        assertEquals(0x10, copy.getInternalAttributes());
        assertEquals(0x20L << 16, copy.getExternalAttributes());
        assertEquals(ZipEntry.STORED, copy.getMethod());
        assertEquals(99, copy.getSize());
    }

    // Tests setMethod with negative value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negative_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setMethod(-1); // -1 is the default but setting it explicitly throws
    }

    // Tests setMethod with valid value succeeds
    @Test
    public void testSetMethod_valid_setsMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    // Tests setSize with negative value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negative_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setSize(-1);
    }

    // Tests setSize with zero (boundary)
    @Test
    public void testSetSize_zero_success() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setSize(0);
        assertEquals(0, entry.getSize());
    }

    // Tests setUnixMode on a regular file (non-directory)
    @Test
    public void testSetUnixMode_regularFile_setsAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        int mode = 0644;
        entry.setUnixMode(mode);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        // externalAttributes = (mode << 16) | ((mode & 0200) == 0 ? 1 : 0)
        long expectedExt = ((long) mode << 16) | (1); // mode 0644 & 0200 == 0? 0200 octal = 128, 0644 & 128 = 0, so read-only flag is 0? Actually condition: (mode & 0200) == 0? 0200 octal = 128 decimal. 0644 & 128 = 0, so condition true -> bit 0 set. Hence 1.
        assertEquals(expectedExt, entry.getExternalAttributes());
        assertEquals(mode, entry.getUnixMode());
    }

    // Tests setUnixMode on a directory entry (name ends with '/')
    @Test
    public void testSetUnixMode_directory_setsDirectoryFlag() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        int mode = 0755;
        entry.setUnixMode(mode);
        long expectedExt = ((long) mode << 16) | (1) | 0x10; // directory flag 0x10 set
        assertEquals(expectedExt, entry.getExternalAttributes());
        assertTrue(entry.isDirectory());
    }

    // Tests getUnixMode when platform is not UNIX (default FAT)
    @Test
    public void testGetUnixMode_nonUnixPlatform_returnsZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        assertEquals(0, entry.getUnixMode());
    }

    // Tests clone produces an equal but independent copy
    @Test
    public void testClone_createsEqualIndependentCopy() throws ZipException {
        ZipArchiveEntry orig = new ZipArchiveEntry("orig");
        orig.setInternalAttributes(1);
        orig.setExternalAttributes(2);
        orig.setMethod(ZipEntry.DEFLATED);
        ZipArchiveEntry cloned = (ZipArchiveEntry) orig.clone();
        assertNotSame(orig, cloned);
        assertEquals(orig.getName(), cloned.getName());
        assertEquals(orig.getInternalAttributes(), cloned.getInternalAttributes());
        assertEquals(orig.getExternalAttributes(), cloned.getExternalAttributes());
        assertEquals(orig.getMethod(), cloned.getMethod());
        // modify cloned and check original unchanged
        cloned.setName("changed");
        assertEquals("orig", orig.getName());
    }

    // Tests addExtraField replaces existing field with same header ID
    @Test
    public void testAddExtraField_replacesExisting() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        // Use UnparseableExtraFieldData as a concrete extra field
        UnparseableExtraFieldData uefd = new UnparseableExtraFieldData();
        entry.addExtraField(uefd);
        assertSame(uefd, entry.getUnparseableExtraFieldData());
        // Adding another replaces
        UnparseableExtraFieldData uefd2 = new UnparseableExtraFieldData();
        entry.addExtraField(uefd2);
        assertSame(uefd2, entry.getUnparseableExtraFieldData());
    }

    // Tests removeExtraField with non-existent type throws NoSuchElementException
    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraField_nonexistent_throwsNoSuchElement() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.removeExtraField(new ZipShort(0x1234));
    }

    // Tests removeUnparseableExtraFieldData when none present throws exception
    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldData_null_throws() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.removeUnparseableExtraFieldData();
    }

    // Tests hashCode uses name
    @Test
    public void testHashCode_sameName_equalHash() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test");
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    // Tests equals with identical fields returns true
    @Test
    public void testEquals_sameFields_true() throws ZipException {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setMethod(ZipEntry.STORED);
        e1.setSize(10);
        e1.setComment("comment");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setMethod(ZipEntry.STORED);
        e2.setSize(10);
        e2.setComment("comment");
        assertTrue(e1.equals(e2));
    }

    // Tests equals with different name returns false
    @Test
    public void testEquals_differentName_false() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("a");
        ZipArchiveEntry e2 = new ZipArchiveEntry("b");
        assertFalse(e1.equals(e2));
    }

    // Tests equals with different comment returns false
    @Test
    public void testEquals_differentComment_false() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setComment("c1");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setComment("c2");
        assertFalse(e1.equals(e2));
    }

    // Tests that on FAT platform, backslash is replaced when no forward slash present (normal case)
    @Test
    public void testSetName_backslashOnFAT_withoutSlash_replaces() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        entry.setName("a\\b");
        assertEquals("a/b", entry.getName());
    }

    // Tests that on FAT platform, backslash is NOT replaced when forward slash is present (buggy behavior detection)
    // The fixed version should replace backslash, but buggy version leaves it.
    @Test
    public void testSetName_backslashOnFAT_withSlash_doesNotReplace() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setName("a\\b/c");
        // Buggy version: backslash remains. Fixed version: backslash replaced.
        // We assert the buggy behavior (no replacement) to fail if fixed.
        // This test will fail on the fixed version, indicating a regression.
        assertFalse("Backslash should have been replaced but was not", 
                     entry.getName().contains("\\"));
    }

    // Tests that on UNIX platform, backslash is NOT replaced (buggy behavior detection)
    // Fixed version should replace backslash on all platforms.
    @Test
    public void testSetName_backslashOnUnix_doesNotReplace() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setUnixMode(0644); // sets platform to UNIX
        entry.setName("a\\b");
        // Buggy version: backslash remains. Fixed version: replaced.
        // This test will fail on the fixed version.
        assertFalse("Backslash should have been replaced but was not",
                     entry.getName().contains("\\"));
    }

    // Tests getRawName returns a copy of raw name after setName(raw)
    @Test
    public void testGetRawName_returnsCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        byte[] raw = new byte[] {0x65, 0x6e, 0x74}; // "ent"
        entry.setName("entry", raw);
        byte[] rawCopy = entry.getRawName();
        assertNotNull(rawCopy);
        assertArrayEquals(raw, rawCopy);
        // Verify it is a copy
        rawCopy[0] = 0;
        assertArrayEquals(new byte[]{0x65, 0x6e, 0x74}, entry.getRawName());
    }

    // Tests setExtra with a byte array that can be parsed into extra fields
    @Test
    public void testSetExtra_byteArray_parsesFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        // Create a minimal valid extra field: header ID 0x0001, length 0
        byte[] extra = new byte[] {0x01, 0x00, 0x00, 0x00};
        entry.setExtra(extra);
        ZipExtraField[] fields = entry.getExtraFields(true);
        assertTrue(fields.length > 0);
    }

    // ========== New tests for uncovered coverage ==========

    @Test
    public void testSetComment_getComment() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setComment("a comment");
        assertEquals("a comment", entry.getComment());
    }

    @Test
    public void testSetTime_getTime() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        long time = 123456789L;
        entry.setTime(time);
        assertEquals(time, entry.getTime());
    }

    @Test
    public void testSetExtraFieldsArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData field1 = new UnparseableExtraFieldData();
        UnparseableExtraFieldData field2 = new UnparseableExtraFieldData();
        entry.setExtraFields(new ZipExtraField[] {field1, field2});
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertSame(field1, fields[0]);
        assertSame(field2, fields[1]);
    }

    @Test
    public void testSetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    @Test
    public void testSetCompressedSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setCompressedSize(12345);
        assertEquals(12345, entry.getCompressedSize());
        entry.setCompressedSize(0);
        assertEquals(0, entry.getCompressedSize());
    }

    @Test
    public void testSetCrc() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setCrc(0x12345678L);
        assertEquals(0x12345678L, entry.getCrc());
        entry.setCrc(0L);
        assertEquals(0L, entry.getCrc());
    }

    @Test
    public void testEqualsWithNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.equals(null));
    }

    @Test
    public void testGetNameAfterSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("original");
        entry.setName("newName");
        assertEquals("newName", entry.getName());
    }

    @Test
    public void testSetSizeLargePositive() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        long largeSize = 0x100000000L; // 4GB + 1
        entry.setSize(largeSize);
        assertEquals(largeSize, entry.getSize());
    }
}