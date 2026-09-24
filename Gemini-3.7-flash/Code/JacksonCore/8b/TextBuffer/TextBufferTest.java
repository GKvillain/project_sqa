package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextBufferTest {

    private BufferRecycler _recycler;
    private TextBuffer _tb;

    @Before
    public void setUp() {
        _recycler = new BufferRecycler();
        _tb = new TextBuffer(_recycler);
    }

    // Tests getTextBuffer on freshly initialized TextBuffer (Defects4J JacksonCore-8 defect)
    @Test
    public void testGetTextBuffer_freshInstance_returnsNonNull() {
        TextBuffer tb = new TextBuffer(null);
        char[] buf = tb.getTextBuffer();
        assertNotNull(buf);
        assertEquals(0, buf.length);
    }

    // Tests getTextBuffer on resetWithEmpty TextBuffer
    @Test
    public void testGetTextBuffer_afterResetWithEmpty_returnsNonNull() {
        _tb.resetWithEmpty();
        char[] buf = _tb.getTextBuffer();
        assertNotNull(buf);
        assertEquals(0, _tb.size());
    }

    // Tests resetWithShared and getTextBuffer / contentsAsString
    @Test
    public void testResetWithShared_validInput_returnsSharedContent() {
        char[] src = "Hello World".toCharArray();
        _tb.resetWithShared(src, 6, 5);

        assertEquals(5, _tb.size());
        assertEquals(6, _tb.getTextOffset());
        assertTrue(_tb.hasTextAsCharacters());
        assertSame(src, _tb.getTextBuffer());
        assertEquals("World", _tb.contentsAsString());
        assertArrayEquals("World".toCharArray(), _tb.contentsAsArray());
    }

    // Tests resetWithCopy
    @Test
    public void testResetWithCopy_validInput_copiesContent() {
        char[] src = "Test Copy Buffer".toCharArray();
        _tb.resetWithCopy(src, 5, 4);

        assertEquals(4, _tb.size());
        assertEquals(0, _tb.getTextOffset());
        assertEquals("Copy", _tb.contentsAsString());
        assertArrayEquals("Copy".toCharArray(), _tb.contentsAsArray());
    }

    // Tests resetWithString
    @Test
    public void testResetWithString_validInput_setsStringContent() {
        _tb.resetWithString("SampleText");

        assertEquals(10, _tb.size());
        assertFalse(_tb.hasTextAsCharacters());
        assertEquals("SampleText", _tb.contentsAsString());
        assertTrue(_tb.hasTextAsCharacters());
        assertArrayEquals("SampleText".toCharArray(), _tb.getTextBuffer());
    }

    // Tests appending single characters and string conversion
    @Test
    public void testAppend_singleChars_appendsCorrectly() {
        _tb.append('a');
        _tb.append('b');
        _tb.append('c');

        assertEquals(3, _tb.size());
        assertEquals("abc", _tb.contentsAsString());
        assertEquals("abc", _tb.toString());
    }

    // Tests appending char array
    @Test
    public void testAppend_charArray_appendsCorrectly() {
        _tb.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        assertEquals(2, _tb.size());
        assertEquals("bc", _tb.contentsAsString());
    }

    // Tests appending String with offset and length
    @Test
    public void testAppend_stringSegment_appendsCorrectly() {
        _tb.append("abcdef", 2, 3);
        assertEquals(3, _tb.size());
        assertEquals("cde", _tb.contentsAsString());
    }

    // Tests appending after shared buffer triggers unsharing
    @Test
    public void testAppend_whenShared_unsharesProperly() {
        char[] src = "shared-data".toCharArray();
        _tb.resetWithShared(src, 0, 6);
        _tb.append('!');

        assertEquals(7, _tb.size());
        assertEquals("shared!", _tb.contentsAsString());
    }

    // Tests ensureNotShared on shared buffer
    @Test
    public void testEnsureNotShared_whenShared_unsharesCorrectly() {
        char[] src = "sharedContent".toCharArray();
        _tb.resetWithShared(src, 0, 6);
        _tb.ensureNotShared();

        assertEquals(6, _tb.size());
        assertEquals("shared", _tb.contentsAsString());
    }

    // Tests buffer expansion across segments with large content
    @Test
    public void testAppend_largeContent_expandsAcrossSegments() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append("0123456789");
        }
        String large = sb.toString();
        _tb.append(large, 0, large.length());

        assertEquals(large.length(), _tb.size());
        assertEquals(large, _tb.contentsAsString());
        assertArrayEquals(large.toCharArray(), _tb.contentsAsArray());
    }

    // Tests getCurrentSegment and emptyAndGetCurrentSegment
    @Test
    public void testEmptyAndGetCurrentSegment_returnsValidBuffer() {
        char[] segment = _tb.emptyAndGetCurrentSegment();
        assertNotNull(segment);
        assertTrue(segment.length > 0);
        assertEquals(0, _tb.getCurrentSegmentSize());

        segment[0] = 'X';
        segment[1] = 'Y';
        _tb.setCurrentLength(2);

        assertEquals(2, _tb.getCurrentSegmentSize());
        assertEquals("XY", _tb.contentsAsString());
    }

    // Tests setCurrentAndReturn method
    @Test
    public void testSetCurrentAndReturn_singleSegment_returnsString() {
        char[] seg = _tb.emptyAndGetCurrentSegment();
        seg[0] = 'A';
        seg[1] = 'B';
        String result = _tb.setCurrentAndReturn(2);

        assertEquals("AB", result);
        assertEquals(2, _tb.size());
    }

    // Tests finishCurrentSegment and expanding current segment
    @Test
    public void testFinishAndExpandCurrentSegment_growsBuffer() {
        char[] first = _tb.emptyAndGetCurrentSegment();
        _tb.setCurrentLength(first.length);
        char[] second = _tb.finishCurrentSegment();

        assertNotNull(second);
        assertTrue(second.length >= first.length);

        char[] expanded = _tb.expandCurrentSegment();
        assertTrue(expanded.length > second.length);
    }

    // Tests contentsAsDecimal and contentsAsDouble
    @Test
    public void testContentsAsDecimalAndDouble_validInputs_parsedCorrectly() {
        _tb.resetWithString("123.45");

        BigDecimal dec = _tb.contentsAsDecimal();
        assertEquals(new BigDecimal("123.45"), dec);

        double val = _tb.contentsAsDouble();
        assertEquals(123.45, val, 0.00001);
    }

    // Tests contentsAsDecimal with shared buffer
    @Test
    public void testContentsAsDecimal_sharedBuffer_parsedCorrectly() {
        char[] src = "xx12345yy".toCharArray();
        _tb.resetWithShared(src, 2, 5);

        BigDecimal dec = _tb.contentsAsDecimal();
        assertEquals(new BigDecimal("12345"), dec);
    }

    // Tests releaseBuffers clears content and recycles buffer
    @Test
    public void testReleaseBuffers_recyclesCorrectly() {
        _tb.emptyAndGetCurrentSegment();
        _tb.append("data", 0, 4);
        _tb.releaseBuffers();

        assertEquals(0, _tb.size());
    }

    // Tests releaseBuffers when allocator is null
    @Test
    public void testReleaseBuffers_nullAllocator_resetsState() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("temp", 0, 4);
        tb.releaseBuffers();

        assertEquals(0, tb.size());
    }

    // Tests contentsAsDecimal when invalid number format throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testContentsAsDecimal_invalidFormat_throwsException() {
        _tb.resetWithString("not_a_number");
        _tb.contentsAsDecimal();
    }

    // Tests contentsAsDouble when invalid format throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testContentsAsDouble_invalidFormat_throwsException() {
        _tb.resetWithString("not_a_number");
        _tb.contentsAsDouble();
    }

    // Tests getCurrentSegment when buffer is shared
    @Test
    public void testGetCurrentSegment_whenShared_unsharesAndReturnsBuffer() {
        char[] src = "hello".toCharArray();
        _tb.resetWithShared(src, 0, 5);
        char[] seg = _tb.getCurrentSegment();

        assertNotNull(seg);
        assertTrue(seg.length >= 5);
        assertEquals(5, _tb.getCurrentSegmentSize());
        assertEquals("hello", _tb.contentsAsString());
    }

    // Tests getTextBuffer and contentsAsArray when segmented
    @Test
    public void testGetTextBuffer_whenSegmented_returnsAggregatedArray() {
        char[] seg = _tb.emptyAndGetCurrentSegment();
        for (int i = 0; i < seg.length; i++) {
            seg[i] = 'a';
        }
        _tb.setCurrentLength(seg.length);
        _tb.finishCurrentSegment();

        _tb.append("tail", 0, 4);
        char[] fullText = _tb.getTextBuffer();
        assertEquals(seg.length + 4, fullText.length);
        assertEquals(0, _tb.getTextOffset());
    }

    // Tests appending empty char array and empty string
    @Test
    public void testAppend_zeroLength_doesNotChangeSize() {
        _tb.append(new char[]{'a', 'b'}, 0, 0);
        _tb.append("test", 0, 0);
        assertEquals(0, _tb.size());
    }

    // Tests ensureNotShared when not shared does nothing
    @Test
    public void testEnsureNotShared_whenNotShared_maintainsBuffer() {
        _tb.append("direct", 0, 6);
        _tb.ensureNotShared();
        assertEquals(6, _tb.size());
        assertEquals("direct", _tb.contentsAsString());
    }

    // Tests setCurrentAndReturn with multiple segments
    @Test
    public void testSetCurrentAndReturn_withMultipleSegments_returnsFullString() {
        char[] seg = _tb.emptyAndGetCurrentSegment();
        seg[0] = 'H';
        seg[1] = 'i';
        _tb.setCurrentLength(2);
        _tb.finishCurrentSegment();

        char[] seg2 = _tb.getCurrentSegment();
        seg2[0] = '!';
        String result = _tb.setCurrentAndReturn(1);

        assertEquals("Hi!", result);
        assertEquals(3, _tb.size());
    }
}