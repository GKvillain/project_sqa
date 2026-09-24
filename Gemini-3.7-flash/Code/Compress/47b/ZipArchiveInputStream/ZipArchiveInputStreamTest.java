package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZipArchiveInputStreamTest {

    // Tests matches() with standard LFH signature
    @Test
    public void testMatches_validLfhSignature_returnsTrue() {
        byte[] sig = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests matches() with empty central directory signature
    @Test
    public void testMatches_validEocdSignature_returnsTrue() {
        byte[] sig = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests matches() with split zip signature
    @Test
    public void testMatches_validDdSignature_returnsTrue() {
        byte[] sig = ZipArchiveOutputStream.DD_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests matches() with single segment split marker
    @Test
    public void testMatches_singleSegmentMarker_returnsTrue() {
        byte[] sig = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests matches() boundary when length is smaller than header size
    @Test
    public void testMatches_lengthLessThanFour_returnsFalse() {
        byte[] sig = ZipArchiveOutputStream.LFH_SIG;
        assertFalse(ZipArchiveInputStream.matches(sig, 3));
    }

    // Tests matches() with non-zip signature
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        byte[] sig = new byte[]{0x00, 0x01, 0x02, 0x03};
        assertFalse(ZipArchiveInputStream.matches(sig, 4));
    }

    // Tests canReadEntryData() with non-ZipArchiveEntry
    @Test
    public void testCanReadEntryData_nonZipArchiveEntry_returnsFalse() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ArchiveEntry otherEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public Date getLastModifiedDate() {
                return null;
            }
        };
        assertFalse(in.canReadEntryData(otherEntry));
        assertFalse(in.canReadEntryData(null));
        in.close();
    }

    // Tests canReadEntryData() with STORED entry without data descriptor
    @Test
    public void testCanReadEntryData_storedEntryWithoutDataDescriptor_returnsTrue() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(10);
        entry.setCrc(0);

        assertTrue(in.canReadEntryData(entry));
        in.close();
    }

    // Tests canReadEntryData() with STORED entry having data descriptor
    @Test
    public void testCanReadEntryData_storedWithDataDescriptor_respectsAllowFlag() throws IOException {
        ZipArchiveInputStream inDisallowed = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, false);
        ZipArchiveInputStream inAllowed = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored_dd.txt");
        entry.setMethod(ZipEntry.STORED);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpb);

        assertFalse(inDisallowed.canReadEntryData(entry));
        assertTrue(inAllowed.canReadEntryData(entry));

        inDisallowed.close();
        inAllowed.close();
    }

    // Tests canReadEntryData() with DEFLATED entry
    @Test
    public void testCanReadEntryData_deflatedEntry_returnsTrue() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("deflated.txt");
        entry.setMethod(ZipEntry.DEFLATED);

        assertTrue(in.canReadEntryData(entry));
        in.close();
    }

    // Tests canReadEntryData() with BZIP2 and unsupported compression methods
    @Test
    public void testCanReadEntryData_bzip2Entry_returnsTrue() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("bzip2.txt");
        entry.setMethod(ZipMethod.BZIP2.getCode());

        assertTrue(in.canReadEntryData(entry));
        in.close();
    }

    // Tests getNextZipEntry() on an empty input stream
    @Test
    public void testGetNextZipEntry_emptyStream_returnsNull() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(in.getNextZipEntry());
        in.close();
    }

    // Tests reading a normal DEFLATED archive entry
    @Test
    public void testRead_deflatedEntry_readsCorrectData() throws IOException {
        byte[] payload = "Hello Apache Commons Compress ZipArchiveInputStream!".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("test.txt", ZipEntry.DEFLATED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = in.getNextZipEntry();

        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());

        byte[] readBuf = new byte[payload.length];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(payload.length, bytesRead);
        assertEquals(new String(payload, "UTF-8"), new String(readBuf, "UTF-8"));
        assertEquals(-1, in.read());

        assertNull(in.getNextEntry());
        in.close();
    }

    // Tests reading a STORED archive entry
    @Test
    public void testRead_storedEntry_readsCorrectData() throws IOException {
        byte[] payload = "Stored data without compression".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("stored.txt", ZipEntry.STORED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = in.getNextZipEntry();

        assertNotNull(entry);
        assertEquals("stored.txt", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());

        byte[] readBuf = new byte[payload.length];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(payload.length, bytesRead);
        assertEquals(new String(payload, "UTF-8"), new String(readBuf, "UTF-8"));

        in.close();
    }

    // Tests skip() operation
    @Test
    public void testSkip_validBytes_skipsDataCorrectly() throws IOException {
        byte[] payload = "1234567890abcdefghij".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("skip.txt", ZipEntry.DEFLATED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(in.getNextZipEntry());

        long skipped = in.skip(10);
        assertEquals(10, skipped);

        byte[] remaining = new byte[10];
        int read = in.read(remaining, 0, remaining.length);
        assertEquals(10, read);
        assertEquals("abcdefghij", new String(remaining, "UTF-8"));

        in.close();
    }

    // Tests skip() with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.skip(-1);
        } finally {
            in.close();
        }
    }

    // Tests read() after close throws IOException
    @Test(expected = IOException.class)
    public void testRead_afterClose_throwsIOException() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.read(new byte[10], 0, 10);
    }

    // Tests read() with invalid bounds throws ArrayIndexOutOfBoundsException
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRead_invalidOffsetLength_throwsException() throws IOException {
        byte[] payload = "test data".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("bounds.txt", ZipEntry.DEFLATED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        in.getNextZipEntry();
        try {
            in.read(new byte[5], 0, 10);
        } finally {
            in.close();
        }
    }

    // Tests unexpected record signature throws ZipException
    @Test(expected = ZipException.class)
    public void testGetNextZipEntry_corruptedSignature_throwsZipException() throws IOException {
        byte[] corrupted = new byte[]{0x50, 0x4b, 0x09, 0x09, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(corrupted));
        try {
            in.getNextZipEntry();
        } finally {
            in.close();
        }
    }

    // Tests split archive signature throws UnsupportedZipFeatureException
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testGetNextZipEntry_splitArchiveSignature_throwsUnsupportedFeature() throws IOException {
        byte[] splitSig = new byte[]{0x50, 0x4b, 0x07, 0x08, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(splitSig));
        try {
            in.getNextZipEntry();
        } finally {
            in.close();
        }
    }

    // Tests canReadEntryData with encrypted entries
    @Test
    public void testCanReadEntryData_encryptedEntry_returnsFalse() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("encrypted.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useEncryption(true);
        entry.setGeneralPurposeBit(gpb);

        assertFalse(in.canReadEntryData(entry));
        in.close();
    }

    // Tests canReadEntryData with unsupported compression method
    @Test
    public void testCanReadEntryData_unsupportedMethod_returnsFalse() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("unsupported.txt");
        entry.setMethod(99); // Unsupported method code

        assertFalse(in.canReadEntryData(entry));
        in.close();
    }

    // Tests read(byte[], int, 0) returns 0
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        byte[] payload = "data".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("zero.txt", ZipEntry.DEFLATED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(in.getNextZipEntry());

        assertEquals(0, in.read(new byte[10], 0, 0));
        in.close();
    }

    // Tests skip past EOF and skip 0 bytes
    @Test
    public void testSkip_zeroAndBeyondEof() throws IOException {
        byte[] payload = "small".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("small.txt", ZipEntry.STORED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(in.getNextZipEntry());

        assertEquals(0, in.skip(0));
        assertEquals(5, in.skip(100));
        assertEquals(0, in.skip(10));
        assertEquals(-1, in.read());

        in.close();
    }

    // Tests multiple entries and skipping entry before reading to the end
    @Test
    public void testRead_multipleEntriesWithoutReadingToEnd() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry1 = new ZipArchiveEntry("file1.txt");
        zaos.putArchiveEntry(entry1);
        zaos.write("First file content here".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        ZipArchiveEntry entry2 = new ZipArchiveEntry("file2.txt");
        zaos.putArchiveEntry(entry2);
        zaos.write("Second file content here".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.close();

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));

        ZipArchiveEntry readEntry1 = in.getNextZipEntry();
        assertNotNull(readEntry1);
        assertEquals("file1.txt", readEntry1.getName());

        // Do not read readEntry1 to EOF, immediately advance to entry2
        ZipArchiveEntry readEntry2 = in.getNextZipEntry();
        assertNotNull(readEntry2);
        assertEquals("file2.txt", readEntry2.getName());

        byte[] buf = new byte[64];
        int read = in.read(buf);
        assertEquals("Second file content here", new String(buf, 0, read, "UTF-8"));

        assertNull(in.getNextZipEntry());
        in.close();
    }

    // Tests reading single bytes using read()
    @Test
    public void testRead_singleByte() throws IOException {
        byte[] payload = new byte[]{65, 66, 67};
        byte[] zipData = createSimpleZipArchive("bytes.txt", ZipEntry.STORED, payload);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(in.getNextZipEntry());

        assertEquals(65, in.read());
        assertEquals(66, in.read());
        assertEquals(67, in.read());
        assertEquals(-1, in.read());

        in.close();
    }

    // Tests close() called multiple times
    @Test
    public void testClose_multipleCalls() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.close(); // Should not throw exception
    }

    // Tests constructors with different parameters
    @Test
    public void testConstructors() throws IOException {
        ZipArchiveInputStream in1 = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8");
        assertNull(in1.getNextZipEntry());
        in1.close();

        ZipArchiveInputStream in2 = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", false);
        assertNull(in2.getNextZipEntry());
        in2.close();

        ZipArchiveInputStream in3 = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true, true);
        assertNull(in3.getNextZipEntry());
        in3.close();
    }

    // Tests reading truncated entry throws EOFException / IOException
    @Test(expected = IOException.class)
    public void testRead_truncatedData_throwsException() throws IOException {
        byte[] payload = "Some lengthy payload for truncation test".getBytes("UTF-8");
        byte[] zipData = createSimpleZipArchive("truncated.txt", ZipEntry.DEFLATED, payload);

        // Truncate the archive data in the middle
        byte[] truncated = new byte[zipData.length - 20];
        System.arraycopy(zipData, 0, truncated, 0, truncated.length);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(truncated));
        assertNotNull(in.getNextZipEntry());
        byte[] buf = new byte[100];
        while (in.read(buf) > 0) {
            // Read until exception
        }
        in.close();
    }

    // Tests reading unsupported feature entry throws UnsupportedZipFeatureException
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testRead_unsupportedMethod_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("unsupported_method.txt");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(4);
        entry.setCrc(0);
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3, 4});
        zaos.closeArchiveEntry();
        zaos.close();

        byte[] zipData = baos.toByteArray();
        // Overwrite compression method in local file header (offset 8-9) with unsupported method 99 (0x0063)
        zipData[8] = 99;
        zipData[9] = 0;

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry readEntry = in.getNextZipEntry();
        assertNotNull(readEntry);
        try {
            in.read(new byte[10]);
        } finally {
            in.close();
        }
    }

    // Tests entry with Unicode extra fields (UnicodePathExtraField)
    @Test
    public void testUnicodePathExtraField() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("ascii_name.txt");
        UnicodePathExtraField unicodeField = new UnicodePathExtraField("unicode_名.txt", entry.getName().getBytes("US-ASCII"));
        entry.addExtraField(unicodeField);

        zaos.putArchiveEntry(entry);
        zaos.write("hello".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", true);
        ZipArchiveEntry readEntry = in.getNextZipEntry();
        assertNotNull(readEntry);
        assertEquals("unicode_名.txt", readEntry.getName());
        in.close();
    }

    private byte[] createSimpleZipArchive(String entryName, int method, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        entry.setMethod(method);
        if (method == ZipEntry.STORED) {
            entry.setSize(content.length);
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setCrc(crc.getValue());
        }
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }
}