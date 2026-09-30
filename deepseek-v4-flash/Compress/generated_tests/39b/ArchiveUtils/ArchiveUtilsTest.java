package org.apache.commons.compress.utils;

import static org.junit.Assert.*;

import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

public class ArchiveUtilsTest {

    // Test toString with directory entry
    @Test
    public void testToString_directoryEntry_returnsFormattedString() {
        ArchiveEntry entry = new ArchiveEntry() {
            @Override
            public String getName() { return "testdir"; }
            @Override
            public long getSize() { return 100; }
            @Override
            public boolean isDirectory() { return true; }
            @Override
            public Date getLastModifiedDate() { return new Date(); }
        };
        String result = ArchiveUtils.toString(entry);
        assertEquals("d     100 testdir", result);
    }

    // Test toString with file entry
    @Test
    public void testToString_fileEntry_returnsFormattedString() {
        ArchiveEntry entry = new ArchiveEntry() {
            @Override
            public String getName() { return "file.txt"; }
            @Override
            public long getSize() { return 2000; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public Date getLastModifiedDate() { return new Date(); }
        };
        String result = ArchiveUtils.toString(entry);
        assertEquals("-    2000 file.txt", result);
    }

    // Test toString with zero size entry
    @Test
    public void testToString_zeroSize_returnsFormattedString() {
        ArchiveEntry entry = new ArchiveEntry() {
            @Override
            public String getName() { return "empty"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public Date getLastModifiedDate() { return new Date(); }
        };
        String result = ArchiveUtils.toString(entry);
        assertEquals("-       0 empty", result);
    }

    // Test matchAsciiBuffer with matching content
    @Test
    public void testMatchAsciiBuffer_matchingContent_returnsTrue() {
        byte[] buffer = "Hello".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(ArchiveUtils.matchAsciiBuffer("Hello", buffer));
    }

    // Test matchAsciiBuffer with non-matching content
    @Test
    public void testMatchAsciiBuffer_nonMatchingContent_returnsFalse() {
        byte[] buffer = "World".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(ArchiveUtils.matchAsciiBuffer("Hello", buffer));
    }

    // Test matchAsciiBuffer with offset and length
    @Test
    public void testMatchAsciiBuffer_withOffsetAndLength_matchesCorrectly() {
        byte[] buffer = "prefixHello".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(ArchiveUtils.matchAsciiBuffer("Hello", buffer, 6, 5));
    }

    // Test matchAsciiBuffer with offset and length, non-matching
    @Test
    public void testMatchAsciiBuffer_withOffsetAndLength_nonMatching_returnsFalse() {
        byte[] buffer = "prefixWorld".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(ArchiveUtils.matchAsciiBuffer("Hello", buffer, 6, 5));
    }

    // Test toAsciiBytes conversion
    @Test
    public void testToAsciiBytes_validString_returnsAsciiBytes() {
        byte[] expected = new byte[] { 72, 101, 108, 108, 111 }; // "Hello"
        assertArrayEquals(expected, ArchiveUtils.toAsciiBytes("Hello"));
    }

    // Test toAsciiBytes with empty string
    @Test
    public void testToAsciiBytes_emptyString_returnsEmptyArray() {
        assertArrayEquals(new byte[0], ArchiveUtils.toAsciiBytes(""));
    }

    // Test toAsciiString with full byte array
    @Test
    public void testToAsciiString_validBytes_returnsString() {
        byte[] input = new byte[] { 72, 101, 108, 108, 111 };
        assertEquals("Hello", ArchiveUtils.toAsciiString(input));
    }

    // Test toAsciiString with offset and length
    @Test
    public void testToAsciiString_withOffsetAndLength_returnsSubstring() {
        byte[] input = new byte[] { 0, 0, 72, 105, 0 };
        assertEquals("Hi", ArchiveUtils.toAsciiString(input, 2, 2));
    }

    // Test isEqual with identical buffers
    @Test
    public void testIsEqual_identicalBuffers_returnsTrue() {
        byte[] buf1 = new byte[] { 1, 2, 3 };
        byte[] buf2 = new byte[] { 1, 2, 3 };
        assertTrue(ArchiveUtils.isEqual(buf1, buf2));
    }

    // Test isEqual with different buffers
    @Test
    public void testIsEqual_differentBuffers_returnsFalse() {
        byte[] buf1 = new byte[] { 1, 2, 3 };
        byte[] buf2 = new byte[] { 1, 2, 4 };
        assertFalse(ArchiveUtils.isEqual(buf1, buf2));
    }

    // Test isEqual with buffers of different lengths, without ignoring trailing nulls
    @Test
    public void testIsEqual_differentLengths_returnsFalse() {
        byte[] buf1 = new byte[] { 1, 2, 3 };
        byte[] buf2 = new byte[] { 1, 2, 3, 0 };
        assertFalse(ArchiveUtils.isEqual(buf1, buf2));
    }

    // Test isEqual with buffers of different lengths, ignoring trailing nulls
    @Test
    public void testIsEqual_differentLengthsIgnoreTrailingNulls_returnsTrue() {
        byte[] buf1 = new byte[] { 1, 2, 3 };
        byte[] buf2 = new byte[] { 1, 2, 3, 0, 0 };
        assertTrue(ArchiveUtils.isEqual(buf1, buf2, true));
    }

    // Test isEqual with buffers where longer has non-null trailing data
    @Test
    public void testIsEqual_differentLengthsIgnoreTrailingNullsNonZeroTail_returnsFalse() {
        byte[] buf1 = new byte[] { 1, 2, 3 };
        byte[] buf2 = new byte[] { 1, 2, 3, 4 };
        assertFalse(ArchiveUtils.isEqual(buf1, buf2, true));
    }

    // Test isEqualWithNull delegates to isEqual with ignoreTrailingNulls=true
    @Test
    public void testIsEqualWithNull_matchingWithTrailingNulls_returnsTrue() {
        byte[] buf1 = new byte[] { 1, 2, 3, 0, 0 };
        byte[] buf2 = new byte[] { 1, 2, 3 };
        assertTrue(ArchiveUtils.isEqualWithNull(buf1, 0, buf1.length, buf2, 0, buf2.length));
    }

    // Test isArrayZero with all zeros
    @Test
    public void testIsArrayZero_allZeros_returnsTrue() {
        byte[] arr = new byte[] { 0, 0, 0, 0 };
        assertTrue(ArchiveUtils.isArrayZero(arr, 4));
    }

    // Test isArrayZero with non-zero element
    @Test
    public void testIsArrayZero_hasNonZero_returnsFalse() {
        byte[] arr = new byte[] { 0, 0, 1, 0 };
        assertFalse(ArchiveUtils.isArrayZero(arr, 4));
    }

    // Test isArrayZero with zero size
    @Test
    public void testIsArrayZero_zeroSize_returnsTrue() {
        byte[] arr = new byte[] { 1, 2, 3 };
        assertTrue(ArchiveUtils.isArrayZero(arr, 0));
    }

    // Test sanitize with normal string
    @Test
    public void testSanitize_normalString_returnsSameString() {
        assertEquals("hello", ArchiveUtils.sanitize("hello"));
    }

    // Test sanitize with control characters
    @Test
    public void testSanitize_controlCharacters_replacesWithQuestionMark() {
        String input = "he\u0000llo\u0007";
        assertEquals("he?llo?", ArchiveUtils.sanitize(input));
    }

    // Test sanitize with non-BMP characters (supplementary)
    @Test
    public void testSanitize_supplementaryCharacters_keepsThem() {
        // Musical symbol G clef (U+1D11E) is not ISO control and not SPECIALS
        String input = "a\uD834\uDD1Eb";
        String result = ArchiveUtils.sanitize(input);
        assertEquals(input, result);
    }

    // Test sanitize with specials unicode block
    @Test
    public void testSanitize_specialsBlock_replacedWithQuestionMark() {
        // U+FFF9 is in SPECIALS block
        String input = "a\uFFF9b";
        assertEquals("a?b", ArchiveUtils.sanitize(input));
    }
}