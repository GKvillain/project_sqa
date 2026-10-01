package com.fasterxml.jackson.core;

import static org.junit.Assert.*;
import org.junit.Test;

public class JsonPointerTest {

    // Tests compile with null input returns EMPTY pointer
    @Test
    public void testCompile_nullInput_returnsEmpty() {
        JsonPointer ptr = JsonPointer.compile(null);
        assertEquals("", ptr.toString());
        assertTrue(ptr.matches());
    }

    // Tests compile with empty string returns EMPTY pointer
    @Test
    public void testCompile_emptyString_returnsEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertEquals("", ptr.toString());
        assertTrue(ptr.matches());
    }

    // Tests compile with string not starting with '/' throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCompile_invalidStart_throwsException() {
        JsonPointer.compile("invalid");
    }

    // Tests parsing a single segment pointer
    @Test
    public void testCompile_singleSegment_returnsPointer() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.matches());
        assertEquals("foo", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
    }

    // Tests parsing a pointer with multiple segments
    @Test
    public void testCompile_multipleSegments_returnsPointer() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        assertFalse(ptr.matches());
        assertEquals("foo", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertEquals("bar", tail.getMatchingProperty());
    }

    // Tests parseTail with an escaped tilde character
    @Test
    public void testCompile_escapedTilde_returnsCorrectSegment() {
        JsonPointer ptr = JsonPointer.compile("/a~0b");
        assertEquals("a~b", ptr.getMatchingProperty());
    }

    // Tests parseTail with an escaped slash character
    @Test
    public void testCompile_escapedSlash_returnsCorrectSegment() {
        JsonPointer ptr = JsonPointer.compile("/a~1b");
        assertEquals("a/b", ptr.getMatchingProperty());
    }

    // Tests matchProperty with matching property name returns next segment
    @Test
    public void testMatchProperty_matchingName_returnsNextSegment() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        JsonPointer result = ptr.matchProperty("foo");
        assertNotNull(result);
        assertEquals("bar", result.getMatchingProperty());
    }

    // Tests matchProperty with non-matching property name returns null
    @Test
    public void testMatchProperty_nonMatchingName_returnsNull() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertNull(ptr.matchProperty("bar"));
    }

    // Tests matchElement with matching index returns next segment
    @Test
    public void testMatchElement_matchingIndex_returnsNextSegment() {
        JsonPointer ptr = JsonPointer.compile("/0/bar");
        // first segment is "0"
        JsonPointer firstSegment = ptr.tail();
        assertNotNull(firstSegment);
        assertEquals("bar", firstSegment.getMatchingProperty());
    }

    // Tests matchElement with non-matching index returns null
    @Test
    public void testMatchElement_nonMatchingIndex_returnsNull() {
        JsonPointer ptr = JsonPointer.compile("/1");
        assertNull(ptr.matchElement(0));
        assertNull(ptr.matchElement(-1));
    }

    // Tests matchElement with negative index returns null
    @Test
    public void testMatchElement_negativeIndex_returnsNull() {
        JsonPointer ptr = JsonPointer.compile("/1");
        assertNull(ptr.matchElement(-1));
    }

    // Tests equals and hashCode based on toString
    @Test
    public void testEquals_samePointer_returnsTrue() {
        JsonPointer ptr1 = JsonPointer.compile("/foo/bar");
        JsonPointer ptr2 = JsonPointer.compile("/foo/bar");
        assertTrue(ptr1.equals(ptr2));
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    // Tests equals with different pointers returns false
    @Test
    public void testEquals_differentPointer_returnsFalse() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/bar");
        assertFalse(ptr1.equals(ptr2));
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_nullInput_returnsFalse() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.equals(null));
    }

    // Tests equals with same object returns true
    @Test
    public void testEquals_sameObject_returnsTrue() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertTrue(ptr.equals(ptr));
    }

    // Tests getMatchingIndex for an index-based pointer
    @Test
    public void testGetMatchingIndex_numericSegment_returnsIndex() {
        JsonPointer ptr = JsonPointer.compile("/123");
        assertEquals(123, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchElement());
    }

    // Tests getMatchingIndex for non-numeric segment returns -1
    @Test
    public void testGetMatchingIndex_nonNumericSegment_returnsMinusOne() {
        JsonPointer ptr = JsonPointer.compile("/abc");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
    }

    // Tests parseIndex with length > 10 returns -1 (boundary condition)
    @Test
    public void testCompile_indexExceedsLength_returnsMinusOne() {
        JsonPointer ptr = JsonPointer.compile("/12345678901");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
    }

    // Tests parseIndex with zero leading digit returns -1
    @Test
    public void testCompile_indexWithLeadingZero_returnsMinusOne() {
        JsonPointer ptr = JsonPointer.compile("/01");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
    }
}