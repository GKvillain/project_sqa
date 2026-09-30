package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.Test;

public class ZipArchiveOutputStreamTest {

    // Helper: compute CRC for byte array
    private long computeCrc(final byte[] data) {
        final CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    // ----------------------------------------
    // Tests for defect detection (bug 43b)
    // ----------------------------------------

    // Tests normal write with seekable channel and Zip64Mode.Always (may trigger defect)
    @Test
    public void testWriteAndClose_seekableChannel_storedEntry_zip64Always_shouldBeReadable() throws IOException {
        final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(channel)) {
            zos.setUseZip64(Zip64Mode.Always);
            final ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            entry.setMethod(ZipArchiveOutputStream.STORED);
            final byte[] data = "Hello World".getBytes("UTF-8");
            entry.setSize(data.length);
            zos.putArchiveEntry(entry);
            zos.write(data, 0, data.length);
            zos.closeArchiveEntry();
            zos.finish();
            // read back
            final byte[] archiveBytes = channel.array();
            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(archiveBytes))) {
                final ZipArchiveEntry readEntry = (ZipArchiveEntry) zis.getNextEntry();
                assertNotNull("Entry not found", readEntry);
                assertEquals("test.txt", readEntry.getName());
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) >= 0) {
                    baos.write(buffer, 0, len);
                }
                assertArrayEquals(data, baos.toByteArray());
            }
        }
    }

    // Tests write with seekable channel, deflated entry, size unknown, Zip64Mode.AsNeeded (common defect trigger)
    @Test
    public void testWriteAndClose_seekableChannel_deflatedEntrySizeUnknown_zip64AsNeeded_shouldBeReadable() throws IOException {
        final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(channel)) {
            // zip64Mode defaults to AsNeeded
            final ZipArchiveEntry entry = new ZipArchiveEntry("def.txt");
            // method default (DEFLATED), size not set => SIZE_UNKNOWN
            final byte[] data = "Compress".getBytes("UTF-8");
            zos.putArchiveEntry(entry);
            zos.write(data, 0, data.length);
            zos.closeArchiveEntry();
            zos.finish();
            final byte[] archiveBytes = channel.array();
            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(archiveBytes))) {
                final ZipArchiveEntry readEntry = (ZipArchiveEntry) zis.getNextEntry();
                assertNotNull("Entry not found", readEntry);
                assertEquals("def.txt", readEntry.getName());
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) >= 0) {
                    baos.write(buffer, 0, len);
                }
                assertArrayEquals(data, baos.toByteArray());
            }
        }
    }

    // Tests normal write with non-seekable output stream (no defect involved)
    @Test
    public void testWriteAndClose_nonSeekableOutputStream_deflatedEntry_shouldBeReadable() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            final ZipArchiveEntry entry = new ZipArchiveEntry("out.txt");
            final byte[] data = "Data".getBytes("UTF-8");
            zos.putArchiveEntry(entry);
            zos.write(data, 0, data.length);
            zos.closeArchiveEntry();
            zos.finish();
            final byte[] archiveBytes = baos.toByteArray();
            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(archiveBytes))) {
                final ZipArchiveEntry readEntry = (ZipArchiveEntry) zis.getNextEntry();
                assertNotNull(readEntry);
                assertEquals("out.txt", readEntry.getName());
                final ByteArrayOutputStream readContent = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) >= 0) {
                    readContent.write(buffer, 0, len);
                }
                assertArrayEquals(data, readContent.toByteArray());
            }
        }
    }

    // ----------------------------------------
    // Boundary and exception tests
    // ----------------------------------------

    // Tests that a stored entry with size >4GB throws Zip64RequiredException when mode is Never
    @Test(expected = Zip64RequiredException.class)
    public void testPutArchiveEntry_storedEntrySizeExceeds4GB_zip64Never_throwsZip64RequiredException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.setUseZip64(Zip64Mode.Never);
            final ZipArchiveEntry entry = new ZipArchiveEntry("large.stored");
            entry.setMethod(ZipArchiveOutputStream.STORED);
            entry.setSize(4294967296L);
            entry.setCrc(0L); // required for STORED non-seekable
            zos.putArchiveEntry(entry);
        }
    }

    // Tests that putArchiveEntry after finish throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_afterFinish_throwsIOException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.finish();
            zos.putArchiveEntry(new ZipArchiveEntry("x.txt"));
        }
    }

    // Tests closeArchiveEntry without current entry throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_withoutEntry_throwsIOException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.closeArchiveEntry();
        }
    }

    // Tests write without current entry throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWrite_withoutEntry_throwsIllegalStateException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.write("test".getBytes(), 0, 4);
        }
    }

    // Tests invalid low compression level
    @Test(expected = IllegalArgumentException.class)
    public void testSetLevel_tooLow_throwsIllegalArgumentException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.setLevel(Deflater.DEFAULT_COMPRESSION - 1);
        }
    }

    // Tests invalid high compression level
    @Test(expected = IllegalArgumentException.class)
    public void testSetLevel_tooHigh_throwsIllegalArgumentException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.setLevel(Deflater.BEST_COMPRESSION + 1);
        }
    }

    // Tests finish with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsIOException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.putArchiveEntry(new ZipArchiveEntry("x.txt"));
            zos.finish();
        }
    }

    // Tests double finish throws IOException
    @Test(expected = IOException.class)
    public void testFinish_twice_throwsIOException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.finish();
            zos.finish();
        }
    }

    // Tests addRawArchiveEntry with seekable channel and STORED raw data
    @Test
    public void testAddRawArchiveEntry_basic_seekableChannel() throws IOException {
        final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(channel)) {
            final ZipArchiveEntry rawEntry = new ZipArchiveEntry("raw.txt");
            rawEntry.setMethod(ZipArchiveOutputStream.STORED);
            final byte[] rawData = "raw content".getBytes("UTF-8");
            rawEntry.setSize(rawData.length);
            rawEntry.setCompressedSize(rawData.length);
            rawEntry.setCrc(computeCrc(rawData));
            zos.addRawArchiveEntry(rawEntry, new ByteArrayInputStream(rawData));
            zos.finish();
            final byte[] archiveBytes = channel.array();
            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(archiveBytes))) {
                final ZipArchiveEntry verifyEntry = (ZipArchiveEntry) zis.getNextEntry();
                assertNotNull(verifyEntry);
                assertEquals("raw.txt", verifyEntry.getName());
                final byte[] verifyContent = new byte[rawData.length];
                final int read = zis.read(verifyContent);
                assertEquals(rawData.length, read);
                assertArrayEquals(rawData, verifyContent);
            }
        }
    }

    // Tests canWriteEntryData returns false for imploding method
    @Test
    public void testCanWriteEntryData_implodingMethod_returnsFalse() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            final ZipArchiveEntry entry = new ZipArchiveEntry("imploding.zip");
            entry.setMethod(ZipMethod.IMPLODING.getCode());
            assertFalse(zos.canWriteEntryData(entry));
        }
    }

    // Tests canWriteEntryData returns false for unshrinking method
    @Test
    public void testCanWriteEntryData_unsupportedMethod_returnsFalse() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            final ZipArchiveEntry entry = new ZipArchiveEntry("unsupp.zip");
            entry.setMethod(ZipMethod.UNSHRINKING.getCode());
            assertFalse(zos.canWriteEntryData(entry));
        }
    }

    // Tests createArchiveEntry after finish throws IOException
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_afterFinish_throwsIOException() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream())) {
            zos.finish();
            zos.createArchiveEntry(new java.io.File("test"), "file.txt");
        }
    }
}