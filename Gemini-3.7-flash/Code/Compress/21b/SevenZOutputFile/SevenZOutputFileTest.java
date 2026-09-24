package org.apache.commons.compress.archivers.sevenz;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SevenZOutputFileTest {

    private File output;
    private File tempDir;

    @Before
    public void setUp() throws Exception {
        output = File.createTempFile("sevenz_test_", ".7z");
        tempDir = File.createTempFile("sevenz_temp_dir_", "");
        tempDir.delete();
        tempDir.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        if (output != null && output.exists()) {
            output.delete();
        }
        if (tempDir != null && tempDir.exists()) {
            tempDir.delete();
        }
    }

    // Tests calling finish on already finished archive throws IOException
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsException() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        try {
            out.finish();
            out.finish();
        } finally {
            out.close();
        }
    }

    // Tests closing an archive without calling finish first
    @Test
    public void testClose_withoutFinish_finishesAndClosesSuccessfully() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test.txt");
        out.putArchiveEntry(entry);
        out.write(1);
        out.closeArchiveEntry();
        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry readEntry = in.getNextEntry();
            assertNotNull(readEntry);
            assertEquals("test.txt", readEntry.getName());
            assertEquals(1, in.read());
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    // Tests createArchiveEntry with a regular file and a directory
    @Test
    public void testCreateArchiveEntry_fileAndDirectory_populatesAttributesCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        try {
            File dummyFile = new File(tempDir, "sample.txt");
            dummyFile.createNewFile();
            dummyFile.setLastModified(1000000000000L);

            SevenZArchiveEntry fileEntry = out.createArchiveEntry(dummyFile, "sample.txt");
            assertNotNull(fileEntry);
            assertEquals("sample.txt", fileEntry.getName());
            assertFalse(fileEntry.isDirectory());
            assertEquals(1000000000000L, fileEntry.getLastModifiedDate().getTime());

            SevenZArchiveEntry dirEntry = out.createArchiveEntry(tempDir, "sampleDir");
            assertNotNull(dirEntry);
            assertEquals("sampleDir", dirEntry.getName());
            assertTrue(dirEntry.isDirectory());

            dummyFile.delete();
        } finally {
            out.close();
        }
    }

    // Tests writing and reading single entry with content
    @Test
    public void testWrite_singleNonEmptyEntry_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("data.bin");
        out.putArchiveEntry(entry);

        byte[] data = new byte[]{10, 20, 30, 40, 50};
        out.write(data[0]);
        out.write(data, 1, data.length - 1);
        out.closeArchiveEntry();
        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry readEntry = in.getNextEntry();
            assertNotNull(readEntry);
            assertEquals("data.bin", readEntry.getName());
            assertEquals(5, readEntry.getSize());
            byte[] readContent = new byte[5];
            int bytesRead = in.read(readContent, 0, 5);
            assertEquals(5, bytesRead);
            assertArrayEquals(data, readContent);
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    // Tests writing empty archive entries (0 bytes)
    @Test
    public void testWrite_emptyEntry_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("empty.txt");
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry readEntry = in.getNextEntry();
            assertNotNull(readEntry);
            assertEquals("empty.txt", readEntry.getName());
            assertEquals(0, readEntry.getSize());
            assertFalse(readEntry.hasStream());
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    // Tests compression methods: COPY, LZMA2, BZIP2, DEFLATE
    @Test
    public void testSetContentCompression_supportedMethods_readsBackCorrectly() throws IOException {
        SevenZMethod[] methods = new SevenZMethod[]{
            SevenZMethod.COPY,
            SevenZMethod.LZMA2,
            SevenZMethod.BZIP2,
            SevenZMethod.DEFLATE
        };

        for (SevenZMethod method : methods) {
            SevenZOutputFile out = new SevenZOutputFile(output);
            out.setContentCompression(method);
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("method_" + method.name() + ".txt");
            out.putArchiveEntry(entry);
            byte[] data = ("Test data for compression: " + method.name()).getBytes("UTF-8");
            out.write(data);
            out.closeArchiveEntry();
            out.close();

            SevenZFile in = new SevenZFile(output);
            try {
                SevenZArchiveEntry readEntry = in.getNextEntry();
                assertNotNull("Entry should not be null for " + method.name(), readEntry);
                assertEquals("method_" + method.name() + ".txt", readEntry.getName());
                byte[] readBytes = new byte[data.length];
                int readLen = in.read(readBytes);
                assertEquals(data.length, readLen);
                assertArrayEquals(data, readBytes);
            } finally {
                in.close();
            }
        }
    }

    // Tests archives with multiple empty files and directory entries (boundary test for bitset encoding)
    @Test
    public void testArchive_multipleEmptyFilesAndDirectories_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        int numEntries = 18; // > 8 to trigger multi-byte bit encoding
        for (int i = 0; i < numEntries; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("item_" + i);
            if (i % 2 == 0) {
                entry.setDirectory(true);
            }
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
        }
        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            for (int i = 0; i < numEntries; i++) {
                SevenZArchiveEntry readEntry = in.getNextEntry();
                assertNotNull("Entry " + i + " should exist", readEntry);
                assertEquals("item_" + i, readEntry.getName());
                assertEquals(i % 2 == 0, readEntry.isDirectory());
            }
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    // Tests anti items bit encoding and reading
    @Test
    public void testArchive_antiItems_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("anti_item");
        entry1.setAntiItem(true);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("regular_empty");
        entry2.setAntiItem(false);
        out.putArchiveEntry(entry2);
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertEquals("anti_item", read1.getName());
            assertTrue(read1.isAntiItem());

            SevenZArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertEquals("regular_empty", read2.getName());
            assertFalse(read2.isAntiItem());

            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    // Tests entries with creation dates (all entries having dates vs partial entries)
    @Test
    public void testArchive_creationDates_allAndPartial_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        Date cDate1 = new Date(1234567800000L);
        Date cDate2 = new Date(1334567800000L);

        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("c1.txt");
        entry1.setCreationDate(cDate1);
        out.putArchiveEntry(entry1);
        out.write(1);
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("c2.txt");
        entry2.setCreationDate(cDate2);
        out.putArchiveEntry(entry2);
        out.write(2);
        out.closeArchiveEntry();

        SevenZArchiveEntry entry3 = new SevenZArchiveEntry();
        entry3.setName("c3.txt");
        entry3.setHasCreationDate(false);
        out.putArchiveEntry(entry3);
        out.write(3);
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertTrue(read1.getHasCreationDate());
            assertEquals(cDate1.getTime(), read1.getCreationDate().getTime());

            SevenZArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertTrue(read2.getHasCreationDate());
            assertEquals(cDate2.getTime(), read2.getCreationDate().getTime());

            SevenZArchiveEntry read3 = in.getNextEntry();
            assertNotNull(read3);
            assertFalse(read3.getHasCreationDate());
        } finally {
            in.close();
        }
    }

    // Tests entries with access dates
    @Test
    public void testArchive_accessDates_allAndPartial_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        Date aDate1 = new Date(1400000000000L);

        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("a1.txt");
        entry1.setAccessDate(aDate1);
        out.putArchiveEntry(entry1);
        out.write(new byte[]{1, 2, 3});
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("a2.txt");
        entry2.setHasAccessDate(false);
        out.putArchiveEntry(entry2);
        out.write(new byte[]{4, 5, 6});
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertTrue(read1.getHasAccessDate());
            assertEquals(aDate1.getTime(), read1.getAccessDate().getTime());

            SevenZArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertFalse(read2.getHasAccessDate());
        } finally {
            in.close();
        }
    }

    // Tests entries with last modified dates
    @Test
    public void testArchive_lastModifiedDates_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        Date mDate1 = new Date(1500000000000L);
        Date mDate2 = new Date(1600000000000L);

        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("m1.txt");
        entry1.setLastModifiedDate(mDate1);
        out.putArchiveEntry(entry1);
        out.write(10);
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("m2.txt");
        entry2.setLastModifiedDate(mDate2);
        out.putArchiveEntry(entry2);
        out.write(20);
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertTrue(read1.getHasLastModifiedDate());
            assertEquals(mDate1.getTime(), read1.getLastModifiedDate().getTime());

            SevenZArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertTrue(read2.getHasLastModifiedDate());
            assertEquals(mDate2.getTime(), read2.getLastModifiedDate().getTime());
        } finally {
            in.close();
        }
    }

    // Tests entries with Windows attributes
    @Test
    public void testArchive_windowsAttributes_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);
        int winAttr1 = 0x20; // Archive attribute
        int winAttr2 = 0x01; // Read-only attribute

        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("w1.txt");
        entry1.setWindowsAttributes(winAttr1);
        out.putArchiveEntry(entry1);
        out.write(1);
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("w2.txt");
        entry2.setWindowsAttributes(winAttr2);
        out.putArchiveEntry(entry2);
        out.write(2);
        out.closeArchiveEntry();

        SevenZArchiveEntry entry3 = new SevenZArchiveEntry();
        entry3.setName("w3.txt");
        entry3.setHasWindowsAttributes(false);
        out.putArchiveEntry(entry3);
        out.write(3);
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertTrue(read1.getHasWindowsAttributes());
            assertEquals(winAttr1, read1.getWindowsAttributes());

            SevenZArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertTrue(read2.getHasWindowsAttributes());
            assertEquals(winAttr2, read2.getWindowsAttributes());

            SevenZArchiveEntry read3 = in.getNextEntry();
            assertNotNull(read3);
            assertFalse(read3.getHasWindowsAttributes());
        } finally {
            in.close();
        }
    }

    // Tests writing multiple non-empty files with non-zero offsets
    @Test
    public void testWrite_multipleFilesWithOffsets_readsBackCorrectly() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output);

        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("file1.txt");
        out.putArchiveEntry(entry1);
        byte[] buffer = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        out.write(buffer, 2, 5); // writes 2, 3, 4, 5, 6 (len 5)
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("file2.txt");
        out.putArchiveEntry(entry2);
        out.write(buffer, 0, 0); // length 0 write
        out.write(buffer, 7, 3); // writes 7, 8, 9 (len 3)
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output);
        try {
            SevenZArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertEquals("file1.txt", read1.getName());
            assertEquals(5, read1.getSize());
            byte[] content1 = new byte[5];
            in.read(content1);
            assertArrayEquals(new byte[]{2, 3, 4, 5, 6}, content1);

            SevenZArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertEquals("file2.txt", read2.getName());
            assertEquals(3, read2.getSize());
            byte[] content2 = new byte[3];
            in.read(content2);
            assertArrayEquals(new byte[]{7, 8, 9}, content2);

            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }
}