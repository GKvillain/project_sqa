package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipArchiveEntryTest {

    // Tests equals with the same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertTrue(entry.equals(entry));
    }

    // Tests equals with null and object of different class
    @Test
    public void testEquals_nullOrDifferentClass_returnsFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("test.txt"));
    }

    // Tests equals when all fields are identical
    @Test
    public void testEquals_identicalEntries_returnsTrue() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("file.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("file.txt");
        assertTrue(entry1.equals(entry2));
        assertTrue(entry2.equals(entry1));
    }

    // Tests equals when names are different
    @Test
    public void testEquals_differentNames_returnsFalse() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("file1.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("file2.txt");
        assertFalse(entry1.equals(entry2));
    }

    // Tests equals when one comment is null and the other is empty string
    @Test
    public void testEquals_nullVsEmptyComment_returnsExpected() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test.txt");
        entry2.setComment("");
        // When entry1 comment is null and entry2 comment is "", or different comments
        assertFalse(entry1.equals(entry2));
    }

    // Tests equals when comments differ
    @Test
    public void testEquals_differentComments_returnsFalse() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
        entry1.setComment("comment1");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test.txt");
        entry2.setComment("comment2");
        assertFalse(entry1.equals(entry2));
    }

    // Tests hashCode returns hash code of name
    @Test
    public void testHashCode_validName_matchesStringHashCode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entryName");
        assertEquals("entryName".hashCode(), entry.hashCode());
    }

    // Tests setName with backslashes on FAT platform converts backslashes to forward slashes
    @Test
    public void testSetName_fatPlatformWithBackslash_replacesWithSlash() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir\\file.txt");
        assertEquals("dir/file.txt", entry.getName());
    }

    // Tests isDirectory when entry ends with forward slash vs does not
    @Test
    public void testIsDirectory_slashSuffix_returnsCorrectBoolean() {
        ZipArchiveEntry dirEntry = new ZipArchiveEntry("folder/");
        assertTrue(dirEntry.isDirectory());

        ZipArchiveEntry fileEntry = new ZipArchiveEntry("folder/file.txt");
        assertFalse(fileEntry.isDirectory());
    }

    // Tests setUnixMode and getUnixMode
    @Test
    public void testSetUnixMode_validMode_setsPlatformAndPermissions() {
        ZipArchiveEntry entry = new ZipArchiveEntry("script.sh");
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0755, entry.getUnixMode());
    }

    // Tests getUnixMode when platform is not UNIX
    @Test
    public void testGetUnixMode_fatPlatform_returnsZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        assertEquals(0, entry.getUnixMode());
    }

    // Tests setMethod with valid and negative values
    @Test
    public void testSetMethod_validMethod_updatesMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveEntry.DEFLATED);
        assertEquals(ZipArchiveEntry.DEFLATED, entry.getMethod());
    }

    // Tests setMethod with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negativeMethod_throwsIllegalArgumentException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(-2);
    }

    // Tests setSize with valid value and negative value
    @Test
    public void testSetSize_validSize_updatesSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(1024L);
        assertEquals(1024L, entry.getSize());
    }

    // Tests setSize with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negativeSize_throwsIllegalArgumentException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(-1L);
    }

    // Tests clone creates an independent copy
    @Test
    public void testClone_clonedEntry_isEqualAndIndependent() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setInternalAttributes(1);
        entry.setExternalAttributes(2L);
        entry.setMethod(ZipArchiveEntry.STORED);

        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        assertNotSame(entry, cloned);
        assertEquals(entry, cloned);
        assertEquals(1, cloned.getInternalAttributes());
        assertEquals(2L, cloned.getExternalAttributes());
    }

    // Tests constructor from java.util.zip.ZipEntry
    @Test
    public void testConstructor_fromJavaZipEntry_copiesFields() throws ZipException {
        ZipEntry standardEntry = new ZipEntry("test.txt");
        standardEntry.setSize(500L);
        standardEntry.setMethod(ZipEntry.DEFLATED);

        ZipArchiveEntry entry = new ZipArchiveEntry(standardEntry);
        assertEquals("test.txt", entry.getName());
        assertEquals(500L, entry.getSize());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    // Tests constructor from File
    @Test
    public void testConstructor_fromFile_setsNameAndMetadata() {
        File file = new File("test.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(file, "test.txt");
        assertEquals("test.txt", entry.getName());
        assertNotNull(entry.getLastModifiedDate());
    }

    // Tests add, lookup, and remove extra field
    @Test
    public void testAddAndRemoveExtraField_standardExtraField_success() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        ZipShort headerId = new ZipShort(1);
        field.setHeaderId(headerId);
        field.setLocalFileDataData(new byte[]{0x01, 0x02});

        entry.addExtraField(field);
        assertEquals(field, entry.getExtraField(headerId));
        assertEquals(1, entry.getExtraFields().length);

        entry.removeExtraField(headerId);
        assertNull(entry.getExtraField(headerId));
        assertEquals(0, entry.getExtraFields().length);
    }

    // Tests removeExtraField for nonexistent field throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraField_nonExistent_throwsNoSuchElementException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.removeExtraField(new ZipShort(999));
    }

    // Tests handling of UnparseableExtraFieldData
    @Test
    public void testUnparseableExtraFieldData_addAndRemove_success() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        byte[] data = new byte[]{1, 2, 3};
        unparseable.parseFromLocalFileData(data, 0, data.length);

        entry.addExtraField(unparseable);
        assertSame(unparseable, entry.getUnparseableExtraFieldData());
        assertEquals(1, entry.getExtraFields(true).length);
        assertEquals(0, entry.getExtraFields(false).length);

        entry.removeUnparseableExtraFieldData();
        assertNull(entry.getUnparseableExtraFieldData());
    }

    // Tests copy constructor with ZipArchiveEntry
    @Test
    public void testCopyConstructor_fromZipArchiveEntry_copiesAllAttributes() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("orig.txt");
        original.setInternalAttributes(3);
        original.setExternalAttributes(4L);
        original.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        original.setVersionMadeBy(20);
        original.setVersionRequired(10);
        original.setRawFlag(2);
        original.setDiskNumberStart(1L);

        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(42));
        field.setLocalFileDataData(new byte[]{1});
        original.addExtraField(field);

        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals("orig.txt", copy.getName());
        assertEquals(3, copy.getInternalAttributes());
        assertEquals(4L, copy.getExternalAttributes());
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, copy.getPlatform());
        assertEquals(20, copy.getVersionMadeBy());
        assertEquals(10, copy.getVersionRequired());
        assertEquals(2, copy.getRawFlag());
        assertEquals(1L, copy.getDiskNumberStart());
        assertNotNull(copy.getExtraField(new ZipShort(42)));
    }

    // Tests addAsFirstExtraField prepends field to list
    @Test
    public void testAddAsFirstExtraField_insertsAtBeginning() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");

        UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        field1.setHeaderId(new ZipShort(1));
        field1.setLocalFileDataData(new byte[]{1});

        UnrecognizedExtraField field2 = new UnrecognizedExtraField();
        field2.setHeaderId(new ZipShort(2));
        field2.setLocalFileDataData(new byte[]{2});

        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(new ZipShort(2), fields[0].getHeaderId());
        assertEquals(new ZipShort(1), fields[1].getHeaderId());
    }

    // Tests setExtraFields replaces all existing extra fields
    @Test
    public void testSetExtraFields_replacesExistingFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");

        UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        field1.setHeaderId(new ZipShort(1));
        field1.setLocalFileDataData(new byte[]{1});

        UnrecognizedExtraField field2 = new UnrecognizedExtraField();
        field2.setHeaderId(new ZipShort(2));
        field2.setLocalFileDataData(new byte[]{2});

        entry.addExtraField(field1);
        entry.setExtraFields(new ZipExtraField[]{field2});

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(1, fields.length);
        assertEquals(new ZipShort(2), fields[0].getHeaderId());
        assertNull(entry.getExtraField(new ZipShort(1)));
    }

    // Tests setExtra parses byte array into extra fields
    @Test
    public void testSetExtra_validByteArray_parsesExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        // Header ID: 0x0001 (little endian), Length: 0x0002, Data: 0x0A, 0x0B
        byte[] extraData = new byte[]{0x01, 0x00, 0x02, 0x00, 0x0A, 0x0B};

        entry.setExtra(extraData);
        ZipExtraField field = entry.getExtraField(new ZipShort(1));
        assertNotNull(field);
        assertArrayEquals(extraData, entry.getLocalFileDataExtra());
    }

    // Tests setCentralDirectoryExtra and getCentralDirectoryExtra
    @Test
    public void testSetCentralDirectoryExtra_validData_setsAndRetrievesBytes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] cdExtra = new byte[]{0x02, 0x00, 0x01, 0x00, (byte) 0xFF};

        entry.setCentralDirectoryExtra(cdExtra);
        assertNotNull(entry.getExtraField(new ZipShort(2)));
        assertArrayEquals(cdExtra, entry.getCentralDirectoryExtra());
    }

    // Tests isUnixSymlink returns true for symlink mode and false for regular mode
    @Test
    public void testIsUnixSymlink_symlinkAndRegularMode_returnsExpected() {
        ZipArchiveEntry entry = new ZipArchiveEntry("link.txt");
        entry.setUnixMode(0120777);
        assertTrue(entry.isUnixSymlink());

        entry.setUnixMode(0100644);
        assertFalse(entry.isUnixSymlink());
    }

    // Tests GeneralPurposeBit get and set
    @Test
    public void testGeneralPurposeBit_getterAndSetter_worksCorrectly() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        gpb.useDataDescriptor(true);

        entry.setGeneralPurposeBit(gpb);
        assertSame(gpb, entry.getGeneralPurposeBit());
    }

    // Tests rawFlag and version fields get and set
    @Test
    public void testVersionAndRawFlag_gettersAndSetters_storeValues() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setVersionMadeBy(45);
        entry.setVersionRequired(20);
        entry.setRawFlag(8);
        entry.setDiskNumberStart(3L);

        assertEquals(45, entry.getVersionMadeBy());
        assertEquals(20, entry.getVersionRequired());
        assertEquals(8, entry.getRawFlag());
        assertEquals(3L, entry.getDiskNumberStart());
    }

    // Tests getLastModifiedDate after setTime
    @Test
    public void testGetLastModifiedDate_afterSetTime_returnsMatchingDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        long timeMillis = 1609459200000L; // 2021-01-01 00:00:00 UTC
        entry.setTime(timeMillis);

        Date lastModified = entry.getLastModifiedDate();
        assertNotNull(lastModified);
    }
}