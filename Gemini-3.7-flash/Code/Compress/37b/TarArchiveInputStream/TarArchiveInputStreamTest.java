package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TarArchiveInputStreamTest {

    // Tests constructors with different parameters and verifies default record size
    @Test
    public void testConstructors_variousParameters_initializesCorrectly() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais1 = new TarArchiveInputStream(in);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais1.getRecordSize());
        assertFalse(tais1.markSupported());
        assertFalse(tais1.isAtEOF());
        assertNull(tais1.getCurrentEntry());
        tais1.close();

        final TarArchiveInputStream tais2 = new TarArchiveInputStream(in, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais2.getRecordSize());
        tais2.close();

        final TarArchiveInputStream tais3 = new TarArchiveInputStream(in, 1024);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais3.getRecordSize());
        tais3.close();

        final TarArchiveInputStream tais4 = new TarArchiveInputStream(in, 1024, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais4.getRecordSize());
        tais4.close();

        final TarArchiveInputStream tais5 = new TarArchiveInputStream(in, 1024, 512);
        assertEquals(512, tais5.getRecordSize());
        tais5.close();

        final TarArchiveInputStream tais6 = new TarArchiveInputStream(in, 1024, 512, "UTF-8");
        assertEquals(512, tais6.getRecordSize());
        tais6.close();
    }

    // Tests parsePaxHeaders with standard valid PAX key-value lines
    @Test
    public void testParsePaxHeaders_validEntries_returnsParsedMap() throws IOException {
        final String paxHeader = "30 path=etc/passwd\n27 uid=1000\n28 gid=1000\n";
        final ByteArrayInputStream in = new ByteArrayInputStream(paxHeader.getBytes(CharsetNames.UTF_8));
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);

        final Map<String, String> headers = tais.parsePaxHeaders(in);
        assertEquals(3, headers.size());
        assertEquals("etc/passwd", headers.get("path"));
        assertEquals("1000", headers.get("uid"));
        assertEquals("1000", headers.get("gid"));
        tais.close();
    }

    // Tests parsePaxHeaders with blank lines and trailing newlines (regression test for Compress-37)
    @Test
    public void testParsePaxHeaders_blankLinesAndTrailingNewline_handlesGracefully() throws IOException {
        final String paxHeader = "30 path=etc/passwd\n\n\n";
        final ByteArrayInputStream in = new ByteArrayInputStream(paxHeader.getBytes(CharsetNames.UTF_8));
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);

        final Map<String, String> headers = tais.parsePaxHeaders(in);
        assertEquals(1, headers.size());
        assertEquals("etc/passwd", headers.get("path"));
        tais.close();
    }

    // Tests parsePaxHeaders with empty stream
    @Test
    public void testParsePaxHeaders_emptyInputStream_returnsEmptyMap() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);

        final Map<String, String> headers = tais.parsePaxHeaders(in);
        assertTrue(headers.isEmpty());
        tais.close();
    }

    // Tests parsePaxHeaders deleting a keyword when rest length is 1 (only newline)
    @Test
    public void testParsePaxHeaders_keyWithEmptyValue_removesKey() throws IOException {
        final String paxHeader = "11 path=\n";
        final ByteArrayInputStream in = new ByteArrayInputStream(paxHeader.getBytes(CharsetNames.UTF_8));
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);

        final Map<String, String> headers = tais.parsePaxHeaders(in);
        assertFalse(headers.containsKey("path"));
        tais.close();
    }

    // Tests parsePaxHeaders with truncated entry throws IOException
    @Test(expected = IOException.class)
    public void testParsePaxHeaders_truncatedContent_throwsIOException() throws IOException {
        final String paxHeader = "30 path=etc/pass";
        final ByteArrayInputStream in = new ByteArrayInputStream(paxHeader.getBytes(CharsetNames.UTF_8));
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        try {
            tais.parsePaxHeaders(in);
        } finally {
            tais.close();
        }
    }

    // Tests matches method with POSIX magic and version
    @Test
    public void testMatches_posixSignature_returnsTrue() {
        final byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests matches method with GNU magic and space/zero versions
    @Test
    public void testMatches_gnuSignature_returnsTrue() {
        final byte[] headerSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, headerSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, headerSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(headerSpace, headerSpace.length));

        final byte[] headerZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, headerZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, headerZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(headerZero, headerZero.length));
    }

    // Tests matches method with Ant magic and version
    @Test
    public void testMatches_antSignature_returnsTrue() {
        final byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests matches method with invalid or too short signature
    @Test
    public void testMatches_invalidOrShortSignature_returnsFalse() {
        final byte[] shortHeader = new byte[10];
        assertFalse(TarArchiveInputStream.matches(shortHeader, shortHeader.length));

        final byte[] invalidHeader = new byte[512];
        assertFalse(TarArchiveInputStream.matches(invalidHeader, invalidHeader.length));
    }

    // Tests getNextTarEntry on an empty stream returns null
    @Test
    public void testGetNextTarEntry_emptyStream_returnsNull() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertNull(tais.getNextTarEntry());
        assertNull(tais.getNextEntry());
        assertTrue(tais.isAtEOF());
        tais.close();
    }

    // Tests read when no entry has been opened throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testRead_noCurrentEntry_throwsIllegalStateException() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[512]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        try {
            tais.read(new byte[10], 0, 10);
        } finally {
            tais.close();
        }
    }

    // Tests skip and available methods without an active entry
    @Test
    public void testSkipAndAvailable_noCurrentEntry_returnsZero() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertEquals(0, tais.skip(100));
        assertEquals(0, tais.skip(-10));
        assertEquals(0, tais.available());
        tais.close();
    }

    // Tests mark and reset no-op behavior
    @Test
    public void testMarkAndReset_noOpSupported() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertFalse(tais.markSupported());
        tais.mark(10);
        tais.reset();
        tais.close();
    }

    // Tests canReadEntryData with TarArchiveEntry and non-TarArchiveEntry
    @Test
    public void testCanReadEntryData_variousEntryTypes_returnsExpected() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        final TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        assertTrue(tais.canReadEntryData(entry));
        assertFalse(tais.canReadEntryData(null));
        tais.close();
    }

    // Tests setAtEOF and setCurrentEntry helper methods
    @Test
    public void testSetAtEOFAndCurrentEntry_updatesState() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        final TarArchiveEntry entry = new TarArchiveEntry("file.txt");

        tais.setCurrentEntry(entry);
        assertEquals(entry, tais.getCurrentEntry());

        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());
        assertEquals(-1, tais.read(new byte[10], 0, 10));
        tais.close();
    }

    // Tests isEOFRecord on null and zero byte array
    @Test
    public void testIsEOFRecord_nullOrZeroArray_returnsTrue() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertTrue(tais.isEOFRecord(null));
        assertTrue(tais.isEOFRecord(new byte[512]));

        final byte[] nonZero = new byte[512];
        nonZero[0] = 1;
        assertFalse(tais.isEOFRecord(nonZero));
        tais.close();
    }

    // Tests reading single and multiple entries with content, verifying available, skip, read, and EOF transitions
    @Test
    public void testReadEntries_multipleFiles_readsContentCorrectly() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final byte[] content1 = "Hello World".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        entry1.setSize(content1.length);
        tos.putArchiveEntry(entry1);
        tos.write(content1);
        tos.closeArchiveEntry();

        final byte[] content2 = "Second File Content with extra bytes to test reading".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry2 = new TarArchiveEntry("dir/file2.txt");
        entry2.setSize(content2.length);
        tos.putArchiveEntry(entry2);
        tos.write(content2);
        tos.closeArchiveEntry();

        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));

        final TarArchiveEntry readEntry1 = tais.getNextTarEntry();
        assertNotNull(readEntry1);
        assertEquals("file1.txt", readEntry1.getName());
        assertEquals(content1.length, readEntry1.getSize());
        assertTrue(tais.available() > 0);

        final byte[] readBuf1 = new byte[content1.length];
        final int bytesRead1 = tais.read(readBuf1, 0, readBuf1.length);
        assertEquals(content1.length, bytesRead1);
        assertArrayEquals(content1, readBuf1);
        assertEquals(-1, tais.read(new byte[5]));
        assertEquals(0, tais.available());

        final TarArchiveEntry readEntry2 = (TarArchiveEntry) tais.getNextEntry();
        assertNotNull(readEntry2);
        assertEquals("dir/file2.txt", readEntry2.getName());

        final long skipped = tais.skip(7);
        assertEquals(7, skipped);

        final byte[] remaining = new byte[content2.length - 7];
        int totalRead = 0;
        int r;
        while ((r = tais.read(remaining, totalRead, remaining.length - totalRead)) != -1 && totalRead < remaining.length) {
            totalRead += r;
        }
        assertEquals(remaining.length, totalRead);

        final byte[] expectedRemaining = new byte[content2.length - 7];
        System.arraycopy(content2, 7, expectedRemaining, 0, expectedRemaining.length);
        assertArrayEquals(expectedRemaining, remaining);

        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
        tais.close();
    }

    // Tests skipping entry content automatically when calling getNextTarEntry before finishing read
    @Test
    public void testGetNextTarEntry_unreadEntry_skipsRemainingData() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final byte[] content1 = new byte[1024];
        for (int i = 0; i < content1.length; i++) {
            content1[i] = (byte) (i % 128);
        }
        final TarArchiveEntry entry1 = new TarArchiveEntry("file1.bin");
        entry1.setSize(content1.length);
        tos.putArchiveEntry(entry1);
        tos.write(content1);
        tos.closeArchiveEntry();

        final byte[] content2 = "Short content".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        entry2.setSize(content2.length);
        tos.putArchiveEntry(entry2);
        tos.write(content2);
        tos.closeArchiveEntry();

        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));

        final TarArchiveEntry readEntry1 = tais.getNextTarEntry();
        assertNotNull(readEntry1);
        assertEquals("file1.bin", readEntry1.getName());

        final byte[] partial = new byte[10];
        assertEquals(10, tais.read(partial));

        final TarArchiveEntry readEntry2 = tais.getNextTarEntry();
        assertNotNull(readEntry2);
        assertEquals("file2.txt", readEntry2.getName());

        final byte[] buf2 = new byte[content2.length];
        assertEquals(content2.length, tais.read(buf2));
        assertArrayEquals(content2, buf2);

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests GNU long name entries handled properly by getNextTarEntry
    @Test
    public void testGetNextTarEntry_gnuLongFileName_readsCorrectLongName() throws IOException {
        final String longFileName = "very/long/path/name/that/exceeds/one/hundred/characters/limit/in/standard/tar/format/which/requires/gnu/longlink/extension/test_long_file_name.txt";
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        final byte[] data = "Data in long file".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry = new TarArchiveEntry(longFileName);
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longFileName, readEntry.getName());
        assertEquals(data.length, readEntry.getSize());

        final byte[] readData = new byte[data.length];
        assertEquals(data.length, tais.read(readData));
        assertArrayEquals(data, readData);

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests GNU long link entries handled properly by getNextTarEntry
    @Test
    public void testGetNextTarEntry_gnuLongLinkName_readsCorrectLongLinkName() throws IOException {
        final String longLinkName = "target/of/symlink/that/is/very/long/and/exceeds/the/hundred/characters/boundary/limit/in/tar/header/test_link_target.txt";
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        final TarArchiveEntry entry = new TarArchiveEntry("symlink_entry", TarConstants.LF_SYMLINK);
        entry.setLinkName(longLinkName);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("symlink_entry", readEntry.getName());
        assertEquals(longLinkName, readEntry.getLinkName());
        assertTrue(readEntry.isSymbolicLink());

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests PAX header entries handling and properties override
    @Test
    public void testGetNextTarEntry_paxHeader_appliesPaxProperties() throws IOException {
        final String longFileName = "pax/extended/header/path/that/is/longer/than/one/hundred/characters/to/force/pax/header/generation/test_pax_entry.txt";
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        final byte[] data = "Pax Content".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry = new TarArchiveEntry(longFileName);
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longFileName, readEntry.getName());
        assertEquals(data.length, readEntry.getSize());

        final byte[] readData = new byte[data.length];
        assertEquals(data.length, tais.read(readData));
        assertArrayEquals(data, readData);

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests global PAX headers applied to subsequent entries
    @Test
    public void testGetNextTarEntry_globalPaxHeaders_appliesToSubsequentEntries() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final TarArchiveEntry globalPax = new TarArchiveEntry("globalPax", TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);
        final byte[] globalPaxData = "22 uname=customUser\n".getBytes(CharsetNames.UTF_8);
        globalPax.setSize(globalPaxData.length);

        final TarArchiveEntry fileEntry = new TarArchiveEntry("file.txt");
        final byte[] fileData = "test".getBytes(CharsetNames.UTF_8);
        fileEntry.setSize(fileData.length);

        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.putArchiveEntry(globalPax);
        tos.write(globalPaxData);
        tos.closeArchiveEntry();

        tos.putArchiveEntry(fileEntry);
        tos.write(fileData);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("file.txt", readEntry.getName());
        assertEquals("customUser", readEntry.getUserName());

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests canReadEntryData with anonymous non-TarArchiveEntry
    @Test
    public void testCanReadEntryData_nonTarArchiveEntry_returnsFalse() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);

        final ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "dummy";
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
            public java.util.Date getLastModifiedDate() {
                return new java.util.Date();
            }
        };

        assertFalse(tais.canReadEntryData(nonTarEntry));
        tais.close();
    }

    // Tests parsePaxHeaders with multi-byte UTF-8 character and negative / non-digit length headers
    @Test
    public void testParsePaxHeaders_multiByteUtf8AndSpecialValues() throws IOException {
        final String paxHeader = "29 path=föö/bår/test.txt\n";
        final ByteArrayInputStream in = new ByteArrayInputStream(paxHeader.getBytes(CharsetNames.UTF_8));
        final TarArchiveInputStream tais = new TarArchiveInputStream(in);

        final Map<String, String> headers = tais.parsePaxHeaders(in);
        assertEquals(1, headers.size());
        assertEquals("föö/bår/test.txt", headers.get("path"));
        tais.close();
    }

    // Tests skip method when skipping more bytes than the entry size
    @Test
    public void testSkip_moreThanEntrySize_skipsOnlyUpToEntrySize() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final byte[] content = "12345".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNotNull(tais.getNextTarEntry());
        final long skipped = tais.skip(100);
        assertEquals(5, skipped);
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read());
        tais.close();
    }
}