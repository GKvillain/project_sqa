package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.apache.commons.compress.utils.CharsetNames;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;

/**
 * Test class for TarArchiveInputStream.
 * Focuses on defect detection and key branch coverage.
 */
public class TarArchiveInputStreamTest {

    // Helper to create a simple tar entry header
    private byte[] createTarHeader(String name, long size, byte typeFlag) {
        byte[] header = new byte[512];
        // Name (offset 0, length 100)
        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
        // Mode (offset 100, length 8) - set to 0644 (octal)
        String mode = "0000644\0";
        System.arraycopy(mode.getBytes(), 0, header, 100, mode.length());
        // UID (offset 108, length 8) - set to 0
        String uid = "0000000\0";
        System.arraycopy(uid.getBytes(), 0, header, 108, uid.length());
        // GID (offset 116, length 8) - set to 0
        String gid = "0000000\0";
        System.arraycopy(gid.getBytes(), 0, header, 116, gid.length());
        // Size (offset 124, length 12) - octal format
        String sizeStr = String.format("%011o\0", size);
        System.arraycopy(sizeStr.getBytes(), 0, header, 124, sizeStr.length());
        // MTime (offset 136, length 12) - set to 0
        String mtime = "00000000000\0";
        System.arraycopy(mtime.getBytes(), 0, header, 136, mtime.length());
        // Checksum (offset 148, length 8) - filled with spaces for calculation
        for (int i = 148; i < 156; i++) {
            header[i] = (byte) ' ';
        }
        // TypeFlag (offset 156, length 1)
        header[156] = typeFlag;
        // Magic (offset 257, length 8) - ustar
        byte[] magic = "ustar\0".getBytes();
        System.arraycopy(magic, 0, header, 257, magic.length);
        // Version (offset 265, length 2) - "00"
        header[265] = '0';
        header[266] = '0';
        // Calculate checksum
        int sum = 0;
        for (byte b : header) {
            sum += b & 0xff;
        }
        String checksum = String.format("%06o\0", sum);
        System.arraycopy(checksum.getBytes(), 0, header, 148, checksum.length());
        return header;
    }

    // Helper to create a tar header with deliberately wrong checksum
    private byte[] createTarHeaderInvalidChecksum(String name, long size, byte typeFlag) {
        byte[] header = createTarHeader(name, size, typeFlag);
        // Corrupt checksum field to make it invalid
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '0';
        header[154] = '\0';
        // Recalculate checksum? no, we want it invalid.
        // Actually we just set all six digits to zero.
        // Need to set the trailing null byte properly.
        // Better approach: change a byte in the header after checksum calculation.
        // Let's just alter one byte in the name area to invalidate checksum.
        header[0] = (byte) 'X'; // modify name
        // Now we need to recompute checksum? No, we want it wrong.
        // But the existing checksum was computed with the original name.
        // So now checksum is wrong.
        return header;
    }

    // Helper to create a tar header for a long link name (GNU extension)
    // Returns the full byte sequence: long name entry header + data + normal header
    private byte[] createLongNameEntry(String longName, String normalName, long normalSize, byte[] normalData) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // Long name header (type 'L')
            byte[] longHeader = createTarHeader("././@LongLink", longName.length(), (byte) 'L');
            bos.write(longHeader);
            // Write long name data
            byte[] nameBytes = longName.getBytes("UTF-8");
            bos.write(nameBytes);
            // Padding to multiple of 512
            int pad = 512 - (nameBytes.length % 512);
            if (pad != 512) {
                for (int i = 0; i < pad; i++) {
                    bos.write(0);
                }
            }
            // Normal entry header
            byte[] normalHeader = createTarHeader(normalName, normalSize, (byte) '0');
            bos.write(normalHeader);
            if (normalData != null) {
                bos.write(normalData);
                // Padding for data
                int padData = 512 - (normalData.length % 512);
                if (padData != 512) {
                    for (int i = 0; i < padData; i++) {
                        bos.write(0);
                    }
                }
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Tests getNextTarEntry basic functionality
    @Test
    public void testGetNextTarEntry_normalEntry_returnsEntry() throws Exception {
        byte[] header = createTarHeader("test.txt", 5, (byte) '0');
        byte[] data = "hello".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(header);

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals("test.txt", entry.getName());
        assertEquals(5, entry.getSize());
    }

    // Tests getNextTarEntry when stream is at EOF
    @Test
    public void testGetNextTarEntry_eof_returnsNull() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        assertNull(tar.getNextTarEntry());
    }

    // Tests reading file content from a normal entry
    @Test
    public void testRead_normalEntry_readsData() throws Exception {
        byte[] header = createTarHeader("test.txt", 5, (byte) '0');
        byte[] data = "hello".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        byte[] buffer = new byte[10];
        int bytesRead = tar.read(buffer, 0, buffer.length);
        assertEquals(5, bytesRead);
        assertEquals("hello", new String(buffer, 0, bytesRead));
    }

    // Tests reading beyond entry boundary returns -1
    @Test
    public void testRead_pastEntryEnd_returnsMinus1() throws Exception {
        byte[] header = createTarHeader("test.txt", 2, (byte) '0');
        byte[] data = "hi".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        byte[] buffer = new byte[10];
        int bytesRead = tar.read(buffer, 0, buffer.length);
        assertEquals(2, bytesRead);
        bytesRead = tar.read(buffer, 0, buffer.length);
        assertEquals(-1, bytesRead);
    }

    // Tests skip method with valid skip amount
    @Test
    public void testSkip_positiveAmount_skipsCorrectly() throws Exception {
        byte[] header = createTarHeader("test.txt", 10, (byte) '0');
        byte[] data = "0123456789".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        long skipped = tar.skip(5);
        assertEquals(5, skipped);
        assertEquals(5, tar.available());
    }

    // Tests skip with zero amount
    @Test
    public void testSkip_zeroAmount_returnsZero() throws Exception {
        byte[] header = createTarHeader("test.txt", 10, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        long skipped = tar.skip(0);
        assertEquals(0, skipped);
    }

    // Tests skip with negative amount
    @Test
    public void testSkip_negativeAmount_returnsZero() throws Exception {
        byte[] header = createTarHeader("test.txt", 10, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        long skipped = tar.skip(-5);
        assertEquals(0, skipped);
    }

    // Tests skip more than available
    @Test
    public void testSkip_moreThanAvailable_skipsToEnd() throws Exception {
        byte[] header = createTarHeader("test.txt", 5, (byte) '0');
        byte[] data = "hello".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        long skipped = tar.skip(100);
        assertEquals(5, skipped);
        assertEquals(0, tar.available());
    }

    // Tests available method returns remaining entry size
    @Test
    public void testAvailable_afterRead_returnsRemaining() throws Exception {
        byte[] header = createTarHeader("test.txt", 10, (byte) '0');
        byte[] data = "0123456789".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        assertEquals(10, tar.available());
        byte[] buffer = new byte[4];
        tar.read(buffer, 0, 4);
        assertEquals(6, tar.available());
    }

    // Tests markSupported returns false
    @Test
    public void testMarkSupported_returnsFalse() {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        assertFalse(tar.markSupported());
    }

    // Tests getRecordSize returns configured value
    @Test
    public void testGetRecordSize_returnsConfiguredValue() {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input, 512, 512);
        assertEquals(512, tar.getRecordSize());
    }

    // Tests constructor with blockSize parameter
    @Test
    public void testConstructor_blockSize_initializesCorrectly() {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input, 1024);
        assertEquals(512, tar.getRecordSize());
    }

    // Tests constructor with encoding parameter
    @Test
    public void testConstructor_encoding_initializesCorrectly() throws Exception {
        byte[] header = createTarHeader("test.txt", 0, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input, "UTF-8");
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals("test.txt", entry.getName());
    }

    // Tests getNextEntry returns TarArchiveEntry
    @Test
    public void testGetNextEntry_returnsTarEntry() throws Exception {
        byte[] header = createTarHeader("test.txt", 0, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        org.apache.commons.compress.archivers.ArchiveEntry entry = tar.getNextEntry();
        assertTrue(entry instanceof TarArchiveEntry);
    }

    // Tests canReadEntryData for regular file
    @Test
    public void testCanReadEntryData_regularFile_returnsTrue() throws Exception {
        byte[] header = createTarHeader("test.txt", 0, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertTrue(tar.canReadEntryData(entry));
    }

    // Tests matches method with valid tar header
    @Test
    public void testMatches_validTarHeader_returnsTrue() {
        byte[] header = createTarHeader("test.txt", 0, (byte) '0');
        assertTrue(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests isEOFRecord with zeroed record
    @Test
    public void testMatches_zeroedRecord_returnsFalse() {
        byte[] zeroed = new byte[512];
        assertFalse(TarArchiveInputStream.matches(zeroed, zeroed.length));
    }

    // Tests multiple entries reading
    @Test
    public void testGetNextTarEntry_multipleEntries_readsAll() throws Exception {
        byte[] header1 = createTarHeader("file1.txt", 3, (byte) '0');
        byte[] data1 = "abc".getBytes();
        byte[] header2 = createTarHeader("file2.txt", 3, (byte) '0');
        byte[] data2 = "def".getBytes();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header1);
        bos.write(data1);
        bos.write(header2);
        bos.write(data2);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());

        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals("file1.txt", entry.getName());
        byte[] buffer = new byte[3];
        tar.read(buffer, 0, 3);
        assertEquals("abc", new String(buffer));
        
        entry = tar.getNextTarEntry();
        assertEquals("file2.txt", entry.getName());
        tar.read(buffer, 0, 3);
        assertEquals("def", new String(buffer));
    }

    // Tests close method
    @Test
    public void testClose_closesUnderlyingStream() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.close();
        // After close, reads should throw IOException
        byte[] buffer = new byte[10];
        try {
            tar.read(buffer, 0, 10);
            fail("Expected IOException after close");
        } catch (IOException e) {
            // Expected
        }
    }

    // ========== New tests for uncovered branches ==========

    // Tests getNextTarEntry with invalid checksum (should throw IOException)
    @Test
    public void testGetNextTarEntry_invalidChecksum_throwsIOException() throws Exception {
        byte[] header = createTarHeaderInvalidChecksum("test.txt", 5, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        try {
            tar.getNextTarEntry();
            fail("Expected IOException due to invalid checksum");
        } catch (IOException e) {
            // Expected
        }
    }

    // Tests getNextTarEntry with directory entry (type '5')
    @Test
    public void testGetNextTarEntry_directoryEntry_returnsEntry() throws Exception {
        byte[] header = createTarHeader("mydir", 0, (byte) '5');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals("mydir", entry.getName());
        assertTrue(entry.isDirectory());
    }

    // Tests getNextTarEntry with symbolic link entry (type '2')
    @Test
    public void testGetNextTarEntry_symbolicLinkEntry_returnsEntry() throws Exception {
        byte[] header = createTarHeader("linkname", 0, (byte) '2');
        // For symlink, link name is stored in the name field for ustar? Actually symlink target is stored in linkName field (offset 157, length 100).
        // We need to set the link target name in the header.
        String linkTarget = "targetfile";
        byte[] targetBytes = linkTarget.getBytes();
        System.arraycopy(targetBytes, 0, header, 157, Math.min(targetBytes.length, 100));
        // Recompute checksum because we changed header
        int sum = 0;
        for (byte b : header) {
            sum += b & 0xff;
        }
        String checksum = String.format("%06o\0", sum);
        System.arraycopy(checksum.getBytes(), 0, header, 148, checksum.length());

        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals("linkname", entry.getName());
        assertTrue(entry.isSymbolicLink());
        assertEquals("targetfile", entry.getLinkName());
    }

    // Tests getNextTarEntry with GNU long name extension (type 'L')
    @Test
    public void testGetNextTarEntry_longNameEntry_returnsEntry() throws Exception {
        String longName = "this/is/a/very/long/path/that/exceeds/100/characters/abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        String normalName = "short.txt";
        byte[] normalData = "data".getBytes();
        byte[] archive = createLongNameEntry(longName, normalName, normalData.length, normalData);
        ByteArrayInputStream input = new ByteArrayInputStream(archive);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals(longName, entry.getName());
        assertEquals(4, entry.getSize());
        byte[] buffer = new byte[10];
        int bytesRead = tar.read(buffer, 0, buffer.length);
        assertEquals(4, bytesRead);
        assertEquals("data", new String(buffer, 0, bytesRead));
    }

    // Tests getNextTarEntry with size zero entry: read returns -1 immediately
    @Test
    public void testGetNextTarEntry_sizeZeroEntry_readReturnsMinusOne() throws Exception {
        byte[] header = createTarHeader("empty.txt", 0, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals(0, entry.getSize());
        byte[] buffer = new byte[10];
        int bytesRead = tar.read(buffer, 0, buffer.length);
        assertEquals(-1, bytesRead);
    }

    // Tests available before any getNextTarEntry
    @Test
    public void testAvailable_beforeGetNextEntry_returnsZero() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        assertEquals(0, tar.available());
    }

    // Tests read with offset and length not starting at 0
    @Test
    public void testRead_withOffsetAndLength_notFromZero() throws Exception {
        byte[] header = createTarHeader("test.txt", 5, (byte) '0');
        byte[] data = "hello".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        byte[] buffer = new byte[10];
        // fill buffer with dummy
        buffer[0] = 'x'; buffer[1] = 'y';
        int bytesRead = tar.read(buffer, 2, 3);
        assertEquals(3, bytesRead);
        assertEquals('x', buffer[0]);
        assertEquals('y', buffer[1]);
        assertEquals("hel", new String(buffer, 2, 3));
    }

    // Tests skip after partial read then read remaining
    @Test
    public void testSkip_afterRead_skipRemaining() throws Exception {
        byte[] header = createTarHeader("test.txt", 10, (byte) '0');
        byte[] data = "abcdefghij".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.getNextTarEntry();
        byte[] buffer = new byte[3];
        tar.read(buffer, 0, 3); // read "abc"
        long skipped = tar.skip(4); // skip "defg"
        assertEquals(4, skipped);
        tar.read(buffer, 0, 3); // read "hij"
        assertEquals("hij", new String(buffer));
        int remaining = tar.read(buffer, 0, 3);
        assertEquals(-1, remaining);
    }

    // Tests matches with buffer size less than 512
    @Test
    public void testMatches_invalidSize_returnsFalse() {
        byte[] header = createTarHeader("test.txt", 0, (byte) '0');
        assertFalse(TarArchiveInputStream.matches(header, 400));
    }

    // Tests getNextTarEntry after all entries have been read (returns null)
    @Test
    public void testGetNextTarEntry_afterAllEntries_returnsNull() throws Exception {
        byte[] header = createTarHeader("file1.txt", 3, (byte) '0');
        byte[] data = "abc".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertNotNull(entry);
        byte[] buffer = new byte[10];
        tar.read(buffer, 0, 3); // read to end of entry
        // Now next entry should be null (EOF)
        assertNull(tar.getNextTarEntry());
        // Calling again should still be null
        assertNull(tar.getNextTarEntry());
    }

    // Tests getNextTarEntry with two consecutive zero blocks (EOF marker)
    @Test
    public void testGetNextTarEntry_withZeroBlock_returnsNull() throws Exception {
        byte[] header = createTarHeader("file1.txt", 3, (byte) '0');
        byte[] data = "abc".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        // Append two blocks of zeros (1024 bytes)
        for (int i = 0; i < 1024; i++) {
            bos.write(0);
        }
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertNotNull(entry);
        byte[] buffer = new byte[10];
        tar.read(buffer, 0, 3);
        // After reading entry, we have two zero blocks -> getNextTarEntry should return null
        assertNull(tar.getNextTarEntry());
    }

    // Tests getNextTarEntry with directory entry and canReadEntryData returns true (directory)
    @Test
    public void testCanReadEntryData_directory_returnsTrue() throws Exception {
        byte[] header = createTarHeader("mydir", 0, (byte) '5');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertTrue(tar.canReadEntryData(entry));
    }

    // Tests constructor with different encoding (Cp437)
    @Test
    public void testConstructor_encoding_cp437() throws Exception {
        byte[] header = createTarHeader("test.txt", 0, (byte) '0');
        ByteArrayInputStream input = new ByteArrayInputStream(header);
        TarArchiveInputStream tar = new TarArchiveInputStream(input, "Cp437");
        TarArchiveEntry entry = tar.getNextTarEntry();
        assertEquals("test.txt", entry.getName());
    }

    // Tests available after closing stream
    @Test
    public void testAvailable_afterClose_returnsZero() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tar = new TarArchiveInputStream(input);
        tar.close();
        assertEquals(0, tar.available());
    }

    private void assertNotNull(TarArchiveEntry entry) {
        if (entry == null) {
            throw new AssertionError("Expected non-null entry");
        }
    }
}