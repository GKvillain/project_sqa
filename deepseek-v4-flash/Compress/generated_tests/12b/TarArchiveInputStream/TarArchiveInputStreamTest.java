package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

public class TarArchiveInputStreamTest {

    // Helper to create a tar with one regular entry
    private byte[] createTar(String entryName, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry(entryName);
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    // Helper to create a tar with a GNU long name entry (>100 chars)
    private byte[] createTarWithGnuLongName() throws IOException {
        String longName = repeat("a", 150);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    // Helper to create a tar with a PAX long name entry (>100 chars)
    private byte[] createTarWithPaxLongName() throws IOException {
        String longName = repeat("b", 150);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    // Helper to concatenate two byte arrays
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    // Helper to repeat a character to build a long string (Java 8 compatible)
    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(count * s.length());
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    // Test normal entry reading
    @Test
    public void testGetNextTarEntry_normalEntry_returnsEntryWithExpectedName() throws IOException {
        byte[] data = "hello".getBytes("UTF-8");
        byte[] tar = createTar("test.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        byte[] buf = new byte[data.length];
        int bytesRead = is.read(buf);
        assertEquals(data.length, bytesRead);
        assertArrayEquals(data, buf);
        is.close();
    }

    // Test GNU long name entry
    @Test
    public void testGetNextTarEntry_gnuLongName_returnsCorrectName() throws IOException {
        byte[] tar = createTarWithGnuLongName();
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(repeat("a", 150), entry.getName());
        is.close();
    }

    // Test PAX long name entry
    @Test
    public void testGetNextTarEntry_paxLongName_appliesPathAttribute() throws IOException {
        byte[] tar = createTarWithPaxLongName();
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(repeat("b", 150), entry.getName());
        is.close();
    }

    // Test reading multiple entries in sequence
    @Test
    public void testGetNextTarEntry_multipleEntries_returnsCorrectSequence() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry e1 = new TarArchiveEntry("first.txt");
        e1.setSize(0);
        tos.putArchiveEntry(e1);
        tos.closeArchiveEntry();
        TarArchiveEntry e2 = new TarArchiveEntry("second.txt");
        e2.setSize(0);
        tos.putArchiveEntry(e2);
        tos.closeArchiveEntry();
        tos.close();
        byte[] tar = bos.toByteArray();
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        assertEquals("first.txt", is.getNextTarEntry().getName());
        assertEquals("second.txt", is.getNextTarEntry().getName());
        assertNull(is.getNextTarEntry());
        is.close();
    }

    // Test read returns -1 when entry is fully consumed
    @Test
    public void testRead_whenEntryFullyRead_returnsMinusOne() throws IOException {
        byte[] data = "12345".getBytes("UTF-8");
        byte[] tar = createTar("test.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        is.getNextTarEntry();
        byte[] buf = new byte[10];
        assertEquals(5, is.read(buf));
        assertEquals(-1, is.read(buf));
        is.close();
    }

    // Test available before any read returns entry size
    @Test
    public void testAvailable_beforeRead_returnsEntrySize() throws IOException {
        byte[] data = "12345".getBytes("UTF-8");
        byte[] tar = createTar("test.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        is.getNextTarEntry();
        assertEquals(data.length, is.available());
        is.close();
    }

    // Test available after partial read returns remaining bytes
    @Test
    public void testAvailable_afterPartialRead_returnsRemaining() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        byte[] tar = createTar("test.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        is.getNextTarEntry();
        byte[] buf = new byte[4];
        is.read(buf);
        assertEquals(6, is.available());
        is.close();
    }

    // Test skip functionality
    @Test
    public void testSkip_skipsCorrectNumberOfBytes() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        byte[] tar = createTar("test.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        is.getNextTarEntry();
        assertEquals(5, is.skip(5));
        assertEquals(5, is.available());
        byte[] buf = new byte[10];
        int read = is.read(buf);
        assertEquals(5, read);
        assertEquals("67890", new String(buf, 0, 5, "UTF-8"));
        is.close();
    }

    // Test getNextEntry returns null after EOF
    @Test
    public void testGetNextEntry_afterEOF_returnsNull() throws IOException {
        byte[] data = "test".getBytes("UTF-8");
        byte[] tar = createTar("test.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        is.getNextTarEntry();
        assertNotNull(is.getNextEntry());
        assertNull(is.getNextEntry());
        is.close();
    }

    // Test matches with valid POSIX signature
    @Test
    public void testMatches_validTarSignature_returnsTrue() {
        byte[] sig = new byte[512];
        System.arraycopy("ustar\0".getBytes(), 0, sig, 257, 6);
        System.arraycopy("00".getBytes(), 0, sig, 263, 2);
        assertTrue(TarArchiveInputStream.matches(sig, 512));
    }

    // Test matches with invalid signature
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        byte[] sig = new byte[512];
        assertFalse(TarArchiveInputStream.matches(sig, 512));
    }

    // Test canReadEntryData for a normal entry (not sparse)
    @Test
    public void testCanReadEntryData_normalEntry_returnsTrue() {
        TarArchiveEntry entry = new TarArchiveEntry("normal.txt");
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(is.canReadEntryData(entry));
    }

    // Test parsePaxHeaders with valid single header
    @Test
    public void testParsePaxHeaders_validInput_returnsMap() throws IOException {
        String paxData = "15 path=./file\n";
        Reader reader = new StringReader(paxData);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = is.parsePaxHeaders(reader);
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertEquals("./file", headers.get("path"));
    }

    // Test parsePaxHeaders with multiple headers
    @Test
    public void testParsePaxHeaders_multipleHeaders_returnsAll() throws IOException {
        String paxData = "15 path=./file\n14 size=12345\n";
        Reader reader = new StringReader(paxData);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = is.parsePaxHeaders(reader);
        assertEquals("./file", headers.get("path"));
        assertEquals("12345", headers.get("size"));
    }

    // Test parsePaxHeaders with malformed input (declared length too long)
    @Test(expected = IOException.class)
    public void testParsePaxHeaders_malformedInput_throwsIOException() throws IOException {
        String paxData = "99 path=test\n";
        Reader reader = new StringReader(paxData);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        is.parsePaxHeaders(reader);
    }

    // Test close closes underlying stream
    @Test(expected = IOException.class)
    public void testClose_closesUnderlyingStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream is = new TarArchiveInputStream(bais);
        is.close();
        is.read(new byte[10]);
    }

    // Test read with internal readBuf (data larger than record, partial record remaining)
    @Test
    public void testRead_withReadBuf_usesReadBufThenBuffer() throws IOException {
        byte[] data = new byte[2000];
        Arrays.fill(data, (byte)'X');
        byte[] tar = createTar("large.txt", data);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        is.getNextTarEntry();
        byte[] first = new byte[1000];
        assertEquals(1000, is.read(first));
        byte[] second = new byte[1000];
        assertEquals(1000, is.read(second));
        assertArrayEquals(data, concat(first, second));
        is.close();
    }
}