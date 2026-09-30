package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextBufferTest {

    private TextBuffer createBuffer() {
        return new TextBuffer(null);
    }

    // Tests resetWithEmpty initial state
    @Test
    public void testResetWithEmpty_initialState_returnsEmpty() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        assertTrue(tb.hasTextAsCharacters());
    }

    // Tests resetWithShared basic behavior
    @Test
    public void testResetWithShared_sharedBuffer_correctContent() {
        TextBuffer tb = createBuffer();
        char[] shared = "hello".toCharArray();
        tb.resetWithShared(shared, 0, 5);
        assertEquals(5, tb.size());
        assertEquals("hello", tb.contentsAsString());
        assertSame(shared, tb.getTextBuffer());
    }

    // Tests resetWithShared with non-zero offset
    @Test
    public void testResetWithShared_withOffset_correctContent() {
        TextBuffer tb = createBuffer();
        char[] shared = "abcdef".toCharArray();
        tb.resetWithShared(shared, 2, 3);
        assertEquals(3, tb.size());
        assertEquals("cde", tb.contentsAsString());
        assertEquals(2, tb.getTextOffset());
    }

    // Tests resetWithCopy copies data correctly
    @Test
    public void testResetWithCopy_copyBuffer_correctContent() {
        TextBuffer tb = createBuffer();
        char[] data = "world".toCharArray();
        tb.resetWithCopy(data, 0, 5);
        assertEquals(5, tb.size());
        assertEquals("world", tb.contentsAsString());
    }

    // Tests resetWithString stores string
    @Test
    public void testResetWithString_stringContent_correct() {
        TextBuffer tb = createBuffer();
        tb.resetWithString("test");
        assertEquals(4, tb.size());
        assertEquals("test", tb.contentsAsString());
        assertFalse(tb.hasTextAsCharacters());
    }

    // Tests appending single characters
    @Test
    public void testAppendChar_singleChar_correctContent() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.append('a');
        tb.append('b');
        assertEquals(2, tb.size());
        assertEquals("ab", tb.contentsAsString());
    }

    // Tests full segment and expansion
    @Test
    public void testAppendCharArray_fullSegment_expands() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] big = new char[1000];
        Arrays.fill(big, 'x');
        tb.append(big, 0, 1000);
        assertEquals(1000, tb.size());
        tb.append('y');
        assertEquals(1001, tb.size());
        String result = tb.contentsAsString();
        assertEquals(1001, result.length());
        assertEquals('x', result.charAt(999));
        assertEquals('y', result.charAt(1000));
    }

    // Tests appending string substring
    @Test
    public void testAppendString_substring_correct() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.append("hello world", 6, 5);
        assertEquals("world", tb.contentsAsString());
    }

    // Tests contentsAsString with multiple segments
    @Test
    public void testContentsAsString_manySegments_correct() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        int total = 5000;
        char[] chunk = new char[2000];
        Arrays.fill(chunk, 'a');
        tb.append(chunk, 0, 2000);
        tb.append(chunk, 0, 2000);
        tb.append(chunk, 0, 1000);
        assertEquals(5000, tb.size());
        String result = tb.contentsAsString();
        assertEquals(5000, result.length());
        for (int i = 0; i < 5000; i++) {
            assertEquals('a', result.charAt(i));
        }
    }

    // Tests getTextBuffer returns shared buffer
    @Test
    public void testGetTextBuffer_shared_returnsShared() {
        TextBuffer tb = createBuffer();
        char[] shared = "shared".toCharArray();
        tb.resetWithShared(shared, 0, 6);
        assertSame(shared, tb.getTextBuffer());
    }

    // Tests ensureNotShared copies to private buffer
    @Test
    public void testEnsureNotShared_sharedBuffer_copiesToPrivate() {
        TextBuffer tb = createBuffer();
        char[] shared = "abc".toCharArray();
        tb.resetWithShared(shared, 0, 3);
        tb.ensureNotShared();
        assertEquals(0, tb.getTextOffset());
        assertNotSame(shared, tb.getTextBuffer());
        assertEquals(3, tb.size());
        assertEquals("abc", tb.contentsAsString());
    }

    // Tests finishCurrentSegment creation
    @Test
    public void testFinishCurrentSegment_segmentsCreated_correctSize() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] seg1 = tb.getCurrentSegment();
        seg1[0] = 'a';
        tb.setCurrentLength(1);
        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = 'b';
        tb.setCurrentLength(1);
        assertEquals(2, tb.size());
        assertEquals("ab", tb.contentsAsString());
    }

    // Tests expandCurrentSegment increases length
    @Test
    public void testExpandCurrentSegment_increasesLength() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        int originalLen = seg.length;
        seg[0] = 'x';
        tb.setCurrentLength(1);
        tb.expandCurrentSegment();
        char[] newSeg = tb.getCurrentSegment();
        assertTrue(newSeg.length > originalLen);
        assertEquals('x', newSeg[0]);
    }

    // Tests expandCurrentSegment with minSize
    @Test
    public void testExpandCurrentSegment_minSize_ensuresMinSize() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        int originalLen = seg.length;
        int minSize = originalLen + 500;
        tb.expandCurrentSegment(minSize);
        char[] newSeg = tb.getCurrentSegment();
        assertTrue(newSeg.length >= minSize);
    }

    // Tests setCurrentAndReturn with single segment
    @Test
    public void testSetCurrentAndReturn_singleSegment_returnsString() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        "hello".getChars(0, 5, seg, 0);
        String result = tb.setCurrentAndReturn(5);
        assertEquals("hello", result);
        assertEquals("hello", tb.contentsAsString());
    }

    // Tests setCurrentAndReturn with multiple segments
    @Test
    public void testSetCurrentAndReturn_multipleSegments_returnsString() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] seg1 = tb.getCurrentSegment();
        seg1[0] = 'a';
        tb.setCurrentLength(1);
        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = 'b';
        tb.setCurrentLength(1);
        String result = tb.setCurrentAndReturn(2);
        assertEquals("ab", result);
    }

    // Tests contentsAsDecimal with shared buffer
    @Test
    public void testContentsAsDecimal_sharedBuffer_returnsBigDecimal() {
        TextBuffer tb = createBuffer();
        char[] data = "123.456".toCharArray();
        tb.resetWithShared(data, 0, 7);
        BigDecimal dec = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("123.456"), dec);
    }

    // Tests contentsAsDecimal with single segment
    @Test
    public void testContentsAsDecimal_singleSegment_returnsBigDecimal() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.append("789.01", 0, 6);
        BigDecimal dec = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("789.01"), dec);
    }

    // Tests contentsAsDouble
    @Test
    public void testContentsAsDouble_string_returnsDouble() {
        TextBuffer tb = createBuffer();
        tb.resetWithString("3.14");
        double d = tb.contentsAsDouble();
        assertEquals(3.14, d, 1e-9);
    }

    // Tests append when using shared buffer triggers unshare
    @Test
    public void testAppend_whenSharedBuffer_unsharesCorrectly() {
        TextBuffer tb = createBuffer();
        char[] shared = "abc".toCharArray();
        tb.resetWithShared(shared, 0, 3);
        tb.append('d');
        assertEquals(4, tb.size());
        assertEquals("abcd", tb.contentsAsString());
        assertTrue(tb.getTextOffset() == 0);
    }

    // ====== Additional tests for uncovered areas ======

    @Test
    public void testContentsAsArray_sharedBuffer_returnsCharArray() {
        TextBuffer tb = createBuffer();
        char[] data = "hello".toCharArray();
        tb.resetWithShared(data, 0, 5);
        char[] result = tb.contentsAsArray();
        assertArrayEquals(new char[]{'h','e','l','l','o'}, result);
    }

    @Test
    public void testContentsAsArray_privateBuffer_returnsCharArray() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.append("world");
        char[] result = tb.contentsAsArray();
        assertArrayEquals(new char[]{'w','o','r','l','d'}, result);
    }

    @Test
    public void testContentsAsArray_multipleSegments_returnsCharArray() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] chunk = new char[2000];
        Arrays.fill(chunk, 'a');
        tb.append(chunk, 0, 2000);
        tb.append(chunk, 0, 2000);
        tb.append(chunk, 0, 1000);
        char[] result = tb.contentsAsArray();
        assertEquals(5000, result.length);
        for (int i = 0; i < 5000; i++) {
            assertEquals('a', result[i]);
        }
    }

    @Test
    public void testCharAt_validIndex_returnsChar() {
        TextBuffer tb = createBuffer();
        tb.resetWithString("hello");
        assertEquals('h', tb.charAt(0));
        assertEquals('e', tb.charAt(1));
        assertEquals('l', tb.charAt(2));
        assertEquals('l', tb.charAt(3));
        assertEquals('o', tb.charAt(4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAt_invalidIndex_throwsException() {
        TextBuffer tb = createBuffer();
        tb.resetWithString("ab");
        tb.charAt(3);
    }

    @Test
    public void testReleaseBuffers_afterUse_releases() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.append("test");
        tb.releaseBuffers();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test
    public void testResetWithCopy_withOffset_correctContent() {
        TextBuffer tb = createBuffer();
        char[] data = "abcdef".toCharArray();
        tb.resetWithCopy(data, 2, 3);
        assertEquals(3, tb.size());
        assertEquals("cde", tb.contentsAsString());
    }

    @Test
    public void testAppendCharArray_withOffset_correctContent() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] data = "abcdef".toCharArray();
        tb.append(data, 2, 3);
        assertEquals(3, tb.size());
        assertEquals("cde", tb.contentsAsString());
    }

    @Test
    public void testExpandCurrentSegment_minSizeLessThanCurrent_noChange() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        int originalLen = seg.length;
        seg[0] = 'x';
        tb.setCurrentLength(1);
        tb.expandCurrentSegment(originalLen - 1);
        char[] newSeg = tb.getCurrentSegment();
        assertSame(seg, newSeg);
        assertEquals(originalLen, newSeg.length);
    }

    @Test
    public void testSetCurrentAndReturn_zeroLength_returnsEmptyString() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        String result = tb.setCurrentAndReturn(0);
        assertEquals("", result);
    }

    @Test(expected = NumberFormatException.class)
    public void testContentsAsDecimal_emptyBuffer_throwsNumberFormatException() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.contentsAsDecimal();
    }

    @Test(expected = NumberFormatException.class)
    public void testContentsAsDouble_emptyBuffer_throwsNumberFormatException() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.contentsAsDouble();
    }

    @Test
    public void testGetTextOffset_private_returnsZero() {
        TextBuffer tb = createBuffer();
        tb.resetWithEmpty();
        tb.append("hello");
        assertEquals(0, tb.getTextOffset());
    }

    @Test
    public void testHasTextAsCharacters_shared_returnsTrue() {
        TextBuffer tb = createBuffer();
        char[] data = "test".toCharArray();
        tb.resetWithShared(data, 0, 4);
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test
    public void testHasTextAsCharacters_copy_returnsTrue() {
        TextBuffer tb = createBuffer();
        char[] data = "test".toCharArray();
        tb.resetWithCopy(data, 0, 4);
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test
    public void testToString_equivalentToContentsAsString() {
        TextBuffer tb = createBuffer();
        tb.resetWithString("hello");
        assertEquals(tb.contentsAsString(), tb.toString());
    }
}