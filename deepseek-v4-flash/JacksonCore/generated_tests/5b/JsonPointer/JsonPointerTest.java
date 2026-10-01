package com.fasterxml.jackson.core;

import static org.junit.Assert.*;
import org.junit.Test;

public class JsonPointerTest {

    // Tests compile with null input returns EMPTY pointer
    @Test
    public void testCompile_nullInput_returnsEmpty() {
        JsonPointer ptr = JsonPointer.compile(null);
        assertTrue(ptr.matches());
        assertEquals("", ptr.toString());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("", ptr.getMatchingProperty());
    }

    // Tests compile with empty string input returns EMPTY pointer
    @Test
    public void testCompile_emptyInput_returnsEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertTrue(ptr.matches());
        assertEquals("", ptr.toString());
    }

    // Tests compile with valid single segment pointer
    @Test
    public void testCompile_singleSegment_createsPointer() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.matches());
        assertEquals("foo", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertNotNull(ptr.tail());
        assertTrue(ptr.tail().matches());
    }

    // Tests compile with pointer that does not start with '/' throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCompile_invalidStart_throwsException() {
        JsonPointer.compile("foo");
    }

    // Tests compile with numeric segment that is valid index
    @Test
    public void testCompile_numericSegment_parsesIndex() {
        JsonPointer ptr = JsonPointer.compile("/123");
        assertEquals(123, ptr.getMatchingIndex());
        assertEquals("123", ptr.getMatchingProperty());
    }

    // Tests compile with numeric segment that is negative (invalid index)
    @Test
    public void testCompile_negativeSegment_returnsMinusOneIndex() {
        // A segment like "/" followed by negative number is not typical JSON Pointer,
        // but the parser sees "-" as non-digit, so index should be -1
        JsonPointer ptr = JsonPointer.compile("/-1");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    // Tests compile with multiple segments
    @Test
    public void testCompile_multipleSegments_createsChain() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        assertFalse(ptr.matches());
        assertEquals("foo", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertEquals("bar", tail.getMatchingProperty());
        assertTrue(tail.tail().matches());
    }

    // Tests matchProperty when property matches returns next segment
    @Test
    public void testMatchProperty_matchingProperty_returnsNext() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        JsonPointer result = ptr.matchProperty("foo");
        assertNotNull(result);
        assertEquals("/bar", result.toString());
    }

    // Tests matchProperty when property does not match returns null
    @Test
    public void testMatchProperty_nonMatchingProperty_returnsNull() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertNull(ptr.matchProperty("bar"));
    }

    // Tests matchElement when index matches returns next segment
    @Test
    public void testMatchElement_matchingIndex_returnsNext() {
        JsonPointer ptr = JsonPointer.compile("/0/bar");
        JsonPointer result = ptr.matchElement(0);
        assertNotNull(result);
        assertEquals("/bar", result.toString());
    }

    // Tests matchElement when index does not match returns null
    @Test
    public void testMatchElement_nonMatchingIndex_returnsNull() {
        JsonPointer ptr = JsonPointer.compile("/1");
        assertNull(ptr.matchElement(0));
    }

    // Tests matchElement with negative index returns null
    @Test
    public void testMatchElement_negativeIndex_returnsNull() {
        JsonPointer ptr = JsonPointer.compile("/0");
        assertNull(ptr.matchElement(-1));
    }

    // Tests toString returns original input string
    @Test
    public void testToString_returnsOriginalString() {
        String input = "/a/b/c";
        JsonPointer ptr = JsonPointer.compile(input);
        assertEquals(input, ptr.toString());
    }

    // Tests equals with same pointer returns true
    @Test
    public void testEquals_samePointer_returnsTrue() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/foo");
        // According to equals implementation, it compares _asString, so they should be equal
        // Actually: compile("/foo") produces "/foo" as _asString, so they should be equal
        assertTrue(ptr1.equals(ptr2));
    }

    // Tests equals with different pointer returns false
    @Test
    public void testEquals_differentPointer_returnsFalse() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/bar");
        assertFalse(ptr1.equals(ptr2));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_consistentWithEquals() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/foo");
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    // Tests tail() returns next segment
    @Test
    public void testTail_returnsNextSegment() {
        JsonPointer ptr = JsonPointer.compile("/a/b");
        JsonPointer tail = ptr.tail();
        assertEquals("/b", tail.toString());
    }

    // Tests tail() on last segment returns EMPTY
    @Test
    public void testTail_onLastSegment_returnsEmpty() {
        JsonPointer ptr = JsonPointer.compile("/a");
        JsonPointer tail = ptr.tail();
        assertTrue(tail.matches());
        assertEquals("", tail.toString());
    }

    // Tests compile with escaped tilde character '~0' should become '~'
    @Test
    public void testCompile_escapedTilde_convertsToTilde() {
        JsonPointer ptr = JsonPointer.compile("/~0");
        // The segment should be "~" (since ~0 is escape for ~)
        assertEquals("~", ptr.getMatchingProperty());
    }

    // Tests compile with escaped slash '~1' should become '/'
    @Test
    public void testCompile_escapedSlash_convertsToSlash() {
        JsonPointer ptr = JsonPointer.compile("/~1");
        // The segment should be "/" (since ~1 is escape for /)
        assertEquals("/", ptr.getMatchingProperty());
    }

    // Tests compile with mixed escape and normal segments
    @Test
    public void testCompile_mixedEscapedAndNormal_createsCorrectPointer() {
        JsonPointer ptr = JsonPointer.compile("/foo~1bar");
        assertEquals("foo/bar", ptr.getMatchingProperty());
    }

    // Tests compile with 10-digit numeric segment that exceeds Integer.MAX_VALUE returns -1 index
    @Test
    public void testCompile_longIndexExceedsMaxInt_returnsMinusOne() {
        // "1234567890" >= 10 digits, but we need > 10 digits or 10-digit number > Integer.MAX_VALUE
        // Integer.MAX_VALUE = 2147483647 (10 digits)
        // 2147483648 is 10 digits but > Integer.MAX_VALUE
        // However string length is 10, so len == 10, and it will parse as long then check > Integer.MAX_VALUE
        // Use "2147483648" which is 10 digits and > Integer.MAX_VALUE
        JsonPointer ptr = JsonPointer.compile("/2147483648");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    // Tests compile with 10-digit numeric segment that is <= Integer.MAX_VALUE returns valid index
    @Test
    public void testCompile_longIndexWithinMaxInt_returnsIndex() {
        // "2147483647" is 10 digits and equals Integer.MAX_VALUE
        JsonPointer ptr = JsonPointer.compile("/2147483647");
        assertEquals(Integer.MAX_VALUE, ptr.getMatchingIndex());
    }

    // Tests compile with segment length > 10 returns -1 index
    @Test
    public void testCompile_segmentLengthGreaterThanTen_returnsMinusOne() {
        // A segment with 11 digits should return -1 (len > 10)
        JsonPointer ptr = JsonPointer.compile("/12345678901");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    // Tests compile with segment that has leading zeros returns valid index (parsed as int)
    @Test
    public void testCompile_leadingZeros_parsesCorrectly() {
        JsonPointer ptr = JsonPointer.compile("/001");
        assertEquals(1, ptr.getMatchingIndex());
    }

    // Tests mayMatchProperty returns true for non-empty segment
    @Test
    public void testMayMatchProperty_nonEmptySegment_returnsTrue() {
        JsonPointer ptr = JsonPointer.compile("/a");
        assertTrue(ptr.mayMatchProperty());
    }

    // Tests mayMatchElement returns true for valid index
    @Test
    public void testMayMatchElement_validIndex_returnsTrue() {
        JsonPointer ptr = JsonPointer.compile("/5");
        assertTrue(ptr.mayMatchElement());
    }

    // Tests mayMatchElement returns false for non-numeric segment
    @Test
    public void testMayMatchElement_nonNumericSegment_returnsFalse() {
        JsonPointer ptr = JsonPointer.compile("/abc");
        assertFalse(ptr.mayMatchElement());
    }

    // Tests getMatchingProperty on empty pointer returns empty string
    @Test
    public void testGetMatchingProperty_emptyPointer_returnsEmptyString() {
        JsonPointer ptr = JsonPointer.compile("");
        assertEquals("", ptr.getMatchingProperty());
    }
}