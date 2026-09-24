package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ZipArchiveEntryTest {

    // Tests normal construction from name and directory detection
    @Test
    public void testConstructor_withDirectoryName_setsDirectoryFlag() {
        final ZipArchiveEntry dirEntry = new ZipArchiveEntry("testDir/");
        assertTrue(dirEntry.isDirectory());
        assertEquals("testDir/", dirEntry.getName());

        final ZipArchiveEntry fileEntry = new ZipArchiveEntry("testFile.txt");
        assertFalse(fileEntry.isDirectory());
        assertEquals("testFile.txt", fileEntry.getName());
    }

    // Tests name normalization for FAT platform with backslashes
    @Test
    public void testSetName_fatPlatformWithBackslashes_normalizesForwardSlashes() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("folder\\file.txt");
        assertEquals("folder/file.txt", entry.getName());
    }

    // Tests construction from standard java.util.zip.ZipEntry
    @Test
    public void testConstructor_fromJavaZipEntry_copiesFieldsCorrectly() throws Exception {
        final ZipEntry javaEntry = new ZipEntry("entryName");
        javaEntry.setSize(1024L);
        javaEntry.setMethod(ZipEntry.DEFLATED);
        javaEntry.setTime(123456000L);

        final ZipArchiveEntry archiveEntry = new ZipArchiveEntry(javaEntry);
        assertEquals("entryName", archiveEntry.getName());
        assertEquals(1024L, archiveEntry.getSize());
        assertEquals(ZipEntry.DEFLATED, archiveEntry.getMethod());
    }

    // Tests copy constructor and clone method
    @Test
    public void testCloneAndCopyConstructor_createsEqualAndIndependentCopy() throws Exception {
        final ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setSize(500L);
        original.setInternalAttributes(1);
        original.setExternalAttributes(2L);
        original.setMethod(ZipMethod.DEFLATED.getCode());
        original.setUnixMode(0644);

        final ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals(original, copy);

        final ZipArchiveEntry cloned = (ZipArchiveEntry) original.clone();
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    // Tests constructor with File object
    @Test
    public void testConstructor_fromFile_setsSizeAndDate() throws IOException {
        final File tmpFile = File.createTempFile("zip_entry_test", ".tmp");
        tmpFile.deleteOnExit();
        final FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(new byte[]{1, 2, 3, 4, 5});
        fos.close();

        final ZipArchiveEntry entry = new ZipArchiveEntry(tmpFile, "entry_from_file.dat");
        assertFalse(entry.isDirectory());
        assertEquals(5L, entry.getSize());
        assertEquals(tmpFile.lastModified(), entry.getTime());
    }

    // Tests method setter and getter with valid and invalid values
    @Test
    public void testSetMethod_validMethod_storesMethod() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setMethod(ZipMethod.STORED.getCode());
        assertEquals(ZipMethod.STORED.getCode(), entry.getMethod());
    }

    // Tests negative method throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negativeMethod_throwsException() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setMethod(-2);
    }

    // Tests negative size throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negativeSize_throwsException() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setSize(-1L);
    }

    // Tests unix mode permissions, platform setting, and symlink detection
    @Test
    public void testUnixMode_regularFileAndSymlink_returnsExpectedPermissions() {
        final ZipArchiveEntry regularEntry = new ZipArchiveEntry("regular.txt");
        regularEntry.setUnixMode(UnixStat.FILE_FLAG | 0644);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, regularEntry.getPlatform());
        assertEquals(UnixStat.FILE_FLAG | 0644, regularEntry.getUnixMode());
        assertFalse(regularEntry.isUnixSymlink());

        final ZipArchiveEntry symlinkEntry = new ZipArchiveEntry("symlink");
        symlinkEntry.setUnixMode(UnixStat.LINK_FLAG | 0777);
        assertEquals(UnixStat.LINK_FLAG | 0777, symlinkEntry.getUnixMode());
        assertTrue(symlinkEntry.isUnixSymlink());
    }

    // Tests unix mode when platform is FAT
    @Test
    public void testGetUnixMode_fatPlatform_returnsZero() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        entry.setExternalAttributes(0644L << 16);
        assertEquals(0, entry.getUnixMode());
        assertFalse(entry.isUnixSymlink());
    }

    // Tests raw name handling
    @Test
    public void testSetName_withRawBytes_storesAndReturnsRawNameCopy() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("testName");
        final byte[] raw = new byte[]{0x74, 0x65, 0x73, 0x74};
        entry.setName("test", raw);

        assertEquals("test", entry.getName());
        assertArrayEquals(raw, entry.getRawName());
        assertNotSame(raw, entry.getRawName());
    }

    // Tests adding, getting, replacing, and removing extra fields
    @Test
    public void testAddAndRemoveExtraField_manipulatesExtraFieldsCorrectly() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        final UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        final ZipShort headerId1 = new ZipShort(1);
        field1.setHeaderId(headerId1);
        field1.setLocalFileDataData(new byte[]{1, 2});
        field1.setCentralDirectoryData(new byte[]{1, 2});

        entry.addExtraField(field1);
        assertEquals(1, entry.getExtraFields().length);
        assertEquals(field1, entry.getExtraField(headerId1));

        final UnrecognizedExtraField field2 = new UnrecognizedExtraField();
        final ZipShort headerId2 = new ZipShort(2);
        field2.setHeaderId(headerId2);
        field2.setLocalFileDataData(new byte[]{3, 4});
        field2.setCentralDirectoryData(new byte[]{3, 4});

        entry.addAsFirstExtraField(field2);
        assertEquals(2, entry.getExtraFields().length);
        assertEquals(field2, entry.getExtraFields()[0]);

        entry.removeExtraField(headerId1);
        assertEquals(1, entry.getExtraFields().length);
        assertNull(entry.getExtraField(headerId1));
    }

    // Tests removing nonexistent extra field throws exception
    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraField_nonExistentField_throwsException() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.removeExtraField(new ZipShort(999));
    }

    // Tests unparseable extra field data handling
    @Test
    public void testUnparseableExtraFieldData_addRetrieveAndRemove() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        final UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[]{1, 2, 3}, 0, 3);

        entry.addExtraField(unparseable);
        assertNotNull(entry.getUnparseableExtraFieldData());
        assertEquals(0, entry.getExtraFields(false).length);
        assertEquals(1, entry.getExtraFields(true).length);

        entry.removeUnparseableExtraFieldData();
        assertNull(entry.getUnparseableExtraFieldData());
    }

    // Tests remove unparseable extra field data when none exists throws exception
    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldData_whenNull_throwsException() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.removeUnparseableExtraFieldData();
    }

    // Tests setExtra and setCentralDirectoryExtra parsing and merging
    @Test
    public void testSetExtraAndSetCentralDirectoryExtra_parsesValidBytes() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        final byte[] extraData = new byte[]{
            1, 0, // Header ID = 1
            2, 0, // Length = 2
            10, 20 // Data
        };

        entry.setExtra(extraData);
        assertNotNull(entry.getExtraField(new ZipShort(1)));
        assertNotNull(entry.getLocalFileDataExtra());

        entry.setCentralDirectoryExtra(extraData);
        assertNotNull(entry.getCentralDirectoryExtra());
    }

    // Tests equals and hashCode behavior
    @Test
    public void testEqualsAndHashCode_sameAndDifferentEntries() {
        final ZipArchiveEntry entry1 = new ZipArchiveEntry("file.txt");
        final ZipArchiveEntry entry2 = new ZipArchiveEntry("file.txt");
        final ZipArchiveEntry entry3 = new ZipArchiveEntry("other.txt");

        assertTrue(entry1.equals(entry1));
        assertTrue(entry1.equals(entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());

        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals(new Object()));
    }

    // Tests version and flag setters and getters
    @Test
    public void testVersionAndFlagFields_setAndGetProperly() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setVersionMadeBy(45);
        entry.setVersionRequired(20);
        entry.setRawFlag(8);

        assertEquals(45, entry.getVersionMadeBy());
        assertEquals(20, entry.getVersionRequired());
        assertEquals(8, entry.getRawFlag());
    }

    // Tests getLastModifiedDate wrapping getTime
    @Test
    public void testGetLastModifiedDate_returnsDateMatchingTime() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        final long time = 1500000000000L;
        entry.setTime(time);
        assertEquals(new Date(entry.getTime()), entry.getLastModifiedDate());
    }

    // Tests general purpose bit getter and setter
    @Test
    public void testGeneralPurposeBit_getAndSet() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        final GeneralPurposeBit bit = new GeneralPurposeBit();
        bit.useUTF8ForNames(true);
        entry.setGeneralPurposeBit(bit);

        assertEquals(bit, entry.getGeneralPurposeBit());
        assertTrue(entry.getGeneralPurposeBit().usesUTF8ForNames());
    }
}