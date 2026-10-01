package org.apache.commons.compress.archivers.sevenz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SevenZOutputFileTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // Tests normal creation and close of empty archive
    @Test
    public void testFinish_emptyArchive_success() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        sevenZFile.close();
        assertTrue(f.exists());
        assertTrue(f.length() > 0);
    }

    // Tests finish with a single entry that has content
    @Test
    public void testWrite_singleEntry_createsArchive() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test.txt");
        sevenZFile.putArchiveEntry(entry);
        sevenZFile.write("hello".getBytes());
        sevenZFile.closeArchiveEntry();
        sevenZFile.close();
        assertTrue(f.exists());
        assertTrue(f.length() > 0);
    }

    // Tests multiple entries
    @Test
    public void testWrite_multipleEntries_createsArchive() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        for (int i = 0; i < 3; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file" + i + ".txt");
            sevenZFile.putArchiveEntry(entry);
            sevenZFile.write(("content" + i).getBytes());
            sevenZFile.closeArchiveEntry();
        }
        sevenZFile.close();
        assertTrue(f.exists());
    }

    // Tests write with empty content (zero bytes written)
    @Test
    public void testCloseArchiveEntry_emptyEntry_hasNoStream() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("empty.txt");
        sevenZFile.putArchiveEntry(entry);
        sevenZFile.closeArchiveEntry();
        assertFalse(entry.hasStream());
        assertEquals(0, entry.getSize());
        assertEquals(0, entry.getCompressedSize());
        sevenZFile.close();
    }

    // Tests write with non-empty content - entry should have stream and CRC
    @Test
    public void testCloseArchiveEntry_nonEmptyEntry_hasStreamAndCrc() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("data.bin");
        sevenZFile.putArchiveEntry(entry);
        sevenZFile.write(new byte[]{1, 2, 3, 4, 5});
        sevenZFile.closeArchiveEntry();
        assertTrue(entry.hasStream());
        assertEquals(5, entry.getSize());
        assertTrue(entry.getHasCrc());
        sevenZFile.close();
    }

    // Tests write with large data to verify size tracking
    @Test
    public void testWrite_largeData_tracksSizeCorrectly() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("large.bin");
        sevenZFile.putArchiveEntry(entry);
        byte[] data = new byte[1024 * 10]; // 10KB
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        sevenZFile.write(data);
        sevenZFile.closeArchiveEntry();
        assertEquals(data.length, entry.getSize());
        sevenZFile.close();
    }

    // Tests putArchiveEntry with invalid cast (should fail)
    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntry_invalidType_throwsException() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        ArchiveEntry badEntry = new ArchiveEntry() {
            public String getName() { return "bad"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public Date getLastModifiedDate() { return new Date(); }
        };
        sevenZFile.putArchiveEntry(badEntry);
    }

    // Tests finish called twice throws IOException
    @Test(expected = IOException.class)
    public void testFinish_twice_throwsException() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        sevenZFile.finish();
        sevenZFile.finish();
    }

    // Tests that close calls finish internally
    @Test
    public void testClose_callsFinish_doesNotThrow() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        sevenZFile.close();
        // Should not throw
    }

    // Tests write(int b) method
    @Test
    public void testWrite_singleByte_writesCorrectly() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("single.txt");
        sevenZFile.putArchiveEntry(entry);
        sevenZFile.write(65); // 'A'
        sevenZFile.closeArchiveEntry();
        assertEquals(1, entry.getSize());
        sevenZFile.close();
    }

    // Tests write with offset and length
    @Test
    public void testWrite_offsetAndLength_writesCorrectly() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("partial.txt");
        sevenZFile.putArchiveEntry(entry);
        byte[] data = "abcdefghij".getBytes();
        sevenZFile.write(data, 2, 3); // writes "cde"
        sevenZFile.closeArchiveEntry();
        assertEquals(3, entry.getSize());
        sevenZFile.close();
    }

    // Tests write with empty byte array (len == 0)
    @Test
    public void testWrite_emptyArray_writesNothing() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("empty.txt");
        sevenZFile.putArchiveEntry(entry);
        sevenZFile.write(new byte[0]);
        sevenZFile.closeArchiveEntry();
        assertFalse(entry.hasStream());
        sevenZFile.close();
    }

    // Tests createArchiveEntry with a directory
    @Test
    public void testCreateArchiveEntry_directory_setsDirectory() throws IOException {
        File f = tempFolder.newFile("test.7z");
        File dir = tempFolder.newFolder("testdir");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = sevenZFile.createArchiveEntry(dir, "mydir");
        assertTrue(entry.isDirectory());
        assertEquals("mydir", entry.getName());
        sevenZFile.close();
    }

    // Tests createArchiveEntry with a file
    @Test
    public void testCreateArchiveEntry_file_setsNotDirectory() throws IOException {
        File f = tempFolder.newFile("test.7z");
        File inputFile = tempFolder.newFile("input.txt");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry = sevenZFile.createArchiveEntry(inputFile, "myfile.txt");
        assertFalse(entry.isDirectory());
        assertEquals("myfile.txt", entry.getName());
        assertNotNull(entry.getLastModifiedDate());
        sevenZFile.close();
    }

    // Tests setContentCompression and verify it doesn't break writing
    @Test
    public void testSetContentCompression_copyMethod_writesSuccessfully() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        sevenZFile.setContentCompression(SevenZMethod.COPY);
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("copy.txt");
        sevenZFile.putArchiveEntry(entry);
        sevenZFile.write("test".getBytes());
        sevenZFile.closeArchiveEntry();
        sevenZFile.close();
        assertTrue(f.exists());
    }

    // Tests multiple closeArchiveEntry calls (should only affect last entry)
    @Test
    public void testCloseArchiveEntry_calledTwice_handlesGracefully() throws IOException {
        File f = tempFolder.newFile("test.7z");
        SevenZOutputFile sevenZFile = new SevenZOutputFile(f);
        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("first.txt");
        sevenZFile.putArchiveEntry(entry1);
        sevenZFile.write("data1".getBytes());
        sevenZFile.closeArchiveEntry();
        // close again without new entry - should not throw but may be redundant
        // This is an edge case of the API usage
        sevenZFile.closeArchiveEntry();
        sevenZFile.close();
    }
}