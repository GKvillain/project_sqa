package com.fasterxml.jackson.core.util;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TextBufferTest {

    private BufferRecycler recycler;
    private TextBuffer tb;

    @Before
    public void setUp() {
        recycler = new BufferRecycler();
        tb = new TextBuffer(recycler);
    }

    // Tests initial state and empty reset
    @Test
    public void testResetWithEmpty_initialState_returnsEmpty() {
        TextBuffer buffer = new TextBuffer(null);
        buffer.resetWithEmpty();
        assertEquals(0, buffer.size());
        assertEquals(0, buffer.getTextOffset());
        assertEquals("", buffer.contentsAsString());
        assertArrayEquals(TextBuffer.NO_CHARS, buffer.contentsAsArray());
        assertTrue(buffer.hasTextAsCharacters());
    }

    // Tests reset with shared buffer and offset
    @Test
    public void testResetWithShared_validInput_retainsSharedData() {
        char[] testChars = "Hello World".toCharArray();
        tb.resetWithShared(testChars, 6, 5);

        assertEquals(5, tb.size());
        assertEquals(6, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(testChars, tb.getTextBuffer());
        assertEquals("World", tb.contentsAsString());
        assertArrayEquals("World".toCharArray(), tb.contentsAsArray());
    }

    // Tests reset with shared buffer when length is zero
    @Test
    public void testResetWithShared_zeroLength_returnsEmptyString() {
        char[] testChars = "Hello".toCharArray();
        tb.resetWithShared(testChars, 0, 0);

        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());
    }

    // Tests reset with copy
    @Test
    public void testResetWithCopy_validInput_copiesContent() {
        char[] testChars = "Sample Copy".toCharArray();
        tb.resetWithCopy(testChars, 0, testChars.length);

        assertEquals(testChars.length, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("Sample Copy", tb.contentsAsString());
        assertArrayEquals(testChars, tb.contentsAsArray());
    }

    // Tests reset with string
    @Test
    public void testResetWithString_validInput_returnsStringAndConvertsToArray() {
        String testStr = "Reset String Value";
        tb.resetWithString(testStr);

        assertEquals(testStr.length(), tb.size());
        assertFalse(tb.hasTextAsCharacters());
        assertEquals(testStr, tb.contentsAsString());
        assertArrayEquals(testStr.toCharArray(), tb.getTextBuffer());
        assertTrue(tb.hasTextAsCharacters());
    }

    // Tests unsharing when appending single char to shared buffer
    @Test
    public void testAppendChar_toSharedBuffer_unsharesAndAppends() {
        char[] testChars = "abc".toCharArray();
        tb.resetWithShared(testChars, 0, 3);
        tb.append('d');

        assertEquals(4, tb.size());
        assertEquals("abcd", tb.contentsAsString());
    }

    // Tests appending char array exceeding single segment capacity
    @Test
    public void testAppendCharArray_exceedingSegment_expandsCorrectly() {
        char[] chunk = new char[1500];
        Arrays.fill(chunk, 'x');

        tb.append(chunk, 0, 1000);
        tb.append(chunk, 1000, 500);

        assertEquals(1500, tb.size());
        assertEquals(new String(chunk), tb.contentsAsString());
        assertEquals(new String(chunk), tb.toString());
    }

    // Tests appending string with offset and length across multiple segments
    @Test
    public void testAppendString_largeString_expandsSegments() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String large = sb.toString();

        tb.append(large, 0, 2500);
        tb.append(large, 2500, 2500);

        assertEquals(5000, tb.size());
        assertEquals(large, tb.contentsAsString());
        assertArrayEquals(large.toCharArray(), tb.contentsAsArray());
    }

    // Tests ensureNotShared on shared buffer
    @Test
    public void testEnsureNotShared_onSharedBuffer_copiesToPrivateBuffer() {
        char[] testChars = "Shared Data".toCharArray();
        tb.resetWithShared(testChars, 0, testChars.length);

        tb.ensureNotShared();
        assertEquals(0, tb.getTextOffset());
        assertEquals("Shared Data", tb.contentsAsString());
        assertNotSame(testChars, tb.getTextBuffer());
    }

    // Tests emptyAndGetCurrentSegment and manual population
    @Test
    public void testEmptyAndGetCurrentSegment_manualWrite_returnsCorrectContent() {
        char[] seg = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertTrue(seg.length >= TextBuffer.MIN_SEGMENT_LEN);

        seg[0] = 'a';
        seg[1] = 'b';
        seg[2] = 'c';
        tb.setCurrentLength(3);

        assertEquals(3, tb.getCurrentSegmentSize());
        assertEquals("abc", tb.contentsAsString());
    }

    // Tests finishCurrentSegment multiple times to build segments
    @Test
    public void testFinishCurrentSegment_multipleSegments_combinesProperly() {
        char[] seg1 = tb.emptyAndGetCurrentSegment();
        seg1[0] = '1';
        tb.setCurrentLength(1);

        char[] seg2 = tb.finishCurrentSegment();
        seg2[0] = '2';
        tb.setCurrentLength(1);

        assertEquals("12", tb.contentsAsString());
        assertArrayEquals(new char[]{'1', '2'}, tb.contentsAsArray());
    }

    // Tests decimal and double conversions
    @Test
    public void testContentsAsDecimalAndDouble_validNumericValues_convertsCorrectly() {
        String numStr = "12345.678";
        tb.resetWithString(numStr);

        assertEquals(new BigDecimal(numStr), tb.contentsAsDecimal());
        assertEquals(12345.678, tb.contentsAsDouble(), 0.00001);

        char[] shared = "987.65".toCharArray();
        tb.resetWithShared(shared, 0, shared.length);
        assertEquals(new BigDecimal("987.65"), tb.contentsAsDecimal());

        tb.resetWithCopy("42.0".toCharArray(), 0, 4);
        assertEquals(new BigDecimal("42.0"), tb.contentsAsDecimal());
    }

    // Tests exception on invalid decimal conversion
    @Test(expected = NumberFormatException.class)
    public void testContentsAsDecimal_invalidNumericString_throwsException() {
        tb.resetWithString("invalid-number");
        tb.contentsAsDecimal();
    }

    // Tests releaseBuffers with allocator and without allocator
    @Test
    public void testReleaseBuffers_withAllocator_clearsAndRecycles() {
        tb.getCurrentSegment();
        tb.releaseBuffers();
        assertEquals(0, tb.size());

        TextBuffer unmanaged = new TextBuffer(null);
        unmanaged.getCurrentSegment();
        unmanaged.releaseBuffers();
        assertEquals(0, unmanaged.size());
    }

    // Tests expandCurrentSegment(int minSize)
    @Test
    public void testExpandCurrentSegment_withRequestedMinSize_expandsProperly() {
        tb.getCurrentSegment();
        char[] expanded = tb.expandCurrentSegment(2500);
        assertTrue(expanded.length >= 2500);
        assertSame(expanded, tb.getCurrentSegment());

        char[] unchanged = tb.expandCurrentSegment(1000);
        assertSame(expanded, unchanged);
    }

    // Tests repeated expandCurrentSegment() beyond MAX_SEGMENT_LEN boundary (Defects4J Bug 4b)
    @Test
    public void testExpandCurrentSegment_beyondMaxSegmentLength_expandsMonotonically() {
        tb.getCurrentSegment();
        int prevLen = 0;
        // Expand continuously until reaching and exceeding MAX_SEGMENT_LEN (256k = 262144)
        for (int i = 0; i < 30; i++) {
            char[] seg = tb.expandCurrentSegment();
            int currLen = seg.length;
            assertTrue("Segment length must grow on expand: " + currLen + " vs " + prevLen, currLen > prevLen);
            prevLen = currLen;
            if (currLen > TextBuffer.MAX_SEGMENT_LEN + 100) {
                break;
            }
        }
        assertTrue("Must have expanded beyond MAX_SEGMENT_LEN", prevLen > TextBuffer.MAX_SEGMENT_LEN);
    }
}