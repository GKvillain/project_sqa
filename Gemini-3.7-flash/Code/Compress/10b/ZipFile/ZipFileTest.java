package org.apache.commons.compress.archivers.zip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ZipFileTest {

    private File tempZipFile;
    private ZipFile zipFile;

    @Before
    public void setUp() throws Exception {
        tempZipFile = File.createTempFile("commons-compress-zipfile-test", ".zip");
        tempZipFile.deleteOnExit();
    }

    @After
    public void tearDown() {
        ZipFile.closeQuietly(zipFile);
        if (tempZipFile != null && tempZipFile.exists()) {
            tempZipFile.delete();
        }
    }

    private File createZipArchive(String[] entryNames, byte[][] contents, int method) throws IOException {
        FileOutputStream fos = new FileOutputStream(tempZipFile);
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(fos);
        for (int i = 0; i < entryNames.length; i++) {
            ZipArchiveEntry ze = new ZipArchiveEntry(entryNames[i]);
            ze.setMethod(method);
            if (method == ZipArchiveEntry.STORED) {
                ze.setSize(contents[i].length);
                ze.setCompressedSize(contents[i].length);
                java.util.zip.CRC32 crc = new java.util.zip.CRC32();
                crc.update(contents[i]);
                ze.setCrc(crc.getValue());
            }
            zaos.putArchiveEntry(ze);
            zaos.write(contents[i]);
            zaos.closeArchiveEntry();
        }
        zaos.close();
        return tempZipFile;
    }

    // Tests reading stored entries from a ZipFile instance
    @Test
    public void testGetInputStream_storedEntry_returnsCorrectData() throws Exception {
        byte[] data = "Hello Stored Content".getBytes("UTF-8");
        createZipArchive(new String[]{"stored.txt"}, new byte[][]{data}, ZipArchiveEntry.STORED);

        zipFile = new ZipFile(tempZipFile);
        ZipArchiveEntry entry = zipFile.getEntry("stored.txt");
        assertNotNull(entry);
        assertEquals(ZipArchiveEntry.STORED, entry.getMethod());

        InputStream is = zipFile.getInputStream(entry);
        assertNotNull(is);
        byte[] readBuffer = new byte[data.length];
        int bytesRead = is.read(readBuffer);
        assertEquals(data.length, bytesRead);
        assertArrayEquals(data, readBuffer);
        assertEquals(-1, is.read());
        is.close();
    }

    // Tests reading deflated entries from a ZipFile instance
    @Test
    public void testGetInputStream_deflatedEntry_returnsCorrectData() throws Exception {
        byte[] data = "Hello Deflated Compressed Content! Repeat repeat repeat repeat.".getBytes("UTF-8");
        createZipArchive(new String[]{"deflated.txt"}, new byte[][]{data}, ZipArchiveEntry.DEFLATED);

        zipFile = new ZipFile(tempZipFile.getAbsolutePath());
        ZipArchiveEntry entry = zipFile.getEntry("deflated.txt");
        assertNotNull(entry);
        assertEquals(ZipArchiveEntry.DEFLATED, entry.getMethod());

        InputStream is = zipFile.getInputStream(entry);
        assertNotNull(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        is.close();
        assertArrayEquals(data, baos.toByteArray());
    }

    // Tests getEntry with non-existent name
    @Test
    public void testGetEntry_nonExistingName_returnsNull() throws Exception {
        byte[] data = "content".getBytes("UTF-8");
        createZipArchive(new String[]{"file1.txt"}, new byte[][]{data}, ZipArchiveEntry.DEFLATED);

        zipFile = new ZipFile(tempZipFile);
        assertNull(zipFile.getEntry("non_existing_file.txt"));
    }

    // Tests getInputStream with entry not part of ZipFile
    @Test
    public void testGetInputStream_foreignEntry_returnsNull() throws Exception {
        byte[] data = "content".getBytes("UTF-8");
        createZipArchive(new String[]{"file1.txt"}, new byte[][]{data}, ZipArchiveEntry.DEFLATED);

        zipFile = new ZipFile(tempZipFile);
        ZipArchiveEntry foreignEntry = new ZipArchiveEntry("foreign.txt");
        assertNull(zipFile.getInputStream(foreignEntry));
    }

    // Tests getEntries returns all entries in archive
    @Test
    public void testGetEntries_multipleEntries_returnsAllEntries() throws Exception {
        String[] names = {"entry1.txt", "entry2.txt", "entry3.txt"};
        byte[][] contents = {
            "Content 1".getBytes("UTF-8"),
            "Content 2".getBytes("UTF-8"),
            "Content 3".getBytes("UTF-8")
        };
        createZipArchive(names, contents, ZipArchiveEntry.DEFLATED);

        zipFile = new ZipFile(tempZipFile);
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
        List<String> returnedNames = new ArrayList<String>();
        while (entries.hasMoreElements()) {
            returnedNames.add(entries.nextElement().getName());
        }

        assertEquals(3, returnedNames.size());
        assertEquals("entry1.txt", returnedNames.get(0));
        assertEquals("entry2.txt", returnedNames.get(1));
        assertEquals("entry3.txt", returnedNames.get(2));
    }

    // Tests getEntriesInPhysicalOrder returns sorted entries
    @Test
    public void testGetEntriesInPhysicalOrder_multipleEntries_returnsInPhysicalOrder() throws Exception {
        String[] names = {"first.txt", "second.txt"};
        byte[][] contents = {
            "First data".getBytes("UTF-8"),
            "Second data".getBytes("UTF-8")
        };
        createZipArchive(names, contents, ZipArchiveEntry.DEFLATED);

        zipFile = new ZipFile(tempZipFile);
        Enumeration<ZipArchiveEntry> physicalEntries = zipFile.getEntriesInPhysicalOrder();
        assertNotNull(physicalEntries);
        assertTrue(physicalEntries.hasMoreElements());
        assertEquals("first.txt", physicalEntries.nextElement().getName());
        assertTrue(physicalEntries.hasMoreElements());
        assertEquals("second.txt", physicalEntries.nextElement().getName());
        assertFalse(physicalEntries.hasMoreElements());
    }

    // Tests getEncoding returns configured encoding
    @Test
    public void testGetEncoding_customEncoding_returnsSpecifiedEncoding() throws Exception {
        createZipArchive(new String[]{"test.txt"}, new byte[][]{"test".getBytes("UTF-8")}, ZipArchiveEntry.DEFLATED);

        zipFile = new ZipFile(tempZipFile, "ISO-8859-1");
        assertEquals("ISO-8859-1", zipFile.getEncoding());
    }

    // Tests closeQuietly on null instance does nothing and throws no exception
    @Test
    public void testCloseQuietly_nullInstance_noExceptionThrown() {
        ZipFile.closeQuietly(null);
    }

    // Tests closeQuietly on valid open instance closes properly
    @Test
    public void testCloseQuietly_validInstance_closesSuccessfully() throws Exception {
        createZipArchive(new String[]{"test.txt"}, new byte[][]{"test".getBytes("UTF-8")}, ZipArchiveEntry.DEFLATED);
        zipFile = new ZipFile(tempZipFile);
        ZipFile.closeQuietly(zipFile);
    }

    // Tests canReadEntryData for standard supported method
    @Test
    public void testCanReadEntryData_supportedEntry_returnsTrue() throws Exception {
        createZipArchive(new String[]{"stored.txt"}, new byte[][]{"content".getBytes("UTF-8")}, ZipArchiveEntry.STORED);
        zipFile = new ZipFile(tempZipFile);
        ZipArchiveEntry entry = zipFile.getEntry("stored.txt");
        assertTrue(zipFile.canReadEntryData(entry));
    }

    // Tests constructor with non-zip corrupt file throws ZipException
    @Test(expected = ZipException.class)
    public void testConstructor_nonZipFile_throwsZipException() throws Exception {
        FileOutputStream fos = new FileOutputStream(tempZipFile);
        fos.write("This is not a zip file at all".getBytes("UTF-8"));
        fos.close();

        zipFile = new ZipFile(tempZipFile);
    }

    // Tests constructor with String path and encoding overload
    @Test
    public void testConstructor_stringPathWithEncoding_opensSuccessfully() throws Exception {
        createZipArchive(new String[]{"file.txt"}, new byte[][]{"data".getBytes("UTF-8")}, ZipArchiveEntry.DEFLATED);
        zipFile = new ZipFile(tempZipFile.getAbsolutePath(), "UTF-8");
        assertNotNull(zipFile.getEntry("file.txt"));
    }

    // Tests reading entry with Unicode Path Extra Field (regression for COMPRESS-10/COMPRESS-164)
    @Test
    public void testGetInputStream_unicodeExtraFieldPresent_entryResolvedAndReadSuccessfully() throws Exception {
        FileOutputStream fos = new FileOutputStream(tempZipFile);
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(fos);
        zaos.setEncoding("ISO-8859-1");

        ZipArchiveEntry ze = new ZipArchiveEntry("ascii_name.txt");
        ze.setMethod(ZipArchiveEntry.DEFLATED);
        UnicodePathExtraField upef = new UnicodePathExtraField("unicode_name_äöü.txt", "ascii_name.txt".getBytes("ISO-8859-1"));
        ze.addExtraField(upef);

        byte[] payload = "Unicode entry content payload".getBytes("UTF-8");
        zaos.putArchiveEntry(ze);
        zaos.write(payload);
        zaos.closeArchiveEntry();
        zaos.close();

        zipFile = new ZipFile(tempZipFile, "ISO-8859-1", true);
        ZipArchiveEntry resolvedEntry = zipFile.getEntry("unicode_name_äöü.txt");
        assertNotNull("Entry with unicode name should be found in nameMap", resolvedEntry);

        InputStream is = zipFile.getInputStream(resolvedEntry);
        assertNotNull("InputStream should not be null for resolved unicode entry", is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        int read;
        while ((read = is.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        is.close();
        assertArrayEquals(payload, baos.toByteArray());
    }

    // Tests reading single byte at a time through BoundedInputStream
    @Test
    public void testGetInputStream_readSingleBytes_readsFullContent() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        createZipArchive(new String[]{"bytes.bin"}, new byte[][]{data}, ZipArchiveEntry.STORED);

        zipFile = new ZipFile(tempZipFile);
        ZipArchiveEntry entry = zipFile.getEntry("bytes.bin");
        InputStream is = zipFile.getInputStream(entry);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], (byte) is.read());
        }
        assertEquals(-1, is.read());
        is.close();
    }
}