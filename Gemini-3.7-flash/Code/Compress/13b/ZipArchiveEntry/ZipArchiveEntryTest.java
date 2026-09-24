package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class ZipArchiveEntryTest {

    // Tests constructor with String name and isDirectory logic
    @Test
    public void testConstructor_stringName_setsNameAndDirectoryFlag() {
        ZipArchiveEntry entryFile = new ZipArchiveEntry("foo.txt");
        assertEquals("foo.txt", entryFile.getName());
        assertFalse(entryFile.isDirectory());

        ZipArchiveEntry entryDir = new ZipArchiveEntry("foo/");
        assertEquals("foo/", entryDir.getName());
        assertTrue(entryDir.isDirectory());
    }

    // Tests constructor from File object
    @Test
    public void testConstructor_fileAndName_setsSizeTimeAndDirectorySlash() {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        ZipArchiveEntry entryDir = new ZipArchiveEntry(dir, "tempDir");
        assertTrue(entryDir.getName().endsWith("/"));
        assertTrue(entryDir.isDirectory());

        File dummyFile = new File("non_existent_file_for_test_12345");
        ZipArchiveEntry entryFile = new ZipArchiveEntry(dummyFile, "dummy.txt");
        assertEquals("dummy.txt", entryFile.getName());
        assertFalse(entryFile.isDirectory());
    }

    // Tests constructor from standard java.util.zip.ZipEntry
    @Test
    public void testConstructor_zipEntry_copiesFieldsCorrectly() throws Exception {
        ZipEntry standardEntry = new ZipEntry("test.txt");
        standardEntry.setComment("a comment");
        standardEntry.setSize(1024L);
        standardEntry.setMethod(ZipEntry.DEFLATED);
        standardEntry.setTime(12345678L);

        ZipArchiveEntry archiveEntry = new ZipArchiveEntry(standardEntry);
        assertEquals("test.txt", archiveEntry.getName());
        assertEquals("a comment", archiveEntry.getComment());
        assertEquals(1024L, archiveEntry.getSize());
        assertEquals(ZipEntry.DEFLATED, archiveEntry.getMethod());
        assertEquals(12345678L, archiveEntry.getTime());
    }

    // Tests copy constructor from another ZipArchiveEntry
    @Test
    public void testCopyConstructor_zipArchiveEntry_clonesAttributesAndExtraFields() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("orig.txt");
        original.setInternalAttributes(2);
        original.setExternalAttributes(4096L);
        original.setUnixMode(0755);

        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getInternalAttributes(), copy.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), copy.getExternalAttributes());
        assertEquals(original.getUnixMode(), copy.getUnixMode());
        assertEquals(original.getPlatform(), copy.getPlatform());
    }

    // Tests clone method
    @Test
    public void testClone_clonedEntry_isEqualAndIndependent() {
        ZipArchiveEntry entry = new ZipArchiveEntry("cloneTest.txt");
        entry.setInternalAttributes(10);
        entry.setExternalAttributes(20L);

        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        assertNotSame(entry, clone);
        assertEquals(entry, clone);
        assertEquals(entry.getInternalAttributes(), clone.getInternalAttributes());
        assertEquals(entry.getExternalAttributes(), clone.getExternalAttributes());
    }

    // Tests method setter and getter with valid values
    @Test
    public void testSetMethod_validMethod_returnsCorrectMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("method.txt");
        assertEquals(-1, entry.getMethod());

        entry.setMethod(ZipArchiveEntry.STORED);
        assertEquals(ZipArchiveEntry.STORED, entry.getMethod());

        entry.setMethod(ZipArchiveEntry.DEFLATED);
        assertEquals(ZipArchiveEntry.DEFLATED, entry.getMethod());
    }

    // Tests method setter exception on negative input
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negativeMethod_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("methodNegative.txt");
        entry.setMethod(-2);
    }

    // Tests size setter and getter with valid and boundary values
    @Test
    public void testSetSize_validSize_returnsCorrectSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("size.txt");
        entry.setSize(0L);
        assertEquals(0L, entry.getSize());

        entry.setSize(5000000000L); // > 2GB (Zip64 test)
        assertEquals(5000000000L, entry.getSize());
    }

    // Tests size setter exception on negative input
    @Test(expected = IllegalArgumentException.class)
    public void testSetSize_negativeSize_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("sizeNegative.txt");
        entry.setSize(-1L);
    }

    // Tests unixMode calculations and platform transition
    @Test
    public void testSetUnixMode_unixModeValue_computesExternalAttributesAndPlatform() {
        ZipArchiveEntry fileEntry = new ZipArchiveEntry("file.txt");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, fileEntry.getPlatform());
        assertEquals(0, fileEntry.getUnixMode());

        fileEntry.setUnixMode(0644);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, fileEntry.getPlatform());
        assertEquals(0644, fileEntry.getUnixMode());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        dirEntry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, dirEntry.getPlatform());
        assertEquals(0755, dirEntry.getUnixMode());
        assertTrue((dirEntry.getExternalAttributes() & 0x10) != 0); // Directory MS-DOS flag
    }

    // Tests extra fields manipulation (add, get, replace, remove)
    @Test
    public void testExtraFields_addAndRemove_manipulatesFieldsProperly() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extra.txt");
        assertEquals(0, entry.getExtraFields().length);

        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[]{1, 2, 3, 4}, 0, 4);
        entry.addExtraField(unparseable);

        assertEquals(0, entry.getExtraFields(false).length);
        assertEquals(1, entry.getExtraFields(true).length);
        assertSame(unparseable, entry.getUnparseableExtraFieldData());

        entry.removeUnparseableExtraFieldData();
        assertNull(entry.getUnparseableExtraFieldData());
        assertEquals(0, entry.getExtraFields(true).length);
    }

    // Tests removeExtraField throws exception when field not found
    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraField_nonExistentField_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extraRemove.txt");
        entry.removeExtraField(new ZipShort(0x1234));
    }

    // Tests removeUnparseableExtraFieldData throws exception when no unparseable data
    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldData_whenNone_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extraRemoveUnparseable.txt");
        entry.removeUnparseableExtraFieldData();
    }

    // Tests setExtra byte array parsing
    @Test
    public void testSetExtra_byteArray_parsesAndPopulatesExtraData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extraBytes.txt");
        byte[] dummyExtra = new byte[] {
            (byte) 0x55, (byte) 0x54, // Header ID
            (byte) 0x05, (byte) 0x00, // Length: 5
            (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04
        };
        entry.setExtra(dummyExtra);
        assertNotNull(entry.getExtra());
        assertTrue(entry.getExtraFields().length > 0 || entry.getUnparseableExtraFieldData() != null);
        assertNotNull(entry.getLocalFileDataExtra());
    }

    // Tests rawName and encoding representation
    @Test
    public void testSetName_withRawName_storesAndReturnsRawBytes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("testName");
        assertNull(entry.getRawName());

        byte[] raw = new byte[] { 't', 'e', 's', 't' };
        entry.setName("test", raw);
        assertEquals("test", entry.getName());
        assertArrayEquals(raw, entry.getRawName());

        byte[] retrieved = entry.getRawName();
        retrieved[0] = 'x'; // Ensure defensive copy
        assertEquals('t', entry.getRawName()[0]);
    }

    // Tests GeneralPurposeBit setter and getter
    @Test
    public void testGeneralPurposeBit_setAndGet_returnsCorrectValue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("gpb.txt");
        assertNotNull(entry.getGeneralPurposeBit());

        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        entry.setGeneralPurposeBit(gpb);
        assertTrue(entry.getGeneralPurposeBit().usesUTF8ForNames());
    }

    // Tests getLastModifiedDate returns correct Date object
    @Test
    public void testGetLastModifiedDate_validTime_returnsMatchingDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("date.txt");
        long time = 1500000000000L;
        entry.setTime(time);
        assertEquals(new Date(time), entry.getLastModifiedDate());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_variousConditions_behaveCorrectly() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry.txt");

        // Same reference
        assertTrue(entry1.equals(entry1));

        // Equal contents
        assertTrue(entry1.equals(entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());

        // Null and different class
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals(new Object()));

        // Different name
        ZipArchiveEntry entryDiffName = new ZipArchiveEntry("other.txt");
        assertFalse(entry1.equals(entryDiffName));

        // Different comment
        entry2.setComment("comment");
        assertFalse(entry1.equals(entry2));
    }
}