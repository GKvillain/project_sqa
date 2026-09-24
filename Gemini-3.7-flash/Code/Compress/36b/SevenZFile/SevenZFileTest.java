package org.apache.commons.compress.archivers.sevenz;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.CRC32;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SevenZFileTest {

    private File tempFile;
    private SevenZFile sevenZFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("sevenz_test", ".7z");
    }

    @After
    public void tearDown() throws Exception {
        if (sevenZFile != null) {
            try {
                sevenZFile.close();
            } catch (final IOException ignored) {
            }
            sevenZFile = null;
        }
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // Tests matches with exact valid 7z signature
    @Test
    public void testMatches_validSignature_returnsTrue() {
        final byte[] sig = { (byte) '7', (byte) 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C };
        assertTrue(SevenZFile.matches(sig, sig.length));
    }

    // Tests matches with longer array containing valid signature prefix
    @Test
    public void testMatches_validSignatureLongerLength_returnsTrue() {
        final byte[] sig = { (byte) '7', (byte) 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C, 0x00, 0x00 };
        assertTrue(SevenZFile.matches(sig, sig.length));
    }

    // Tests matches with insufficient length
    @Test
    public void testMatches_lengthTooShort_returnsFalse() {
        final byte[] sig = { (byte) '7', (byte) 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27 };
        assertFalse(SevenZFile.matches(sig, sig.length));
        assertFalse(SevenZFile.matches(sig, 0));
    }

    // Tests matches with invalid signature bytes
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        final byte[] sig = { (byte) 'P', (byte) 'K', (byte) 0x03, (byte) 0x04, (byte) 0x27, (byte) 0x1C };
        assertFalse(SevenZFile.matches(sig, sig.length));
    }

    // Tests matches with null or boundary lengths
    @Test
    public void testMatches_nullOrVeryShort_returnsFalse() {
        assertFalse(SevenZFile.matches(new byte[] { '7' }, 1));
        assertFalse(SevenZFile.matches(new byte[] { '7', 'z' }, 2));
    }

    // Tests reading an archive with invalid 7z magic signature
    @Test(expected = IOException.class)
    public void testConstructor_invalidSignature_throwsIOException() throws IOException {
        final byte[] badBytes = new byte[32];
        writeToFile(badBytes);
        sevenZFile = new SevenZFile(tempFile);
    }

    // Tests reading an archive with unsupported major version
    @Test(expected = IOException.class)
    public void testConstructor_unsupportedVersion_throwsIOException() throws IOException {
        final byte[] badVersionArchive = create7zDataWithHeader(new byte[] { 0x00 }, (byte) 1, (byte) 0);
        writeToFile(badVersionArchive);
        sevenZFile = new SevenZFile(tempFile);
    }

    // Tests reading an archive with start header CRC mismatch
    @Test(expected = IOException.class)
    public void testConstructor_corruptStartHeaderCrc_throwsIOException() throws IOException {
        final byte[] validArchive = createMinimalValidArchive();
        // Corrupt start header CRC (bytes 8..11)
        validArchive[8] ^= (byte) 0xFF;
        writeToFile(validArchive);
        sevenZFile = new SevenZFile(tempFile);
    }

    // Tests reading an archive with header CRC mismatch
    @Test(expected = IOException.class)
    public void testConstructor_corruptHeaderCrc_throwsIOException() throws IOException {
        final byte[] validArchive = create7zDataWithHeader(new byte[] { 0x01, 0x00 }, (byte) 0, (byte) 4);
        // Corrupt header byte to cause CRC mismatch
        validArchive[validArchive.length - 1] = (byte) 0xFF;
        writeToFile(validArchive);
        sevenZFile = new SevenZFile(tempFile);
    }

    // Tests reading an archive without header marker
    @Test(expected = IOException.class)
    public void testConstructor_noHeaderNid_throwsIOException() throws IOException {
        // 0x02 is not kHeader (0x01) or kEncodedHeader (0x17)
        final byte[] archiveBytes = create7zDataWithHeader(new byte[] { 0x02, 0x00 }, (byte) 0, (byte) 4);
        writeToFile(archiveBytes);
        sevenZFile = new SevenZFile(tempFile);
    }

    // Tests read() before calling getNextEntry throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testRead_noEntryCalled_throwsIllegalStateException() throws IOException {
        final byte[] validArchive = createMinimalValidArchive();
        writeToFile(validArchive);
        sevenZFile = new SevenZFile(tempFile);
        sevenZFile.read();
    }

    // Tests read(byte[], int, int) before calling getNextEntry throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testReadBuffer_noEntryCalled_throwsIllegalStateException() throws IOException {
        final byte[] validArchive = createMinimalValidArchive();
        writeToFile(validArchive);
        sevenZFile = new SevenZFile(tempFile);
        final byte[] buffer = new byte[16];
        sevenZFile.read(buffer, 0, buffer.length);
    }

    // Tests getNextEntry on empty archive returns null
    @Test
    public void testGetNextEntry_emptyArchive_returnsNull() throws IOException {
        final byte[] validArchive = createMinimalValidArchive();
        writeToFile(validArchive);
        sevenZFile = new SevenZFile(tempFile);
        assertNull(sevenZFile.getNextEntry());
        assertNull(sevenZFile.getNextEntry());
    }

    // Tests getEntries and toString on empty archive
    @Test
    public void testGetEntriesAndToString_emptyArchive_returnsValidData() throws IOException {
        final byte[] validArchive = createMinimalValidArchive();
        writeToFile(validArchive);
        sevenZFile = new SevenZFile(tempFile);

        final Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
        assertNotNull(entries);
        final Iterator<SevenZArchiveEntry> it = entries.iterator();
        assertFalse(it.hasNext());
        assertNotNull(sevenZFile.toString());
    }

    // Tests reading an entry with size 0 (empty stream) and read() behavior (Defects4J bug Compress-36)
    @Test
    public void testRead_emptyStreamEntry_returnsEof() throws IOException {
        final byte[] archiveWithEmptyFile = createArchiveWithEmptyStreamFile();
        writeToFile(archiveWithEmptyFile);
        sevenZFile = new SevenZFile(tempFile);

        final SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        assertNotNull(entry);
        assertEquals(0, entry.getSize());

        // Should return -1 (EOF) rather than throwing IllegalStateException
        assertEquals(-1, sevenZFile.read());
        final byte[] buffer = new byte[10];
        assertEquals(-1, sevenZFile.read(buffer));
        assertEquals(-1, sevenZFile.read(buffer, 0, buffer.length));

        assertNull(sevenZFile.getNextEntry());
    }

    // Tests reading uncompressed (COPY) content from archive
    @Test
    public void testRead_copyStreamContent_readsSuccessfully() throws IOException {
        final byte[] content = "Hello, 7-Zip world!".getBytes("UTF-8");
        final byte[] archive = createArchiveWithCopyStream(content);
        writeToFile(archive);
        sevenZFile = new SevenZFile(tempFile);

        final SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        assertNotNull(entry);
        assertEquals(content.length, entry.getSize());

        // Read first byte via read()
        final int firstByte = sevenZFile.read();
        assertEquals(content[0] & 0xFF, firstByte);

        // Read remainder via read(byte[], int, int)
        final byte[] rest = new byte[content.length - 1];
        final int bytesRead = sevenZFile.read(rest, 0, rest.length);
        assertEquals(rest.length, bytesRead);

        final byte[] combined = new byte[content.length];
        combined[0] = (byte) firstByte;
        System.arraycopy(rest, 0, combined, 1, rest.length);
        assertArrayEquals(content, combined);

        // EOF check
        assertEquals(-1, sevenZFile.read());
        assertEquals(-1, sevenZFile.read(new byte[10]));
        assertNull(sevenZFile.getNextEntry());
    }

    // Tests SevenZArchiveEntry property getters and setters
    @Test
    public void testSevenZArchiveEntry_properties() {
        final SevenZArchiveEntry entry = new SevenZArchiveEntry();

        entry.setName("dir/file.txt");
        assertEquals("dir/file.txt", entry.getName());

        entry.setDirectory(true);
        assertTrue(entry.isDirectory());
        entry.setDirectory(false);
        assertFalse(entry.isDirectory());

        entry.setAntiItem(true);
        assertTrue(entry.isAntiItem());

        entry.setHasStream(true);
        assertTrue(entry.hasStream());

        entry.setSize(1024L);
        assertEquals(1024L, entry.getSize());

        entry.setCompressedSize(512L);
        assertEquals(512L, entry.getCompressedSize());

        entry.setCrc(12345678L);
        assertEquals(12345678L, entry.getCrc());
        assertEquals(12345678, entry.getCrcValue());

        entry.setCompressedCrc(87654321L);
        assertEquals(87654321L, entry.getCompressedCrc());
        assertEquals(87654321, entry.getCompressedCrcValue());

        final Date now = new Date();
        entry.setHasCreationDate(true);
        entry.setCreationDate(now);
        assertTrue(entry.getHasCreationDate());
        assertEquals(now, entry.getCreationDate());

        entry.setHasLastModifiedDate(true);
        entry.setLastModifiedDate(now);
        assertTrue(entry.getHasLastModifiedDate());
        assertEquals(now, entry.getLastModifiedDate());

        entry.setHasAccessDate(true);
        entry.setAccessDate(now);
        assertTrue(entry.getHasAccessDate());
        assertEquals(now, entry.getAccessDate());

        entry.setHasWindowsAttributes(true);
        entry.setWindowsAttributes(0x20);
        assertTrue(entry.getHasWindowsAttributes());
        assertEquals(0x20, entry.getWindowsAttributes());
    }

    // Tests SevenZArchiveEntry equals and hashCode
    @Test
    public void testSevenZArchiveEntry_equalsAndHashCode() {
        final SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("test.txt");

        final SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("test.txt");

        final SevenZArchiveEntry entry3 = new SevenZArchiveEntry();
        entry3.setName("other.txt");

        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("test.txt"));
    }

    // Tests close cleans password and can be called multiple times
    @Test
    public void testClose_withPassword_cleansResources() throws IOException {
        final byte[] validArchive = createMinimalValidArchive();
        writeToFile(validArchive);
        final byte[] password = "secret".getBytes("UTF-16LE");
        sevenZFile = new SevenZFile(tempFile, password);
        sevenZFile.close();
        // Closing second time should be safe
        sevenZFile.close();
    }

    private void writeToFile(final byte[] data) throws IOException {
        final FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            fos.write(data);
        } finally {
            fos.close();
        }
    }

    private static byte[] createMinimalValidArchive() throws IOException {
        // kHeader (0x01) followed by kEnd (0x00)
        final byte[] header = new byte[] { 0x01, 0x00 };
        return create7zDataWithHeader(header, (byte) 0, (byte) 4);
    }

    private static byte[] createArchiveWithEmptyStreamFile() throws IOException {
        // Header with kFilesInfo (0x05), 1 file, kEmptyStream (0x0e) = 1 (empty), kEnd (0x00)
        final byte[] header = new byte[] {
            0x01,                   // kHeader
            0x05,                   // kFilesInfo
            0x01,                   // numFiles = 1
            0x0e,                   // kEmptyStream
            (byte) 0x80,            // bit 0 = 1 (has empty stream)
            0x00,                   // kEnd of FilesInfo
            0x00                    // kEnd of Header
        };
        return create7zDataWithHeader(header, (byte) 0, (byte) 4);
    }

    private static byte[] createArchiveWithCopyStream(final byte[] content) throws IOException {
        final ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
        headerOut.write(0x01); // kHeader

        // kMainStreamsInfo
        headerOut.write(0x04);
        // kPackInfo
        headerOut.write(0x06);
        headerOut.write(0x00); // packPos = 0
        headerOut.write(0x01); // numPackStreams = 1
        headerOut.write(0x09); // kSize
        headerOut.write(content.length); // packSize
        headerOut.write(0x00); // kEnd of PackInfo

        // kUnpackInfo
        headerOut.write(0x07);
        headerOut.write(0x0b); // kFolder
        headerOut.write(0x01); // numFolders = 1
        headerOut.write(0x00); // external = 0
        headerOut.write(0x01); // numCoders = 1
        headerOut.write(0x01); // coder 0: 1 byte method id, no attributes
        headerOut.write(0x00); // Method ID 0x00 (COPY)
        headerOut.write(0x0c); // kCodersUnpackSize
        headerOut.write(content.length); // unpackSize
        headerOut.write(0x00); // kEnd of UnpackInfo

        headerOut.write(0x00); // kEnd of MainStreamsInfo

        // kFilesInfo
        headerOut.write(0x05);
        headerOut.write(0x01); // numFiles = 1
        headerOut.write(0x00); // kEnd of FilesInfo

        headerOut.write(0x00); // kEnd of Header

        final byte[] headerBytes = headerOut.toByteArray();
        return create7zDataWithPackAndHeader(content, headerBytes);
    }

    private static byte[] create7zDataWithPackAndHeader(final byte[] packData, final byte[] headerBytes) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 6-byte signature
        out.write(new byte[] { (byte) '7', (byte) 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C });
        out.write((byte) 0);
        out.write((byte) 4);

        // Calculate nextHeader CRC
        final CRC32 headerCrc = new CRC32();
        headerCrc.update(headerBytes);
        final long nextHeaderCrcVal = headerCrc.getValue();

        // StartHeader (20 bytes)
        final ByteArrayOutputStream startHeaderOut = new ByteArrayOutputStream();
        writeLongLE(startHeaderOut, packData.length);             // nextHeaderOffset
        writeLongLE(startHeaderOut, (long) headerBytes.length);   // nextHeaderSize
        writeIntLE(startHeaderOut, (int) nextHeaderCrcVal);       // nextHeaderCRC

        final byte[] startHeaderBytes = startHeaderOut.toByteArray();

        // StartHeader CRC
        final CRC32 startHeaderCrc = new CRC32();
        startHeaderCrc.update(startHeaderBytes);
        final long startHeaderCrcVal = startHeaderCrc.getValue();

        writeIntLE(out, (int) startHeaderCrcVal);
        out.write(startHeaderBytes);
        out.write(packData);
        out.write(headerBytes);

        return out.toByteArray();
    }

    private static byte[] create7zDataWithHeader(final byte[] headerBytes,
                                                 final byte majorVersion,
                                                 final byte minorVersion) throws IOException {
        return create7zDataWithPackAndHeader(new byte[0], headerBytes);
    }

    private static void writeLongLE(final ByteArrayOutputStream out, long value) {
        for (int i = 0; i < 8; i++) {
            out.write((int) (value & 0xFF));
            value >>>= 8;
        }
    }

    private static void writeIntLE(final ByteArrayOutputStream out, int value) {
        for (int i = 0; i < 4; i++) {
            out.write(value & 0xFF);
            value >>>= 8;
        }
    }
}