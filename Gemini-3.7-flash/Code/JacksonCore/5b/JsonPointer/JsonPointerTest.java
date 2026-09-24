package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonPointerTest {

    // Tests empty string and null input compiling to EMPTY instance
    @Test
    public void testCompile_emptyAndNull_returnsEmpty() {
        JsonPointer pNull = JsonPointer.compile(null);
        JsonPointer pEmpty = JsonPointer.compile("");

        assertSame(JsonPointer.EMPTY, pNull);
        assertSame(JsonPointer.EMPTY, pEmpty);
        assertTrue(pNull.matches());
        assertEquals("", pNull.getMatchingProperty());
        assertEquals(-1, pNull.getMatchingIndex());
        assertNull(pNull.tail());
    }

    // Tests exception path when pointer does not start with slash
    @Test(expected = IllegalArgumentException.class)
    public void testCompile_missingLeadingSlash_throwsIllegalArgumentException() {
        JsonPointer.compile("invalid/pointer");
    }

    // Tests valueOf alias method
    @Test
    public void testValueOf_validPointer_returnsExpectedInstance() {
        JsonPointer ptr = JsonPointer.valueOf("/test");
        assertNotNull(ptr);
        assertEquals("/test", ptr.toString());
        assertEquals("test", ptr.getMatchingProperty());
    }

    // Tests single segment parsing
    @Test
    public void testCompile_singlePropertySegment_matchesCorrectly() {
        JsonPointer ptr = JsonPointer.compile("/prop");

        assertFalse(ptr.matches());
        assertEquals("prop", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());

        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertTrue(tail.matches());
    }

    // Tests multiple segment parsing and traversal
    @Test
    public void testCompile_multipleSegments_traversesCorrectly() {
        JsonPointer ptr = JsonPointer.compile("/a/b/c");

        assertEquals("a", ptr.getMatchingProperty());
        assertEquals("/a/b/c", ptr.toString());

        JsonPointer next = ptr.tail();
        assertEquals("b", next.getMatchingProperty());

        next = next.tail();
        assertEquals("c", next.getMatchingProperty());

        next = next.tail();
        assertTrue(next.matches());
        assertNull(next.tail());
    }

    // Tests parsing numeric index segments
    @Test
    public void testCompile_numericIndex_parsesCorrectMatchingIndex() {
        JsonPointer ptr1 = JsonPointer.compile("/0");
        assertEquals(0, ptr1.getMatchingIndex());
        assertTrue(ptr1.mayMatchElement());

        JsonPointer ptr2 = JsonPointer.compile("/123");
        assertEquals(123, ptr2.getMatchingIndex());
        assertTrue(ptr2.mayMatchElement());

        JsonPointer ptr3 = JsonPointer.compile("/2147483647");
        assertEquals(Integer.MAX_VALUE, ptr3.getMatchingIndex());
    }

    // Tests parsing non-numeric strings with digits and characters (regression for index parsing loop)
    @Test
    public void testCompile_nonNumericIndex_returnsMinusOneIndex() {
        JsonPointer ptr1 = JsonPointer.compile("/1a");
        assertEquals(-1, ptr1.getMatchingIndex());
        assertEquals("1a", ptr1.getMatchingProperty());

        JsonPointer ptr2 = JsonPointer.compile("/a1");
        assertEquals(-1, ptr2.getMatchingIndex());

        JsonPointer ptr3 = JsonPointer.compile("/12a34");
        assertEquals(-1, ptr3.getMatchingIndex());

        JsonPointer ptr4 = JsonPointer.compile("/12345678901");
        assertEquals(-1, ptr4.getMatchingIndex());

        JsonPointer ptr5 = JsonPointer.compile("/9999999999");
        assertEquals(-1, ptr5.getMatchingIndex());
    }

    // Tests escape sequences ~0 for tilde and ~1 for slash
    @Test
    public void testCompile_escapedCharacters_unescapesCorrectly() {
        JsonPointer ptr = JsonPointer.compile("/a~1b/c~0d");

        assertEquals("a/b", ptr.getMatchingProperty());
        JsonPointer next = ptr.tail();
        assertEquals("c~d", next.getMatchingProperty());
    }

    // Tests escaping of unexpected character after tilde
    @Test
    public void testCompile_unrecognizedEscapeSequence_preservesTildeAndChar() {
        JsonPointer ptr = JsonPointer.compile("/~x");
        assertEquals("~x", ptr.getMatchingProperty());
    }

    // Tests matchProperty matching and non-matching branches
    @Test
    public void testMatchProperty_matchingAndNonMatching_returnsExpected() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");

        assertNull(ptr.matchProperty("bar"));
        JsonPointer matched = ptr.matchProperty("foo");
        assertNotNull(matched);
        assertEquals("/bar", matched.toString());
        assertNull(matched.matchProperty("other"));

        assertNull(JsonPointer.EMPTY.matchProperty("foo"));
    }

    // Tests matchElement matching and non-matching branches
    @Test
    public void testMatchElement_matchingAndNonMatching_returnsExpected() {
        JsonPointer ptr = JsonPointer.compile("/42/test");

        assertNull(ptr.matchElement(0));
        assertNull(ptr.matchElement(-1));
        JsonPointer matched = ptr.matchElement(42);
        assertNotNull(matched);
        assertEquals("/test", matched.toString());

        JsonPointer notAnIndex = JsonPointer.compile("/abc");
        assertNull(notAnIndex.matchElement(0));
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_contract_validatesCorrectly() {
        JsonPointer p1 = JsonPointer.compile("/a/b");
        JsonPointer p2 = JsonPointer.compile("/a/b");
        JsonPointer p3 = JsonPointer.compile("/a/c");

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertEquals(p1.hashCode(), p2.hashCode());

        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("not a json pointer"));
    }

    // Tests toString representation
    @Test
    public void testToString_variousPointers_returnsExpectedString() {
        assertEquals("", JsonPointer.EMPTY.toString());
        assertEquals("/foo/bar", JsonPointer.compile("/foo/bar").toString());
        assertEquals("/0", JsonPointer.compile("/0").toString());
    }
}