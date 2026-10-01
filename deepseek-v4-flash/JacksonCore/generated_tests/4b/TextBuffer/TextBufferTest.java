package com.fasterxml.jackson.core.util;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.util.Arrays;

public class TextBufferTest {

    @Test
    public void testResetWithEmpty_initialState_returnsCorrect() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        assertEquals(0, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test
    public void testResetWithShared_validBuffer_returnsCorrect() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "hello".toCharArray();
        tb.resetWithShared(shared, 0, 5);
        assertEquals(5, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(shared, tb.getTextBuffer());
        assertEquals("hello", tb.contentsAsString());
    }

    @Test
    public void testResetWithCopy_validBuffer_returnsCorrect() {
        TextBuffer tb = new TextBuffer(null);
        char[] buf = "world".toCharArray();
        tb.resetWithCopy(buf, 0, 5);
        assertEquals(5, tb.size());
        assertEquals("world", tb.contentsAsString());
        assertNotSame(buf, tb.getTextBuffer());
        assertEquals(0, tb.getTextOffset());
    }

    @Test
    public void testResetWithString_initialState_returnsString() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("test");
        assertEquals(4, tb.size());
        assertEquals("test", tb.contentsAsString());
        assertFalse(tb.hasTextAsCharacters());
    }

    @Test
    public void testAppendChar_singleChar_appendsCorrectly() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.append('x');
        assertEquals(1, tb.size());
        assertEquals("x", tb.contentsAsString());
    }

    @Test
    public void testAppendCharArray_smallArray_appendsCorrectly() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.append("abc".toCharArray(), 0, 3);
        assertEquals(3, tb.size());
        assertEquals("abc", tb.contentsAsString());
    }

    @Test
    public void testAppendString_partialString_appendsCorrectly() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.append("hello world", 6, 5);
        assertEquals(5, tb.size());
        assertEquals("world", tb.contentsAsString());
    }

    @Test
    public void testAppendString_largeString_triggersExpand() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        StringBuilder sb = new StringBuilder(2000);
        for (int i = 0; i < 2000; i++) sb.append('a');
        String large = sb.toString();
        tb.append(large, 0, large.length());
        assertEquals(large, tb.contentsAsString());
    }

    @Test
    public void testAppendCharArray_largeArray_triggersExpand() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        char[] large = new char[1500];
        Arrays.fill(large, 'z');
        tb.append(large, 0, large.length());
        assertEquals(1500, tb.size());
        assertEquals(new String(large), tb.contentsAsString());
    }

    @Test
    public void testContentsAsString_multipleSegments_returnsFullString() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        char[] first = new char[500];
        Arrays.fill(first, 'a');
        tb.append(first, 0, 500);
        tb.finishCurrentSegment();
        char[] second = new char[500];
        Arrays.fill(second, 'b');
        tb.append(second, 0, 500);
        assertEquals(new String(first) + new String(second), tb.contentsAsString());
    }

    @Test
    public void testContentsAsArray_multipleSegments_returnsCombinedArray() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        char[] first = new char[600];
        Arrays.fill(first, 'x');
        tb.append(first, 0, 600);
        tb.finishCurrentSegment();
        char[] second = new char[600];
        Arrays.fill(second, 'y');
        tb.append(second, 0, 600);
        char[] result = tb.contentsAsArray();
        assertEquals(1200, result.length);
        assertEquals('x', result[0]);
        assertEquals('y', result[1199]);
    }

    @Test
    public void testContentsAsDecimal_sharedBuffer_returnsDecimal() {
        TextBuffer tb = new TextBuffer(null);
        char[] buf = "123.456".toCharArray();
        tb.resetWithShared(buf, 0, 7);
        assertEquals(new BigDecimal("123.456"), tb.contentsAsDecimal());
    }

    @Test
    public void testContentsAsDecimal_segmentedBuffer_returnsDecimal() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.append("123.456".toCharArray(), 0, 7);
        tb.finishCurrentSegment();
        tb.append("789".toCharArray(), 0, 3);
        assertEquals(new BigDecimal("123.456789"), tb.contentsAsDecimal());
    }

    @Test
    public void testContentsAsDecimal_resultArray_returnsDecimal() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("45.67");
        tb.contentsAsArray();
        assertEquals(new BigDecimal("45.67"), tb.contentsAsDecimal());
    }

    @Test
    public void testEnsureNotShared_sharedBuffer_copiesToPrivate() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "shared".toCharArray();
        tb.resetWithShared(shared, 0, 6);
        tb.ensureNotShared();
        assertEquals(0, tb.getTextOffset());
        assertEquals(6, tb.size());
        assertEquals("shared", tb.contentsAsString());
        assertNotSame(shared, tb.getTextBuffer());
    }

    @Test
    public void testGetCurrentSegment_sharedBuffer_unshares() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "data".toCharArray();
        tb.resetWithShared(shared, 0, 4);
        char[] seg = tb.getCurrentSegment();
        assertNotNull(seg);
        assertEquals(0, tb.getTextOffset());
        assertEquals(4, tb.getCurrentSegmentSize());
    }

    @Test
    public void testEmptyAndGetCurrentSegment_clearsAndReturnsSegment() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("hello", 0, 5);
        char[] seg = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test
    public void testFinishCurrentSegment_createsNewSegment() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.append("first", 0, 5);
        char[] newSeg = tb.finishCurrentSegment();
        assertNotNull(newSeg);
        assertEquals(0, tb.getCurrentSegmentSize());
        assertEquals("first", tb.contentsAsString());
    }

    @Test
    public void testExpandCurrentSegment_increasesSize() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        int origLen = seg.length;
        char[] expanded = tb.expandCurrentSegment();
        assertTrue(expanded.length > origLen);
        assertSame(expanded, tb.getCurrentSegment());
    }

    @Test
    public void testExpandCurrentSegment_withMinSize_ensuresMin() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        int minSize = seg.length + 100;
        char[] expanded = tb.expandCurrentSegment(minSize);
        assertTrue(expanded.length >= minSize);
    }

    @Test
    public void testReleaseBuffers_withoutAllocator_resets() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("something");
        tb.releaseBuffers();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test
    public void testReleaseBuffers_withAllocator_recyclesBuffer() {
        BufferRecycler br = new BufferRecycler();
        TextBuffer tb = new TextBuffer(br);
        tb.resetWithEmpty();
        tb.append("data", 0, 4);
        tb.releaseBuffers();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test
    public void testGetTextOffset_sharedWithOffset_returnsOffset() {
        TextBuffer tb = new TextBuffer(null);
        char[] buf = "  hello".toCharArray();
        tb.resetWithShared(buf, 2, 5);
        assertEquals(2, tb.getTextOffset());
        assertEquals("hello", tb.contentsAsString());
    }

    @Test
    public void testGetTextBuffer_resultString_convertsToArray() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("abc");
        char[] buf = tb.getTextBuffer();
        assertNotNull(buf);
        assertEquals(3, buf.length);
        assertEquals('a', buf[0]);
        assertSame(buf, tb.getTextBuffer());
    }

    @Test
    public void testHasTextAsCharacters_resultString_returnsFalse() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("abc");
        assertFalse(tb.hasTextAsCharacters());
    }

    // ====== New test cases for uncovered parts ======

    @Test
    public void testResetWithCopy_offsetGreaterThanZero() {
        TextBuffer tb = new TextBuffer(null);
        char[] buf = "hello-world".toCharArray();
        tb.resetWithCopy(buf, 6, 5); // copy "world" from offset 6
        assertEquals(5, tb.size());
        assertEquals("world", tb.contentsAsString());
        assertNotSame(buf, tb.getTextBuffer());
        assertEquals(0, tb.getTextOffset());
    }

    @Test
    public void testResetWithShared_lengthZero() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "abc".toCharArray();
        tb.resetWithShared(shared, 1, 0);
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        assertEquals(1, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(shared, tb.getTextBuffer());
    }

    @Test
    public void testResetWithString_empty() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("");
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        assertFalse(tb.hasTextAsCharacters());
    }

    @Test
    public void testHasTextAsCharacters_sharedBuffer() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithShared("abc".toCharArray(), 0, 3);
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test
    public void testHasTextAsCharacters_copyBuffer() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithCopy("hello".toCharArray(), 0, 5);
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test
    public void testGetTextBuffer_sharedBuffer_returnsSame() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "data".toCharArray();
        tb.resetWithShared(shared, 0, 4);
        assertSame(shared, tb.getTextBuffer());
    }

    @Test
    public void testAppendMultipleChars_sequential() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.append('a');
        tb.append('b');
        tb.append('c');
        assertEquals(3, tb.size());
        assertEquals("abc", tb.contentsAsString());
    }

    @Test
    public void testSetCurrentLength_basic() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        tb.getCurrentSegment(); // ensure segment exists
        tb.setCurrentLength(3);
        assertEquals(3, tb.getCurrentSegmentSize());
        assertEquals(3, tb.size());
    }

    @Test
    public void testSetCurrentLength_reducesSize() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("hello");
        tb.setCurrentLength(2);
        assertEquals(2, tb.size());
        assertEquals("he", tb.contentsAsString());
    }

    @Test
    public void testExpandCurrentSegment_afterSetCurrentLength() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();
        char[] seg = tb.getCurrentSegment();
        seg[0] = 'x';
        seg[1] = 'y';
        tb.setCurrentLength(2);
        tb.expandCurrentSegment(10);
        assertTrue(tb.getCurrentSegment().length >= 10);
        assertEquals("xy", tb.contentsAsString());
    }
}