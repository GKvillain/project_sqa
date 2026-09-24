package org.apache.commons.compress.archivers.ar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArArchiveInputStreamTest {

    private static final byte[] VALID_SIGNATURE = new byte[]{0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a};

    // Helper method to construct a valid entry block
    private byte[] createArArchive(String entryName, long size, byte[] content) {
        StringBuilder sb = new StringBuilder();
        sb.append(ArArchiveEntry.HEADER); // "!<arch>\n"
        
        // name (16 bytes)
        sb.append(String.format("%-16s", entryName));
        // lastmodified (12 bytes)
        sb.append(String.format("%-12s", "0"));
        // userid (6 bytes)
        sb.append(String.format("%-6s", "0"));
        // groupid (6 bytes)
        sb.append(String.format("%-6s", "0"));
        // filemode (8 bytes)
        sb.append(String.format("%-8s", "100644"));
        // length (10 bytes)
        sb.append(String.format("%-10s", String.valueOf(size)));
        // trailer ("`\n")
        sb.append(ArArchiveEntry.TRAILER);

        byte[] headerBytes = sb.toString().getBytes();
        int padding = (content != null && (content.length % 2 != 0)) ? 1 : 0;
        int contentLength = (content != null) ? content.length : 0;
        byte[] result = new byte[headerBytes.length + contentLength + padding];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        if (content != null && content.length > 0) {
            System.arraycopy(content, 0, result, headerBytes.length, content.length);
        }
        if (padding > 0) {
            result[result.length - 1] = 0x0a;
        }
        return result;
    }

    // Tests matches() with exact valid signature
    @Test
    public void testMatches_validSignature_returnsTrue() {
        assertTrue(ArArchiveInputStream.matches(VALID_SIGNATURE, 8));
    }

    // Tests matches() with longer valid signature
    @Test
    public void testMatches_longerSignature_returnsTrue() {
        byte[] extended = new byte[]{0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a, 0x00, 0x01};
        assertTrue(ArArchiveInputStream.matches(extended, 10));
    }

    // Tests matches() with length less than 8
    @Test
    public void testMatches_shortLength_returnsFalse() {
        assertFalse(ArArchiveInputStream.matches(VALID_SIGNATURE, 7));
        assertFalse(ArArchiveInputStream.matches(VALID_SIGNATURE, 0));
    }

    // Tests matches() with invalid signature bytes
    @Test
    public void testMatches_invalidSignatureBytes_returnsFalse() {
        for (int i = 0; i < 8; i++) {
            byte[] invalid = VALID_SIGNATURE.clone();
            invalid[i] = (byte) (invalid[i] ^ 0xFF);
            assertFalse(ArArchiveInputStream.matches(invalid, 8));
        }
    }

    // Tests getNextArEntry() on empty stream when available is 0
    @Test
    public void testGetNextArEntry_emptyStream_returnsNull() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream in = new ArArchiveInputStream(emptyStream);
        assertNull(in.getNextArEntry());
        in.close();
    }

    // Tests getNextArEntry() and getNextEntry() with a valid single entry
    @Test
    public void testGetNextArEntry_validSingleEntry_returnsCorrectEntry() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] archiveData = createArArchive("test.txt", content.length, content);
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(archiveData));

        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(content.length, entry.getSize());

        byte[] readBuffer = new byte[content.length];
        int bytesRead = in.read(readBuffer);
        assertEquals(content.length, bytesRead);
        assertEquals("Hello World", new String(readBuffer));

        in.close();
    }

    // Tests getNextEntry() delegating to getNextArEntry()
    @Test
    public void testGetNextEntry_validEntry_returnsArchiveEntry() throws IOException {
        byte[] content = "data".getBytes();
        byte[] archiveData = createArArchive("file.dat", content.length, content);
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(archiveData));

        ArchiveEntry entry = in.getNextEntry();
        assertNotNull(entry);
        assertEquals("file.dat", entry.getName());
        assertEquals(content.length, entry.getSize());

        in.close();
    }

    // Tests reading single bytes using read()
    @Test
    public void testRead_singleBytes_readsCorrectly() throws IOException {
        byte[] content = "AB".getBytes();
        byte[] archiveData = createArArchive("ab.txt", content.length, content);
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(archiveData));

        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull(entry);

        int byte1 = in.read();
        assertEquals('A', byte1);
        int byte2 = in.read();
        assertEquals('B', byte2);

        in.close();
    }

    // Tests read(byte[], off, len)
    @Test
    public void testRead_bufferWithOffsetAndLen_readsCorrectly() throws IOException {
        byte[] content = "12345678".getBytes();
        byte[] archiveData = createArArchive("num.txt", content.length, content);
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(archiveData));

        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull(entry);

        byte[] buffer = new byte[16];
        int readCount = in.read(buffer, 2, 8);
        assertEquals(8, readCount);
        assertEquals("12345678", new String(buffer, 2, 8));

        in.close();
    }

    // Tests read() when EOF is reached on underlying stream
    @Test
    public void testRead_atEof_returnsNegativeOne() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream in = new ArArchiveInputStream(emptyStream);
        assertEquals(-1, in.read());
        assertEquals(-1, in.read(new byte[10], 0, 10));
        in.close();
    }

    // Tests header read failure when stream length is shorter than expected header
    @Test(expected = IOException.class)
    public void testGetNextArEntry_truncatedHeader_throwsException() throws IOException {
        byte[] truncatedHeader = "!<arc".getBytes();
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(truncatedHeader));
        in.getNextArEntry();
    }

    // Tests invalid header content
    @Test(expected = IOException.class)
    public void testGetNextArEntry_invalidHeaderContent_throwsException() throws IOException {
        byte[] badHeader = "!<wrong>\n".getBytes();
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(badHeader));
        in.getNextArEntry();
    }

    // Tests invalid trailer in entry header
    @Test(expected = IOException.class)
    public void testGetNextArEntry_invalidTrailer_throwsException() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(ArArchiveEntry.HEADER);
        sb.append(String.format("%-16s", "test.txt"));
        sb.append(String.format("%-12s", "0"));
        sb.append(String.format("%-6s", "0"));
        sb.append(String.format("%-6s", "0"));
        sb.append(String.format("%-8s", "100644"));
        sb.append(String.format("%-10s", "10"));
        sb.append("XX"); // Bad trailer instead of "`\n"

        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(sb.toString().getBytes()));
        in.getNextArEntry();
    }

    // Tests close() method called multiple times safely
    @Test
    public void testClose_multipleCalls_closesStreamOnce() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream in = new ArArchiveInputStream(emptyStream);
        in.close();
        in.close();
    }
}