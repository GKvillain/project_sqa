package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;

import org.junit.Test;

public class ZipArchiveEntryTest {

    // ==================== Existing test methods ====================

    @Test
    public void testConstructor_StringName_setsNameAndIsNotDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
    }

    @Test
    public void testConstructor_StringNameEndingWithSlash_isDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("testdir/");
        assertEquals("testdir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    @Test
    public void testConstructor_ZipEntry_createsEntry() throws Exception {
        ZipEntry zipEntry = new ZipEntry("test.txt");
        zipEntry.setMethod(ZipEntry.DEFLATED);
        ZipArchiveEntry entry = new ZipArchiveEntry(zipEntry);
        assertEquals("test.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    @Test
    public void testConstructor_FileAndEntryName_file_setsSize() {
        File file = new File("src/test/resources/test.txt");
        if (!file.exists()) {
            return;
        }
        ZipArchiveEntry entry = new ZipArchiveEntry(file, "test.txt");
        assertEquals("test.txt", entry.getName());
        assertTrue(entry.getSize() >= 0);
    }

    @Test
    public void testConstructor_FileAndEntryName_directory_addsTrailingSlash() {
        File dir = new File("src/test/resources");
        if (!dir.isDirectory()) {
            return;
        }
        ZipArchiveEntry entry = new ZipArchiveEntry(dir, "testdir");
        assertEquals("testdir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negativeValue_throwsIllegalArgumentException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(-1);
    }

    @Test
    public void testSetMethod_positiveValue_setsMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    @Test
    public void testSetMethod_zeroValue_setsMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(0);
        assertEquals(0, entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negativeValue_throwsIllegalArgumentException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(-1);
    }

    @Test
    public void testSetSize_zeroValue_setsSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(0);
        assertEquals(0, entry.getSize());
    }

    @Test
    public void testSetSize_positiveValue_setsSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(100);
        assertEquals(100, entry.getSize());
    }

    @Test
    public void testGetUnixMode_nonUnixPlatform_returnsZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(0, entry.getUnixMode());
    }

    @Test
    public void testGetUnixMode_unixPlatform_returnsMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setUnixMode(0755);
        assertEquals(0755, entry.getUnixMode());
    }

    @Test
    public void testSetUnixMode_setsPlatformToUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setUnixMode(0644);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test
    public void testAddExtraField_addsField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        AsiExtraField field = new AsiExtraField();
        entry.addExtraField(field);
        assertNotNull(entry.getExtraField(field.getHeaderId()));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraField_nonExistentType_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.removeExtraField(new ZipShort(1));
    }

    @Test
    public void testGetExtraFields_noFields_returnsEmptyArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(0, fields.length);
    }

    @Test
    public void testGetExtraFields_includeUnparseable_noUnparseable_returnsOnlyParsed() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ZipExtraField[] fields = entry.getExtraFields(true);
        assertEquals(0, fields.length);
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertTrue(entry.equals(entry));
    }

    @Test
    public void testEquals_nullObject_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertFalse(entry.equals(null));
    }

    @Test
    public void testEquals_differentClass_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertFalse(entry.equals("string"));
    }

    @Test
    public void testEquals_differentName_returnsFalse() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test1.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test2.txt");
        assertFalse(entry1.equals(entry2));
    }

    @Test
    public void testEquals_sameNameAndSameAttributes_returnsTrue() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test.txt");
        assertTrue(entry1.equals(entry2));
    }

    @Test
    public void testClone_createsCopy() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        entry.setSize(100);
        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        assertEquals(entry.getName(), cloned.getName());
        assertEquals(entry.getMethod(), cloned.getMethod());
        assertEquals(entry.getSize(), cloned.getSize());
    }

    @Test
    public void testHashCode_nameHashCode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt".hashCode(), entry.hashCode());
    }

    @Test
    public void testGetRawName_notSet_returnsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertNull(entry.getRawName());
    }

    @Test
    public void testGetGeneralPurposeBit_returnsNonNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertNotNull(entry.getGeneralPurposeBit());
    }

    @Test
    public void testSetGeneralPurposeBit_setsBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        GeneralPurposeBit bit = new GeneralPurposeBit();
        entry.setGeneralPurposeBit(bit);
        assertSame(bit, entry.getGeneralPurposeBit());
    }

    // ==================== New test methods for uncovered parts ====================

    @Test
    public void testSetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    @Test
    public void testGetPlatform_default() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        // Default platform is PLATFORM_UNIX
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test
    public void testSetInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setInternalAttributes(0x1234);
        assertEquals(0x1234, entry.getInternalAttributes());
    }

    @Test
    public void testSetExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setExternalAttributes(0xABCD);
        assertEquals(0xABCD, entry.getExternalAttributes());
    }

    @Test
    public void testSetVersionMadeBy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setVersionMadeBy(20);
        assertEquals(20, entry.getVersionMadeBy());
    }

    @Test
    public void testSetVersionRequired() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setVersionRequired(10);
        assertEquals(10, entry.getVersionRequired());
    }

    @Test
    public void testSetLocalHeaderOffset() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setLocalHeaderOffset(100);
        assertEquals(100, entry.getLocalHeaderOffset());
    }

    @Test
    public void testSetDataOffset() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setDataOffset(200);
        assertEquals(200, entry.getDataOffset());
    }

    @Test
    public void testSetComment() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setComment("a comment");
        assertEquals("a comment", entry.getComment());
    }

    @Test
    public void testSetExtra_byteArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] extra = new byte[] {0, 1, 2};
        entry.setExtra(extra);
        assertArrayEquals(extra, entry.getExtra());
    }

    @Test
    public void testSetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] extra = new byte[] {3, 4, 5};
        entry.setCentralDirectoryExtra(extra);
        assertArrayEquals(extra, entry.getCentralDirectoryExtra());
    }

    @Test
    public void testSetLastModifiedTime() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        FileTime time = FileTime.fromMillis(System.currentTimeMillis());
        entry.setLastModifiedTime(time);
        assertEquals(time, entry.getLastModifiedTime());
    }

    @Test
    public void testSetLastAccessTime() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        FileTime time = FileTime.fromMillis(System.currentTimeMillis());
        entry.setLastAccessTime(time);
        assertEquals(time, entry.getLastAccessTime());
    }

    @Test
    public void testSetCreationTime() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        FileTime time = FileTime.fromMillis(System.currentTimeMillis());
        entry.setCreationTime(time);
        assertEquals(time, entry.getCreationTime());
    }

    @Test
    public void testSetStreamContiguous() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertFalse(entry.isStreamContiguous());
        entry.setStreamContiguous(true);
        assertTrue(entry.isStreamContiguous());
    }

    @Test
    public void testToString_containsName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        String str = entry.toString();
        assertTrue(str.contains("test.txt"));
    }

    @Test
    public void testSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("old.txt");
        entry.setName("new.txt");
        assertEquals("new.txt", entry.getName());
    }

    @Test
    public void testSetName_withRawName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("old.txt");
        byte[] raw = "raw".getBytes();
        entry.setName("new.txt", raw);
        assertEquals("new.txt", entry.getName());
        assertArrayEquals(raw, entry.getRawName());
    }

    @Test
    public void testSetRawName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] raw = "rawbytes".getBytes();
        entry.setRawName(raw);
        assertArrayEquals(raw, entry.getRawName());
    }
}