package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZipArchiveEntryTest {

    // Tests equality between two entries with different names (Defects4J Compress-6 bug regression)
    @Test
    public void testEquals_differentNames_returnsFalse() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
        assertFalse(entry1.equals(entry2));
    }

    // Tests equality with copy constructor (Defects4J Compress-6 bug regression)
    @Test
    public void testEquals_copyConstructor_returnsTrue() throws Exception {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry(entry1);
        assertTrue(entry1.equals(entry2));
        assertTrue(entry2.equals(entry1));
    }

    // Tests equality with same instance and null / incompatible type
    @Test
    public void testEquals_sameObjectAndInvalidTypes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("foo");
        assertTrue(entry.equals(entry));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("foo"));
    }

    // Tests hashCode consistency with entry name
    @Test
    public void testHashCode_matchesNameHashCode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test/file.txt");
        assertEquals("test/file.txt".hashCode(), entry.hashCode());
    }

    // Tests isDirectory for file and directory paths
    @Test
    public void testIsDirectory_directoryAndFileNames() {
        ZipArchiveEntry dirEntry = new ZipArchiveEntry("directory/");
        ZipArchiveEntry fileEntry = new ZipArchiveEntry("directory/file.txt");

        assertTrue(dirEntry.isDirectory());
        assertFalse(fileEntry.isDirectory());
    }

    // Tests constructor taking File and entryName for file
    @Test
    public void testConstructor_fileInput() {
        File tempFile = new File("pom.xml");
        ZipArchiveEntry entry = new ZipArchiveEntry(tempFile, "pom.xml");

        assertEquals("pom.xml", entry.getName());
        assertFalse(entry.isDirectory());
        assertEquals(new Date(tempFile.lastModified()), entry.getLastModifiedDate());
    }

    // Tests constructor taking File and entryName for directory
    @Test
    public void testConstructor_directoryInput() {
        File dir = new File(".");
        ZipArchiveEntry entry = new ZipArchiveEntry(dir, "rootDir");

        assertEquals("rootDir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    // Tests method setting and supported compression method checks
    @Test
    public void testMethod_supportedAndUnsupportedValues() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(-1, entry.getMethod());
        assertFalse(entry.isSupportedCompressionMethod());

        entry.setMethod(ZipArchiveEntry.STORED);
        assertEquals(ZipArchiveEntry.STORED, entry.getMethod());
        assertTrue(entry.isSupportedCompressionMethod());

        entry.setMethod(ZipArchiveEntry.DEFLATED);
        assertEquals(ZipArchiveEntry.DEFLATED, entry.getMethod());
        assertTrue(entry.isSupportedCompressionMethod());

        entry.setMethod(88);
        assertEquals(88, entry.getMethod());
        assertFalse(entry.isSupportedCompressionMethod());
    }

    // Tests setting negative compression method throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetMethod_negativeValue_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(-2);
    }

    // Tests internal and external attributes getters and setters
    @Test
    public void testAttributes_getAndSet() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setInternalAttributes(12);
        assertEquals(12, entry.getInternalAttributes());

        entry.setExternalAttributes(0644L);
        assertEquals(0644L, entry.getExternalAttributes());
    }

    // Tests unix mode and platform handling
    @Test
    public void testUnixMode_andPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        assertEquals(0, entry.getUnixMode());

        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0755, entry.getUnixMode());
    }

    // Tests adding, retrieving, and removing extra fields
    @Test
    public void testExtraFields_addGetRemove() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(0, entry.getExtraFields().length);

        ExtraField dummyField = new ExtraField(new ZipShort(1), new byte[]{1, 2});
        entry.addExtraField(dummyField);

        assertEquals(1, entry.getExtraFields().length);
        assertEquals(dummyField, entry.getExtraField(new ZipShort(1)));
        assertNull(entry.getExtraField(new ZipShort(2)));

        entry.removeExtraField(new ZipShort(1));
        assertEquals(0, entry.getExtraFields().length);
    }

    // Tests removing non-existent extra field throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraField_notFound_throwsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.removeExtraField(new ZipShort(99));
    }

    // Tests adding extra field as first extra field
    @Test
    public void testAddAsFirstExtraField_orderCorrect() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        ExtraField field1 = new ExtraField(new ZipShort(1), new byte[]{1});
        ExtraField field2 = new ExtraField(new ZipShort(2), new byte[]{2});

        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(new ZipShort(2), fields[0].getHeaderId());
        assertEquals(new ZipShort(1), fields[1].getHeaderId());
    }

    // Tests clone operation produces equivalent independent copy
    @Test
    public void testClone_createsIndependentCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setUnixMode(0644);
        ExtraField field = new ExtraField(new ZipShort(1), new byte[]{1});
        entry.addExtraField(field);

        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        assertEquals(entry.getName(), cloned.getName());
        assertEquals(entry.getUnixMode(), cloned.getUnixMode());
        assertEquals(entry.getPlatform(), cloned.getPlatform());
        assertEquals(1, cloned.getExtraFields().length);
    }

    // Tests constructor from standard java.util.zip.ZipEntry
    @Test
    public void testConstructor_fromStandardZipEntry() throws Exception {
        ZipEntry stdEntry = new ZipEntry("stdEntry.txt");
        stdEntry.setMethod(ZipEntry.DEFLATED);

        ZipArchiveEntry entry = new ZipArchiveEntry(stdEntry);
        assertEquals("stdEntry.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        assertNotNull(entry.getLocalFileDataExtra());
        assertNotNull(entry.getCentralDirectoryExtra());
    }

    // Helper ZipExtraField implementation for testing extra fields behavior
    private static class ExtraField implements ZipExtraField {
        private final ZipShort headerId;
        private final byte[] data;

        ExtraField(ZipShort headerId, byte[] data) {
            this.headerId = headerId;
            this.data = data;
        }

        public ZipShort getHeaderId() {
            return headerId;
        }

        public ZipShort getLocalFileDataLength() {
            return new ZipShort(data.length);
        }

        public ZipShort getCentralDirectoryLength() {
            return new ZipShort(data.length);
        }

        public byte[] getLocalFileDataData() {
            return data;
        }

        public byte[] getCentralDirectoryData() {
            return data;
        }

        public void parseFromLocalFileData(byte[] buffer, int offset, int length) {
        }

        public void parseFromCentralDirectoryData(byte[] buffer, int offset, int length) {
        }
    }
}