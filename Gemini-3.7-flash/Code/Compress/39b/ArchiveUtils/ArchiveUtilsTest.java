package org.apache.commons.compress.utils;

import java.io.File;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArchiveUtilsTest {

    // Tests toString for a file entry
    @Test
    public void testToString_fileEntry_returnsFormattedString() {
        ArchiveEntry entry = new ArchiveEntry() {
            public String getName() { return "test.txt"; }
            public long getSize() { return 1024L; }
            public boolean isDirectory() { return false; }
            public Date getLastModifiedDate() { return null; }
        };
        String result = ArchiveUtils.toString(entry);
        assertEquals("-    1024 test.txt", result);
    }

    // Tests toString for a directory entry
    @Test
    public void testToString_directoryEntry_returnsFormattedString() {
        ArchiveEntry entry = new ArchiveEntry() {
            public String getName() { return "folder"; }
            public long getSize() { return 0L; }
            public boolean isDirectory() { return true; }
            public Date getLastModifiedDate() { return null; }
        };
        String result = ArchiveUtils.toString(entry);
        assertEquals("d       0 folder", result);
    }

    // Tests toAsciiBytes and toAsciiString conversion
    @Test
    public void testToAsciiBytesAndString_validAscii_matchesCorrectly() {
        String input = "Hello World!";
        byte[] bytes = ArchiveUtils.toAsciiBytes(input);
        assertNotNull(bytes);
        assertEquals(input, ArchiveUtils.toAsciiString(bytes));
    }

    // Tests toAsciiString with offset and length
    @Test
    public void testToAsciiString_withOffsetAndLength_returnsSubstring() {
        byte[] bytes = ArchiveUtils.toAsciiBytes("PrefixTestDataSuffix");
        String result = ArchiveUtils.toAsciiString(bytes, 6, 8);
        assertEquals("TestData", result);
    }

    // Tests matchAsciiBuffer with matching buffer
    @Test
    public void testMatchAsciiBuffer_matchingBuffer_returnsTrue() {
        byte[] buffer = "abcdef".getBytes();
        assertTrue(ArchiveUtils.matchAsciiBuffer("abcdef", buffer));
    }

    // Tests matchAsciiBuffer with offset and length
    @Test
    public void testMatchAsciiBuffer_withOffset_returnsTrue() {
        byte[] buffer = "xxHello Worldyy".getBytes();
        assertTrue(ArchiveUtils.matchAsciiBuffer("Hello World", buffer, 2, 11));
    }

    // Tests matchAsciiBuffer with mismatching content
    @Test
    public void testMatchAsciiBuffer_mismatch_returnsFalse() {
        byte[] buffer = "abcxyz".getBytes();
        assertFalse(ArchiveUtils.matchAsciiBuffer("abcdef", buffer));
    }

    // Tests isEqual with identical byte arrays
    @Test
    public void testIsEqual_identicalArrays_returnsTrue() {
        byte[] b1 = new byte[]{1, 2, 3};
        byte[] b2 = new byte[]{1, 2, 3};
        assertTrue(ArchiveUtils.isEqual(b1, b2));
    }

    // Tests isEqual with different byte arrays
    @Test
    public void testIsEqual_differentArrays_returnsFalse() {
        byte[] b1 = new byte[]{1, 2, 3};
        byte[] b2 = new byte[]{1, 2, 4};
        assertFalse(ArchiveUtils.isEqual(b1, b2));
    }

    // Tests isEqual with offset and length mismatch
    @Test
    public void testIsEqual_offsetAndLength_returnsCorrectResult() {
        byte[] b1 = new byte[]{0, 1, 2, 3, 0};
        byte[] b2 = new byte[]{9, 1, 2, 3, 9};
        assertTrue(ArchiveUtils.isEqual(b1, 1, 3, b2, 1, 3));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 3, b2, 0, 3));
    }

    // Tests isEqual ignoring trailing nulls when buffer1 is longer
    @Test
    public void testIsEqual_ignoreTrailingNulls_buffer1Longer() {
        byte[] b1 = new byte[]{1, 2, 3, 0, 0};
        byte[] b2 = new byte[]{1, 2, 3};
        assertTrue(ArchiveUtils.isEqual(b1, 0, 5, b2, 0, 3, true));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 5, b2, 0, 3, false));
    }

    // Tests isEqual ignoring trailing nulls when buffer2 is longer
    @Test
    public void testIsEqual_ignoreTrailingNulls_buffer2Longer() {
        byte[] b1 = new byte[]{1, 2, 3};
        byte[] b2 = new byte[]{1, 2, 3, 0, 0};
        assertTrue(ArchiveUtils.isEqual(b1, 0, 3, b2, 0, 5, true));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 3, b2, 0, 5, false));
    }

    // Tests isEqual ignoring trailing nulls with non-zero trailing byte
    @Test
    public void testIsEqual_ignoreTrailingNulls_nonZeroTrailing_returnsFalse() {
        byte[] b1 = new byte[]{1, 2, 3, 0, 1};
        byte[] b2 = new byte[]{1, 2, 3};
        assertFalse(ArchiveUtils.isEqual(b1, 0, 5, b2, 0, 3, true));
    }

    // Tests isEqualWithNull helper method
    @Test
    public void testIsEqualWithNull_trailingZeros_returnsTrue() {
        byte[] b1 = new byte[]{1, 2, 0, 0};
        byte[] b2 = new byte[]{1, 2};
        assertTrue(ArchiveUtils.isEqualWithNull(b1, 0, 4, b2, 0, 2));
    }

    // Tests isEqual with boolean ignoreTrailingNulls flag
    @Test
    public void testIsEqual_withBooleanFlag_returnsTrue() {
        byte[] b1 = new byte[]{65, 66, 0};
        byte[] b2 = new byte[]{65, 66};
        assertTrue(ArchiveUtils.isEqual(b1, b2, true));
        assertFalse(ArchiveUtils.isEqual(b1, b2, false));
    }

    // Tests isArrayZero on all-zero and non-zero arrays
    @Test
    public void testIsArrayZero_validZeroAndNonZero_returnsExpected() {
        byte[] allZero = new byte[]{0, 0, 0, 0};
        byte[] notAllZero = new byte[]{0, 0, 1, 0};
        assertTrue(ArchiveUtils.isArrayZero(allZero, 4));
        assertFalse(ArchiveUtils.isArrayZero(notAllZero, 4));
        assertTrue(ArchiveUtils.isArrayZero(notAllZero, 2));
    }

    // Tests sanitize with normal and control characters
    @Test
    public void testSanitize_controlChars_replacedWithQuestionMark() {
        String input = "Hello\nWorld\t\0!";
        String sanitized = ArchiveUtils.sanitize(input);
        assertEquals("Hello?World??!", sanitized);
    }

    // Tests sanitize with Unicode specials block character
    @Test
    public void testSanitize_specialsUnicodeBlock_replacedWithQuestionMark() {
        String input = "Special\uFFF0Char";
        String sanitized = ArchiveUtils.sanitize(input);
        assertEquals("Special?Char", sanitized);
    }

    // Tests sanitize does not exceed 255 characters
    @Test
    public void testSanitize_longString_outputNotLongerThan255Chars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append('a');
        }
        String sanitized = ArchiveUtils.sanitize(sb.toString());
        assertTrue(sanitized.length() <= 255);
    }
}