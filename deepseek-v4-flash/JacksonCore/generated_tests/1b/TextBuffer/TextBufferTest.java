package com.fasterxml.jackson.core.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class TextBufferTest {

    // ---------- Helper to create TextBuffer without allocator ----------
    private TextBuffer createBuffer() {
        return new TextBuffer(null);
    }

    // ---------- Basic construction and release ----------
    @Test
    public void testConstructorAndReleaseBuffers_noAllocator_doesNotThrow() {
        TextBuffer buf = createBuffer();
        // Should not throw
        buf.releaseBuffers();
        assertNotNull(buf);
    }

    // ---------- Reset with empty then append ----------
    // Detects defect: resetWithEmpty does not initialize _currentSegment,
    // causing NullPointerException on append.
    @Test(expected = NullPointerException.class)
    public void testResetWithEmptyThenAppend_throwsNPE() {
        TextBuffer buf = createBuffer();
        buf.resetWithEmpty();
        buf.append('a');   // Expected to throw NPE due to null _currentSegment
    }

    // ---------- Reset with string then append ----------
    // Similarly, resetWithString leaves _currentSegment null.
    @Test(expected = NullPointerException.class)
    public void testResetWithStringThenAppend_throwsNPE() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("hello");
        buf.append('a');   // Expected NPE
    }

    // ---------- Normal append char ----------
    @Test
    public void testAppendChar_singleChar_returnsCorrectString() {
        TextBuffer buf = createBuffer();
        // Use emptyAndGetCurrentSegment to initialize properly
        buf.emptyAndGetCurrentSegment();
        buf.append('X');
        assertEquals("X", buf.contentsAsString());
        assertEquals(1, buf.size());
    }

    // ---------- Normal append char array ----------
    @Test
    public void testAppendCharArray_validInput_returnsCorrectString() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        buf.append(new char[] {'J', 'a', 'c', 'k', 's', 'o', 'n'}, 0, 7);
        assertEquals("Jackson", buf.contentsAsString());
        assertEquals(7, buf.size());
    }

    // ---------- Normal append String ----------
    @Test
    public void testAppendString_validInput_returnsCorrectString() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        buf.append("Core", 0, 4);
        assertEquals("Core", buf.contentsAsString());
        assertEquals(4, buf.size());
    }

    // ---------- Reset with shared buffer ----------
    @Test
    public void testResetWithShared_sharedBuffer_sizeAndTextOffset() {
        TextBuffer buf = createBuffer();
        char[] shared = "shared".toCharArray();
        buf.resetWithShared(shared, 0, 6);
        assertEquals(6, buf.size());
        assertEquals(0, buf.getTextOffset());
        assertTrue(buf.hasTextAsCharacters());
        assertSame(shared, buf.getTextBuffer());
    }

    // ---------- Reset with copy ----------
    @Test
    public void testResetWithCopy_copiesInput() {
        TextBuffer buf = createBuffer();
        char[] source = "copy".toCharArray();
        buf.resetWithCopy(source, 0, 4);
        assertEquals(4, buf.size());
        assertNotSame(source, buf.getTextBuffer()); // must be a copy
        assertEquals("copy", buf.contentsAsString());
    }

    // ---------- Reset with string ----------
    @Test
    public void testResetWithString_nonEmpty_sizeAndContent() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("text");
        assertEquals(4, buf.size());
        assertEquals("text", buf.contentsAsString());
        // getTextBuffer should return char array (cached)
        char[] arr = buf.getTextBuffer();
        assertEquals(4, arr.length);
    }

    // ---------- Size paths ----------
    @Test
    public void testSize_sharedBuffer_returnsInputLen() {
        TextBuffer buf = createBuffer();
        char[] shared = "hello".toCharArray();
        buf.resetWithShared(shared, 0, 5);
        assertEquals(5, buf.size());
    }

    @Test
    public void testSize_afterContentsAsArray_returnsArrayLength() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("array");
        buf.contentsAsArray();               // sets _resultArray
        assertEquals(5, buf.size());
    }

    @Test
    public void testSize_afterResetWithString_returnsStringLength() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("str");
        assertEquals(3, buf.size());
    }

    @Test
    public void testSize_afterSegments_returnsCombinedLength() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        // Append data to force multiple segments
        for (int i = 0; i < 2000; i++) {
            buf.append('x');
        }
        int expected = 2000;
        assertEquals(expected, buf.size());
    }

    // ---------- GetTextBuffer paths ----------
    @Test
    public void testGetTextBuffer_sharedBuffer_returnsInputBuffer() {
        TextBuffer buf = createBuffer();
        char[] shared = "shared".toCharArray();
        buf.resetWithShared(shared, 0, 6);
        assertSame(shared, buf.getTextBuffer());
    }

    @Test
    public void testGetTextBuffer_resultArrayCached_returnsResultArray() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("cached");
        buf.contentsAsArray();             // triggers caching
        // after caching, getTextBuffer returns _resultArray
        char[] arr = buf.getTextBuffer();
        assertNotNull(arr);
        assertEquals(6, arr.length);
    }

    @Test
    public void testGetTextBuffer_noSegments_returnsCurrentSegment() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        buf.append("seg", 0, 3);
        // No segments, only current
        assertNotNull(buf.getTextBuffer());
        assertEquals(3, buf.getTextBuffer().length);
    }

    // ---------- EnsureNotShared ----------
    @Test
    public void testEnsureNotShared_sharedBuffer_copiesToPrivate() {
        TextBuffer buf = createBuffer();
        char[] shared = "unshare".toCharArray();
        buf.resetWithShared(shared, 0, 7);
        buf.ensureNotShared();
        // Now should not be using shared buffer
        assertFalse(buf.hasTextAsCharacters() && buf.getTextBuffer() == shared);
        assertEquals("unshare", buf.contentsAsString());
    }

    // ---------- Expand current segment ----------
    @Test
    public void testExpandCurrentSegment_increasesCapacity() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        int oldLen = buf.getCurrentSegment().length;
        buf.expandCurrentSegment();
        int newLen = buf.getCurrentSegment().length;
        assertTrue(newLen > oldLen);
    }

    // ---------- Finish current segment ----------
    @Test
    public void testFinishCurrentSegment_createsSegments() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        // Force a segment to be created
        for (int i = 0; i < 2000; i++) {
            buf.append('a');
        }
        // finish current segment
        char[] newSeg = buf.finishCurrentSegment();
        assertNotNull(newSeg);
        // Should have segments now
        assertTrue(buf.size() >= 2000);
    }

    // ---------- Contents as decimal ----------
    @Test
    public void testContentsAsDecimal_validNumber_returnsBigDecimal() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("123.45");
        BigDecimal dec = buf.contentsAsDecimal();
        assertEquals(new BigDecimal("123.45"), dec);
    }

    // ---------- Contents as double ----------
    @Test
    public void testContentsAsDouble_validNumber_returnsDouble() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("567.89");
        double d = buf.contentsAsDouble();
        assertEquals(567.89, d, 1e-9);
    }

    // ---------- Contents as array ----------
    @Test
    public void testContentsAsArray_returnsCorrectArray() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("array");
        char[] arr = buf.contentsAsArray();
        assertArrayEquals(new char[] {'a','r','r','a','y'}, arr);
    }

    // ---------- Empty string ----------
    @Test
    public void testResetWithString_emptyString_sizeZero() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("");
        assertEquals(0, buf.size());
        assertEquals("", buf.contentsAsString());
    }

    // ========== NEW TEST CASES (for uncovered coverage) ==========

    // ---------- contentsAsString with shared buffer ----------
    @Test
    public void testContentsAsString_sharedBuffer_returnsCorrectString() {
        TextBuffer buf = createBuffer();
        char[] shared = "sharedContent".toCharArray();
        buf.resetWithShared(shared, 0, 13);
        assertEquals("sharedContent", buf.contentsAsString());
        // ensure still using shared buffer
        assertTrue(buf.hasTextAsCharacters());
        assertSame(shared, buf.getTextBuffer());
    }

    // ---------- contentsAsString after caching result array ----------
    @Test
    public void testContentsAsString_afterCachingResultArray_returnsCorrectString() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("cachedString");
        buf.contentsAsArray();  // creates _resultArray
        assertEquals("cachedString", buf.contentsAsString());
        // should use _resultArray for string conversion
        assertEquals(12, buf.size());
    }

    // ---------- contentsAsString with multiple segments ----------
    @Test
    public void testContentsAsString_withSegments_returnsCorrectString() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        StringBuilder sb = new StringBuilder();
        // Append enough to create segments (over 500 chars per segment default)
        for (int i = 0; i < 1500; i++) {
            buf.append('a');
            sb.append('a');
        }
        String expected = sb.toString();
        assertEquals(expected, buf.contentsAsString());
        assertEquals(1500, buf.size());
    }

    // ---------- getCurrentSegment after emptyAndGetCurrentSegment ----------
    @Test
    public void testGetCurrentSegment_returnsCurrentSegment() {
        TextBuffer buf = createBuffer();
        char[] seg = buf.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertTrue(seg.length > 0);
        // same as getCurrentSegment()
        assertSame(seg, buf.getCurrentSegment());
    }

    // ---------- setCurrentLength updates size ----------
    @Test
    public void testSetCurrentLength_updatesSize() {
        TextBuffer buf = createBuffer();
        buf.emptyAndGetCurrentSegment();
        buf.append("abc", 0, 3);
        int oldSize = buf.size();
        // artificially shrink length
        buf.setCurrentLength(2);
        assertEquals(2, buf.size());
        // restore
        buf.setCurrentLength(3);
        assertEquals(oldSize, buf.size());
    }

    // ---------- getTextOffset in various scenarios ----------
    @Test
    public void testGetTextOffset_variousScenarios() {
        TextBuffer buf = createBuffer();
        // shared buffer with offset
        char[] shared = "offsetTest".toCharArray();
        buf.resetWithShared(shared, 2, 8); // offset 2, length 8 -> "fsetTest"
        assertEquals(2, buf.getTextOffset());
        assertEquals("fsetTest", buf.contentsAsString());
        // after resetWithString offset should be 0
        buf.resetWithString("noffset");
        assertEquals(0, buf.getTextOffset());
        // after resetWithCopy offset should be 0
        buf.resetWithCopy("copy".toCharArray(), 0, 4);
        assertEquals(0, buf.getTextOffset());
        // after segments (emptyAndGetCurrentSegment) offset is 0
        buf.emptyAndGetCurrentSegment();
        assertEquals(0, buf.getTextOffset());
    }

    // ---------- hasTextAsCharacters in different states ----------
    @Test
    public void testHasTextAsCharacters_various() {
        TextBuffer buf = createBuffer();
        // shared buffer -> true
        char[] shared = "test".toCharArray();
        buf.resetWithShared(shared, 0, 4);
        assertTrue(buf.hasTextAsCharacters());
        // resetWithString -> false (because _resultString is set)
        buf.resetWithString("string");
        assertFalse(buf.hasTextAsCharacters());
        // after contentsAsArray -> false (still _resultString)
        buf.contentsAsArray();
        assertFalse(buf.hasTextAsCharacters());
        // after segments -> true (since _segments are character arrays)
        buf.emptyAndGetCurrentSegment();
        buf.append("x", 0, 1);
        assertTrue(buf.hasTextAsCharacters());
    }

    // ---------- releaseBuffers with actual allocator ----------
    @Test
    public void testReleaseBuffers_withAllocator_doesNotThrow() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer buf = new TextBuffer(recycler);
        // use some buffer space
        buf.emptyAndGetCurrentSegment();
        buf.append("release", 0, 7);
        buf.releaseBuffers();
        // after release, internal state should be cleared
        assertEquals(0, buf.size());
        // calling again should not throw
        buf.releaseBuffers();
    }

    // ---------- contentsAsDecimal with invalid input ----------
    @Test(expected = NumberFormatException.class)
    public void testContentsAsDecimal_invalidFormat_throws() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("notanumber");
        buf.contentsAsDecimal();
    }

    // ---------- contentsAsDouble with invalid input ----------
    @Test(expected = NumberFormatException.class)
    public void testContentsAsDouble_invalidFormat_throws() {
        TextBuffer buf = createBuffer();
        buf.resetWithString("notadouble");
        buf.contentsAsDouble();
    }
}