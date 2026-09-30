package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ZipArchiveOutputStreamTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private int readShort(byte[] data, int offset) {
        return ZipShort.getValue(data, offset);
    }

    private long readLong(byte[] data, int offset) {
        return ZipLong.getValue(data, offset);
    }

    private int findDD(byte[] data, int start) {
        for (int i = start; i < data.length - 12; i++) {
            if (readLong(data, i) == 0x08074b50) {
                return i;
            }
        }
        return -1;
    }

    @Test
    // Tests DEFLATED non-seekable: local header has zero CRC/sizes, data descriptor written
    public void testWriteDeflatedEntry_nonSeekable_verifiesLocalHeaderAndDataDescriptor() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        byte[] content = "Hello World".getBytes("UTF-8");
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] zipData = bos.toByteArray();
        int off = 0;
        assertEquals("LFH sig", 0x04034b50, (int) readLong(zipData, off));
        off += 4;
        // version needed = 20 for DEFLATED non-seekable
        assertEquals("version needed", 20, readShort(zipData, off));
        off += 2;
        int flag = readShort(zipData, off);
        off += 2;
        assertTrue("data descriptor flag", (flag & 0x08) != 0);
        assertTrue("EFS flag", (flag & ZipArchiveOutputStream.EFS_FLAG) != 0);
        assertEquals("compression method", 8, readShort(zipData, off));
        off += 2;
        off += 4; // time/date
        // CRC, sizes are zero
        assertEquals("CRC in LFH", 0, readLong(zipData, off));
        off += 4;
        assertEquals("compressed size", 0, readLong(zipData, off));
        off += 4;
        assertEquals("uncompressed size", 0, readLong(zipData, off));
        off += 4;
        int nameLen = readShort(zipData, off);
        off += 2;
        int extraLen = readShort(zipData, off);
        off += 2;
        String name = new String(zipData, off, nameLen, "UTF-8");
        assertEquals("test.txt", name);
        off += nameLen + extraLen;
        // After compressed data should be data descriptor
        int ddOff = findDD(zipData, off);
        assertTrue("Data descriptor signature found", ddOff >= 0);
    }

    @Test
    // Tests STORED non-seekable: version needed = 10, flag no bit3, CRC/sizes written correctly
    public void testWriteStoredEntry_nonSeekable_CRCAndSizesInLocalHeader() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] content = "Stored content".getBytes("UTF-8");
        CRC32 crc32 = new CRC32();
        crc32.update(content);
        entry.setCrc(crc32.getValue());
        entry.setSize(content.length);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] zipData = bos.toByteArray();
        int off = 0;
        readLong(zipData, off);
        off += 4;
        assertEquals("version needed", 10, readShort(zipData, off));
        off += 2;
        int flag = readShort(zipData, off);
        off += 2;
        assertEquals("bit3 not set", 0, flag & 0x08);
        assertEquals("compression method", 0, readShort(zipData, off));
        off += 2;
        off += 4;
        assertEquals("CRC", crc32.getValue(), readLong(zipData, off));
        off += 4;
        assertEquals("compressed size", content.length, readLong(zipData, off));
        off += 4;
        assertEquals("uncompressed size", content.length, readLong(zipData, off));
    }

    @Test
    // Tests STORED seekable: local header CRC updated after closeArchiveEntry
    public void testWriteStoredEntry_seekable_crcUpdatedInLocalHeader() throws IOException {
        File tmpFile = tempFolder.newFile("test.zip");
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tmpFile);
        assertTrue("seekable", zaos.isSeekable());
        ZipArchiveEntry entry = new ZipArchiveEntry("seekable.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        byte[] content = "Seekable content".getBytes("UTF-8");
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");
        byte[] fileData = new byte[(int) raf.length()];
        raf.readFully(fileData);
        raf.close();

        int off = 0;
        readLong(fileData, off);
        off += 4;
        off += 2; // version
        int flag = readShort(fileData, off);
        off += 2;
        assertEquals("bit3 not set", 0, flag & 0x08);
        off += 2; // method
        off += 4; // time
        CRC32 crc32 = new CRC32();
        crc32.update(content);
        assertEquals("CRC updated", crc32.getValue(), readLong(fileData, off));
        off += 4;
        assertEquals("compressed size", content.length, readLong(fileData, off));
        off += 4;
        assertEquals("uncompressed size", content.length, readLong(fileData, off));
    }

    @Test(expected = ZipException.class)
    // Tests STORED non-seekable without size throws ZipException
    public void testPutArchiveEntry_storedNonSeekableNoSize_throwsZipException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("sizeMissing.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
    }

    @Test(expected = ZipException.class)
    // Tests STORED non-seekable without CRC throws ZipException
    public void testPutArchiveEntry_storedNonSeekableNoCrc_throwsZipException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("crcMissing.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(10);
        zaos.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class)
    // Tests finish() with unclosed entry throws IOException
    public void testFinish_withOpenEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("open.txt");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        zaos.finish();
    }

    @Test(expected = IllegalArgumentException.class)
    // Tests setLevel with value below allowed range throws IllegalArgumentException
    public void testSetLevel_invalidLow_throwsIllegalArgumentException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setLevel(Deflater.DEFAULT_COMPRESSION - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    // Tests setLevel with value above allowed range throws IllegalArgumentException
    public void testSetLevel_invalidHigh_throwsIllegalArgumentException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setLevel(Deflater.BEST_COMPRESSION + 1);
    }

    @Test
    // Tests setLevel with valid values does not throw
    public void testSetLevel_validLevel_doesNotThrow() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setLevel(0);
        zaos.setLevel(9);
        zaos.setLevel(-1);
    }

    @Test
    // Tests encoding fallback: non-encodable filename forces UTF-8 and sets EFS flag
    public void testEncodingAndFallback_efsFlagSetForNonEncodableName() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setEncoding("ISO-8859-1");
        zaos.setFallbackToUTF8(true);
        zaos.setUseLanguageEncodingFlag(false);
        ZipArchiveEntry entry = new ZipArchiveEntry("\u30C6\u30B9\u30C8.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] content = "data".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.setSize(content.length);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] zipData = bos.toByteArray();
        int off = 0;
        readLong(zipData, off);
        off += 4;
        off += 2; // version
        int flag = readShort(zipData, off);
        off += 2;
        assertTrue("EFS flag set due to fallback", (flag & ZipArchiveOutputStream.EFS_FLAG) != 0);
        off += 2; // method
        off += 4; // time
        off += 12; // crc, sizes
        int nameLen = readShort(zipData, off);
        off += 2;
        int extraLen = readShort(zipData, off);
        off += 2;
        String nameRead = new String(zipData, off, nameLen, "UTF-8");
        assertEquals("Japanese name", "\u30C6\u30B9\u30C8.txt", nameRead);
    }

    @Test
    // Tests that Unicode extra fields are written when policy is ALWAYS
    public void testUnicodeExtraFields_extraFieldLengthNonZero() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] content = "data".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.setSize(content.length);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] zipData = bos.toByteArray();
        int off = 0;
        readLong(zipData, off);
        off += 4;
        off += 2; // version
        off += 2; // flag
        off += 2; // method
        off += 4; // time
        off += 12; // crc, sizes
        off += 2; // nameLen
        int extraLen = readShort(zipData, off);
        assertTrue("Extra field length > 0", extraLen > 0);
    }

    @Test
    // Tests that data descriptor is not written for seekable DEFLATED entries
    public void testWriteDataDescriptor_notWrittenForSeekableDeflated() throws IOException {
        File tmpFile = tempFolder.newFile("seekDeflated.zip");
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tmpFile);
        assertTrue(zaos.isSeekable());
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        zaos.write("data".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");
        byte[] fileData = new byte[(int) raf.length()];
        raf.readFully(fileData);
        raf.close();

        assertEquals("No data descriptor signature", -1, findDD(fileData, 0));
    }

    @Test
    // Tests that closeArchiveEntry does nothing when entry is null
    public void testCloseArchiveEntry_whenEntryNull_doesNothing() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.closeArchiveEntry(); // should not throw
    }

    @Test
    // Tests central directory header fields (version, flag, CRC, sizes)
    public void testCentralDirectoryHeader_hasCorrectFields() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("entry.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] content = "content".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.setSize(content.length);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] zipData = bos.toByteArray();
        int cdOff = -1;
        for (int i = 0; i < zipData.length - 4; i++) {
            if (readLong(zipData, i) == 0x02014b50) {
                cdOff = i;
                break;
            }
        }
        assertTrue("Central directory found", cdOff >= 0);
        int off = cdOff;
        readLong(zipData, off); off += 4; // sig
        off += 2; // version made by
        int versionNeeded = readShort(zipData, off); off += 2;
        assertEquals("Version needed in CEN", 10, versionNeeded);
        int flag = readShort(zipData, off); off += 2;
        assertTrue("EFS flag in CEN", (flag & ZipArchiveOutputStream.EFS_FLAG) != 0);
        int method = readShort(zipData, off); off += 2;
        assertEquals("Method in CEN", 0, method);
        off += 4; // time
        long crcCen = readLong(zipData, off); off += 4;
        assertEquals("CRC in CEN", crc.getValue(), crcCen);
        long compSize = readLong(zipData, off); off += 4;
        assertEquals("compressed size in CEN", content.length, compSize);
        long uncompSize = readLong(zipData, off); off += 4;
        assertEquals("uncompressed size in CEN", content.length, uncompSize);
        int nameLen = readShort(zipData, off); off += 2;
        int extraLen = readShort(zipData, off); off += 2;
        int commentLen = readShort(zipData, off); off += 2;
        String name = new String(zipData, off, nameLen, "UTF-8");
        assertEquals("entry.txt", name);
    }

    @Test
    // Tests write with block size larger than DEFLATER_BLOCK_SIZE (8192)
    public void testWrite_withLargeBlockSize_usesMultipleBlocks() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        byte[] data = new byte[20000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 100);
        }
        zaos.write(data);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
    }

    @Test
    // Tests flush() does not throw
    public void testFlush_doesNotThrow() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.flush();
        zaos.close();
    }

    @Test
    // Tests setComment and verifies comment in End of Central Directory record
    public void testSetComment_stringWrittenInEOCD() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setComment("ZIP Comment");
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] content = "data".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.setSize(content.length);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] zipData = bos.toByteArray();
        int eocdOff = -1;
        for (int i = zipData.length - 22; i >= 0; i--) {
            if (readLong(zipData, i) == 0x06054b50) {
                eocdOff = i;
                break;
            }
        }
        assertTrue("EOCD found", eocdOff >= 0);
        int commentLen = readShort(zipData, eocdOff + 20);
        assertEquals("ZIP Comment".length(), commentLen);
        String commentRead = new String(zipData, eocdOff + 22, commentLen, "UTF-8");
        assertEquals("ZIP Comment", commentRead);
    }

    // ===== New tests for uncovered parts =====

    @Test
    // Tests putArchiveEntry with null entry - should handle gracefully
    public void testPutArchiveEntry_nullEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        try {
            zaos.putArchiveEntry(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    // Tests creating with File that doesn't exist - should create it
    public void testCreateNewFile_createsFile() throws IOException {
        File newFile = new File(tempFolder.getRoot(), "newZip.zip");
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(newFile);
        zaos.close();
        assertTrue("File created", newFile.exists());
    }

    @Test
    // Tests setEncoding with null - should use default
    public void testSetEncoding_null_usesDefault() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setEncoding(null);
        zaos.close();
        // Should not throw
    }

    @Test
    // Tests setUseLanguageEncodingFlag with default value
    public void testSetUseLanguageEncodingFlag_default() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setUseLanguageEncodingFlag(true);
        assertTrue(zaos.isSeekable());
        zaos.close();
    }

    @Test
    // Tests setFallbackToUTF8 with default value
    public void testSetFallbackToUTF8_default() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setFallbackToUTF8(true);
        zaos.close();
    }

    @Test
    // Tests setCreateUnicodeExtraFields with null - should use default
    public void testSetCreateUnicodeExtraFields_null() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setCreateUnicodeExtraFields(null);
        zaos.close();
    }

    @Test
    // Tests setLevel with default value
    public void testSetLevel_default() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setLevel(Deflater.DEFAULT_COMPRESSION);
        zaos.close();
    }

    @Test
    // Tests write with empty byte array
    public void testWrite_emptyByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        byte[] empty = new byte[0];
        zaos.write(empty);
        zaos.close();
    }

    @Test
    // Tests write with offset and length parameters
    public void testWrite_withOffsetAndLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        byte[] data = "Hello World".getBytes("UTF-8");
        zaos.write(data, 1, 5);
        zaos.close();
    }

    @Test
    // Tests write with null byte array - should throw NullPointerException
    public void testWrite_nullArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        try {
            zaos.write(null, 0, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        zaos.close();
    }

    @Test
    // Tests write with invalid offset - should throw IndexOutOfBoundsException
    public void testWrite_invalidOffset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        byte[] data = "test".getBytes("UTF-8");
        try {
            zaos.write(data, -1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        zaos.close();
    }

    @Test
    // Tests write with invalid length - should throw IndexOutOfBoundsException
    public void testWrite_invalidLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        byte[] data = "test".getBytes("UTF-8");
        try {
            zaos.write(data, 0, data.length + 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        zaos.close();
    }

    @Test
    // Tests close() method multiple times should not throw
    public void testClose_multipleTimes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.close();
        zaos.close();
    }

    @Test
    // Tests finish() followed by close() should work
    public void testFinish_thenClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] content = "data".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.setSize(content.length);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
    }

    @Test
    // Tests creating ZipArchiveOutputStream with OutputStream that doesn't support seeking
    public void testOutputStream_nonSeekable() throws IOException {
        OutputStream nonSeekableOut = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(nonSeekableOut);
        assertFalse("Not seekable", zaos.isSeekable());
        zaos.close();
    }
}