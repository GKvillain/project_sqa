package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.util.Arrays;

public class TarArchiveInputStreamTest {

    // ========== Normal cases ==========

    @Test
    // Tests that getNextEntry returns a non-null entry for a normal file and null at end.
    public void testGetNextEntry_normalFile_returnsEntry() throws IOException {
        byte[] data = createSingleFileTar("test.txt", "Hello");
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry entry = (TarArchiveEntry) is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertNull(is.getNextEntry());
        is.close();
    }

    @Test
    // Tests reading file content through read method.
    public void testRead_normalContent_returnsCorrectData() throws IOException {
        String content = "Hello World";
        byte[] data = createSingleFileTar("file.txt", content);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        is.getNextEntry();
        byte[] buf = new byte[256];
        int len = is.read(buf);
        assertEquals(content.length(), len);
        assertEquals(content, new String(buf, 0, len));
        is.close();
    }

    @Test
    // Tests skip method to skip part of data.
    public void testSkip_normalContent_skipsCorrectly() throws IOException {
        String content = "0123456789";
        byte[] data = createSingleFileTar("skip.txt", content);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        is.getNextEntry();
        long skipped = is.skip(5);
        assertEquals(5, skipped);
        byte[] buf = new byte[256];
        int len = is.read(buf);
        assertEquals(5, len);
        assertEquals("56789", new String(buf, 0, len));
        is.close();
    }

    @Test
    // Tests available method returning correct number of bytes left.
    public void testAvailable_returnsRemainingBytes() throws IOException {
        String content = "1234567890";
        byte[] data = createSingleFileTar("avail.txt", content);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        is.getNextEntry();
        assertEquals(10, is.available());
        is.read(new byte[4]);
        assertEquals(6, is.available());
        is.close();
    }

    @Test
    // Tests getNextEntry for an entry with zero size.
    public void testGetNextEntry_emptyFile_returnsEntry() throws IOException {
        byte[] data = createSingleFileTar("empty.txt", "");
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry entry = (TarArchiveEntry) is.getNextEntry();
        assertNotNull(entry);
        assertEquals(0, entry.getSize());
        assertEquals(-1, is.read());
        is.close();
    }

    @Test
    // Tests sequential access to multiple entries.
    public void testGetNextEntry_multipleEntries_sequential() throws IOException {
        byte[] data = createMultiEntryTar(new String[]{"a.txt", "b.txt"},
                new byte[][]{"apple".getBytes(), "banana".getBytes()});
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry entry1 = (TarArchiveEntry) is.getNextEntry();
        assertNotNull(entry1);
        assertEquals("a.txt", entry1.getName());
        byte[] buf = new byte[256];
        int len1 = is.read(buf);
        assertEquals("apple", new String(buf, 0, len1));
        TarArchiveEntry entry2 = (TarArchiveEntry) is.getNextEntry();
        assertNotNull(entry2);
        assertEquals("b.txt", entry2.getName());
        int len2 = is.read(buf);
        assertEquals("banana", new String(buf, 0, len2));
        assertNull(is.getNextEntry());
        is.close();
    }

    // ========== Boundary cases ==========

    @Test
    // Tests file name longer than 100 bytes (triggers GNU long name extension).
    public void testGetNextEntry_longName_returnsCorrectName() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append('A');
        }
        String longName = sb.toString();
        byte[] data = createSingleFileTar(longName, "data");
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry entry = (TarArchiveEntry) is.getNextEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());
        is.close();
    }

    // ========== PAX header tests ==========

    @Test
    // Tests that a PAX header entry correctly modifies the following file entry.
    public void testGetNextEntry_paxHeaderEntry_returnsModifiedAttributes() throws IOException {
        byte[] data = createPaxAndFileTar("paxfile.txt", "content", "gname", "testgroup");
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry entry = (TarArchiveEntry) is.getNextEntry();
        assertNotNull(entry);
        assertEquals("testgroup", entry.getGroupName());
        byte[] buf = new byte[256];
        int len = is.read(buf);
        assertEquals("content", new String(buf, 0, len));
        is.close();
    }

    @Test
    // Defect detection: PAX header at end of archive should not cause exception.
    // Bug: getNextTarEntry throws NullPointerException when processing PAX header
    // as the last entry. Fixed version returns null.
    public void testGetNextEntry_paxHeaderAtEnd_returnsNullWithoutException() throws IOException {
        byte[] data = createPaxHeaderOnlyTar();
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry entry = (TarArchiveEntry) is.getNextEntry();
        assertNull(entry);
        is.close();
    }

    // ========== Exception/invalid cases ==========

    @Test
    // Tests that an invalid tar header (wrong checksum) causes IOException.
    public void testGetNextEntry_invalidHeader_throwsIOException() throws IOException {
        byte[] data = createInvalidChecksumTar();
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        try {
            is.getNextEntry();
            fail("Expected IOException for invalid header");
        } catch (IOException e) {
            // expected
        }
        is.close();
    }

    // ========== General method tests ==========

    @Test
    // Tests skip with negative argument returns 0.
    public void testSkip_negative_returnsZero() throws IOException {
        byte[] data = createSingleFileTar("test.dat", "data");
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(data));
        is.getNextEntry();
        assertEquals(0, is.skip(-1));
        is.close();
    }

    @Test
    // Tests getRecordSize returns the default record size.
    public void testGetRecordSize_default_returns512() throws IOException {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(512, is.getRecordSize());
        is.close();
    }

    @Test
    // Tests markSupported returns false.
    public void testMarkSupported_returnsFalse() throws IOException {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(is.markSupported());
        is.close();
    }

    // ========== Helper methods ==========

    // Creates a tar archive with a single file entry.
    private static byte[] createSingleFileTar(String name, String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(content.length());
        taos.putArchiveEntry(entry);
        if (content.length() > 0) {
            taos.write(content.getBytes());
        }
        taos.closeArchiveEntry();
        taos.close();
        return baos.toByteArray();
    }

    // Creates a tar archive with multiple entries.
    private static byte[] createMultiEntryTar(String[] names, byte[][] contents) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        for (int i = 0; i < names.length; i++) {
            TarArchiveEntry entry = new TarArchiveEntry(names[i]);
            entry.setSize(contents[i] != null ? contents[i].length : 0);
            taos.putArchiveEntry(entry);
            if (contents[i] != null && contents[i].length > 0) {
                taos.write(contents[i]);
            }
            taos.closeArchiveEntry();
        }
        taos.close();
        return baos.toByteArray();
    }

    // Creates a tar archive consisting only of a PAX header (type 'x') with size 0
    // followed by two end-of-archive records.
    private static byte[] createPaxHeaderOnlyTar() {
        byte[] header = createTarHeader("", 0, 'x');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(header);
            baos.write(new byte[512]);
            baos.write(new byte[512]);
        } catch (IOException e) {
            // not expected
        }
        return baos.toByteArray();
    }

    // Creates a tar archive with a PAX header (setting given key=value) followed by
    // a regular file entry.
    private static byte[] createPaxAndFileTar(String fileName, String fileContent,
                                              String attrKey, String attrValue) throws IOException {
        String paxLine = createPaxLine(attrKey, attrValue);
        byte[] paxData = paxLine.getBytes();
        int paxSize = paxData.length;

        byte[] paxHeader = createTarHeader("", paxSize, 'x');
        byte[] fileHeader = createTarHeader(fileName, fileContent.length(), '0');
        byte[] fileData = fileContent.getBytes();

        int paxPad = (512 - (paxSize % 512)) % 512;
        int filePad = (512 - (fileData.length % 512)) % 512;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(paxHeader);
            baos.write(paxData);
            if (paxPad > 0) baos.write(new byte[paxPad]);
            baos.write(fileHeader);
            baos.write(fileData);
            if (filePad > 0) baos.write(new byte[filePad]);
            baos.write(new byte[512]);
            baos.write(new byte[512]);
        } catch (IOException e) {
            // not expected
        }
        return baos.toByteArray();
    }

    // Creates a tar archive with a single file header that has an invalid checksum.
    private static byte[] createInvalidChecksumTar() {
        byte[] header = createTarHeader("bad.txt", 10, '0');
        // Corrupt checksum: set checksum bytes to spaces (does not match correct sum)
        Arrays.fill(header, 148, 156, (byte) ' ');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(header);
            baos.write(new byte[512]);
            baos.write(new byte[512]);
        } catch (IOException e) {
            // not expected
        }
        return baos.toByteArray();
    }

    // Creates a 512-byte tar header for given name, size, and type flag.
    private static byte[] createTarHeader(String name, long size, char typeFlag) {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 0);

        if (name != null && name.length() > 0) {
            byte[] nameBytes = name.getBytes();
            System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
        }

        String sizeStr = String.format("%012o", size);
        byte[] sizeBytes = sizeStr.getBytes();
        System.arraycopy(sizeBytes, 0, header, 124, Math.min(sizeBytes.length, 12));

        header[156] = (byte) typeFlag;

        System.arraycopy("ustar\0".getBytes(), 0, header, 257, 6);
        System.arraycopy("00".getBytes(), 0, header, 263, 2);

        setChecksum(header);
        return header;
    }

    // Sets the checksum field (bytes 148-155) of a tar header.
    private static void setChecksum(byte[] header) {
        Arrays.fill(header, 148, 156, (byte) ' ');
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += (header[i] & 0xFF);
        }
        String chkStr = String.format("%06o", sum);
        byte[] chkBytes = chkStr.getBytes();
        System.arraycopy(chkBytes, 0, header, 148, chkBytes.length);
        header[154] = ' ';
        header[155] = ' ';
    }

    // Creates a self-consistent PAX data line of the form: "LENGTH KEY=VALUE\n"
    private static String createPaxLine(String key, String value) {
        String lineWithoutLength = " " + key + "=" + value + "\n";
        int baseLen = lineWithoutLength.length();
        int L = baseLen + 1;
        while (true) {
            String lStr = Integer.toString(L);
            int total = lStr.length() + baseLen;
            if (total == L) {
                break;
            }
            L = total;
        }
        return L + " " + key + "=" + value + "\n";
    }
}