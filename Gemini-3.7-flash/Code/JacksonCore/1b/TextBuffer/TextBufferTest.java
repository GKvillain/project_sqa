package com.fasterxml.jackson.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;

public class TextBufferTest {

    // Tests contentsAsDecimal after resetting with String
    @Test
    public void testContentsAsDecimal_resetWithString_returnsCorrectBigDecimal() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("123.45");
        assertEquals(new BigDecimal("123.45"), tb.contentsAsDecimal());
    }

    // Tests contentsAsDecimal with segmented buffer
    @Test
    public void testContentsAsDecimal_segmentedBuffer_returnsCorrectBigDecimal() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.emptyAndGetCurrentSegment();
        seg[0] = '1';
        seg[1] = '0';
        tb.setCurrentLength(2);
        tb.finishCurrentSegment();

        char[] next = tb.getCurrentSegment();
        next[0] = '.';
        next[1] = '5';
        tb.setCurrentLength(2);

        assertEquals(new BigDecimal("10.5"), tb.contentsAsDecimal());
    }

    // Tests resetWithString accessor methods
    @Test
    public void testResetWithString_stringAccessors_returnsExpectedValues() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("Hello");
        assertEquals(5, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertFalse(tb.hasTextAsCharacters());
        assertEquals("Hello", tb.contentsAsString());
        assertEquals("Hello", tb.toString());
        assertArrayEquals("Hello".toCharArray(), tb.getTextBuffer());
        assertTrue(tb.hasTextAsCharacters());
    }

    // Tests resetWithShared normal behavior and accessors
    @Test
    public void testResetWithShared_validSubArray_returnsSharedView() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "prefix-target-suffix".toCharArray();
        tb.resetWithShared(source, 7, 6);

        assertEquals(6, tb.size());
        assertEquals(7, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(source, tb.getTextBuffer());
        assertEquals("target", tb.contentsAsString());
        assertArrayEquals("target".toCharArray(), tb.contentsAsArray());
        assertEquals(new BigDecimal("123.45"),
                new TextBuffer(null) {{
                    resetWithShared("123.45".toCharArray(), 0, 6);
                }}.contentsAsDecimal());
    }

    // Tests unsharing on append when initialized as shared
    @Test
    public void testAppend_afterResetWithShared_unsharesAndModifies() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "abc".toCharArray();
        tb.resetWithShared(source, 0, 3);
        tb.append('d');

        assertEquals(4, tb.size());
        assertEquals("abcd", tb.contentsAsString());
        assertEquals(0, tb.getTextOffset());
    }

    // Tests ensureNotShared unshares internal buffer
    @Test
    public void testEnsureNotShared_sharedBuffer_copiesToPrivateSegment() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "unshare".toCharArray();
        tb.resetWithShared(source, 0, source.length);
        tb.ensureNotShared();

        assertEquals(0, tb.getTextOffset());
        assertEquals("unshare", tb.contentsAsString());
    }

    // Tests resetWithCopy copies data into internal segment
    @Test
    public void testResetWithCopy_validArray_copiesContent() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "copy-content".toCharArray();
        tb.resetWithCopy(source, 0, source.length);

        assertEquals(source.length, tb.size());
        assertEquals("copy-content", tb.contentsAsString());
    }

    // Tests multi-segment expansion using char append
    @Test
    public void testAppendChar_manyCharacters_expandsAcrossSegments() {
        TextBuffer tb = new TextBuffer(null);
        for (int i = 0; i < 2500; i++) {
            tb.append((char) ('a' + (i % 26)));
        }
        assertEquals(2500, tb.size());
        assertEquals(2500, tb.contentsAsString().length());
        assertEquals(2500, tb.contentsAsArray().length);
    }

    // Tests multi-segment append with char arrays and Strings
    @Test
    public void testAppendArrayAndString_largeChunks_combinesCorrectly() {
        TextBuffer tb = new TextBuffer(null);
        char[] chunk = new char[800];
        Arrays.fill(chunk, 'x');

        tb.append(chunk, 0, 800);
        tb.append(chunk, 0, 800);
        tb.append("tail", 0, 4);

        assertEquals(1604, tb.size());
        String result = tb.contentsAsString();
        assertEquals(1604, result.length());
        assertTrue(result.endsWith("tail"));
    }

    // Tests double parsing
    @Test
    public void testContentsAsDouble_validDoubleString_returnsDouble() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("123.456", 0, 7);
        assertEquals(123.456, tb.contentsAsDouble(), 0.00001);
    }

    // Tests empty buffer handling
    @Test
    public void testResetWithEmpty_emptyBuffer_returnsEmptyResults() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("test", 0, 4);
        tb.resetWithEmpty();

        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(new char[0], tb.contentsAsArray());
    }

    // Tests segment manipulation helpers
    @Test
    public void testSegmentHelpers_growAndExpandSegments_worksCorrectly() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertTrue(seg.length > 0);

        tb.setCurrentLength(10);
        assertEquals(10, tb.getCurrentSegmentSize());

        char[] expanded = tb.expandCurrentSegment();
        assertTrue(expanded.length > seg.length);

        char[] next = tb.finishCurrentSegment();
        assertNotNull(next);
        assertNotSame(expanded, next);
    }

    // Tests releaseBuffers lifecycle with BufferRecycler
    @Test
    public void testReleaseBuffers_withBufferRecycler_recyclesAndClears() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);
        tb.getCurrentSegment();
        tb.setCurrentLength(50);
        tb.releaseBuffers();

        assertEquals(0, tb.size());

        // Test release without allocator
        TextBuffer tbNoAlloc = new TextBuffer(null);
        tbNoAlloc.getCurrentSegment();
        tbNoAlloc.releaseBuffers();
        assertEquals(0, tbNoAlloc.size());
    }

    // Tests contentsAsInt with positive, negative, shared, and segmented states
    @Test
    public void testContentsAsInt_allBufferStates_parsesCorrectly() {
        // From shared buffer
        TextBuffer tb = new TextBuffer(null);
        char[] src = "12345".toCharArray();
        tb.resetWithShared(src, 0, 5);
        assertEquals(12345, tb.contentsAsInt(false));
        assertEquals(-12345, tb.contentsAsInt(true));

        // From resetWithString
        tb.resetWithString("9876");
        assertEquals(9876, tb.contentsAsInt(false));
        assertEquals(-9876, tb.contentsAsInt(true));

        // From segmented buffer
        tb.resetWithEmpty();
        char[] seg = tb.emptyAndGetCurrentSegment();
        seg[0] = '4';
        seg[1] = '2';
        tb.setCurrentLength(2);
        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = '0';
        tb.setCurrentLength(1);

        assertEquals(420, tb.contentsAsInt(false));
        assertEquals(-420, tb.contentsAsInt(true));
    }

    // Tests contentsAsLong with positive, negative, shared, and segmented states
    @Test
    public void testContentsAsLong_allBufferStates_parsesCorrectly() {
        // From shared buffer
        TextBuffer tb = new TextBuffer(null);
        char[] src = "1234567890123".toCharArray();
        tb.resetWithShared(src, 0, 13);
        assertEquals(1234567890123L, tb.contentsAsLong(false));
        assertEquals(-1234567890123L, tb.contentsAsLong(true));

        // From resetWithString
        tb.resetWithString("9876543210");
        assertEquals(9876543210L, tb.contentsAsLong(false));
        assertEquals(-9876543210L, tb.contentsAsLong(true));

        // From segmented buffer
        tb.resetWithEmpty();
        char[] seg = tb.emptyAndGetCurrentSegment();
        seg[0] = '9';
        seg[1] = '9';
        tb.setCurrentLength(2);
        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = '9';
        seg2[1] = '9';
        tb.setCurrentLength(2);

        assertEquals(9999L, tb.contentsAsLong(false));
        assertEquals(-9999L, tb.contentsAsLong(true));
    }

    // Tests expandCurrentSegment(int minSize)
    @Test
    public void testExpandCurrentSegment_withMinSize_allocatesSufficientCapacity() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();
        char[] expanded = tb.expandCurrentSegment(2500);
        assertTrue(expanded.length >= 2500);
    }

    // Tests contentsAsDouble with segmented and shared buffer
    @Test
    public void testContentsAsDouble_segmentedAndShared_parsesDoubleCorrectly() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "3.14159".toCharArray();
        tb.resetWithShared(src, 0, src.length);
        assertEquals(3.14159, tb.contentsAsDouble(), 0.000001);

        tb.resetWithEmpty();
        char[] seg = tb.emptyAndGetCurrentSegment();
        seg[0] = '2';
        seg[1] = '.';
        tb.setCurrentLength(2);
        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = '7';
        seg2[1] = '1';
        tb.setCurrentLength(2);

        assertEquals(2.71, tb.contentsAsDouble(), 0.0001);
    }

    // Tests ensureNotShared when buffer is already not shared
    @Test
    public void testEnsureNotShared_whenAlreadyNotShared_noOp() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();
        tb.setCurrentLength(0);
        tb.append("test", 0, 4);

        int offsetBefore = tb.getTextOffset();
        tb.ensureNotShared();
        assertEquals(offsetBefore, tb.getTextOffset());
        assertEquals("test", tb.contentsAsString());
    }

    // Tests resetWithCopy when copying from another buffer segment
    @Test
    public void testResetWithCopy_withOffsetAndLength_copiesOnlySpecifiedWindow() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "prefixDATAappendix".toCharArray();
        tb.resetWithCopy(src, 6, 4);

        assertEquals(4, tb.size());
        assertEquals("DATA", tb.contentsAsString());
        assertTrue(tb.hasTextAsCharacters());
        assertEquals(0, tb.getTextOffset());
    }
}