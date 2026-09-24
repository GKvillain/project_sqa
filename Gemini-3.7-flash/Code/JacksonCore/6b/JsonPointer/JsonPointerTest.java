package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonPointerTest {

    // Tests empty and null input handling in compile and valueOf
    @Test
    public void testCompile_emptyAndNullInput_returnsEmptyInstance() {
        JsonPointer p1 = JsonPointer.compile("");
        assertSame(JsonPointer.EMPTY, p1);
        assertTrue(p1.matches());
        assertEquals("", p1.getMatchingProperty());
        assertEquals(-1, p1.getMatchingIndex());
        assertFalse(p1.mayMatchElement());
        assertTrue(p1.mayMatchProperty());
        assertNull(p1.tail());
        assertEquals("", p1.toString());

        JsonPointer p2 = JsonPointer.compile(null);
        assertSame(JsonPointer.EMPTY, p2);

        JsonPointer p3 = JsonPointer.valueOf("");
        assertSame(JsonPointer.EMPTY, p3);
    }

    // Tests invalid input without leading slash throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCompile_noLeadingSlash_throwsIllegalArgumentException() {
        JsonPointer.compile("invalid/path");
    }

    // Tests single segment property path
    @Test
    public void testCompile_singlePropertySegment_parsesCorrectly() {
        JsonPointer p = JsonPointer.compile("/prop");
        assertNotNull(p);
        assertFalse(p.matches());
        assertEquals("prop", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertFalse(p.mayMatchElement());
        assertTrue(p.mayMatchProperty());
        assertEquals("/prop", p.toString());

        JsonPointer tail = p.tail();
        assertNotNull(tail);
        assertTrue(tail.matches());
        assertSame(JsonPointer.EMPTY, tail);
    }

    // Tests multi-segment property path traversal
    @Test
    public void testCompile_multiSegmentPropertyPath_navigatesTailCorrectly() {
        JsonPointer p = JsonPointer.compile("/a/b/c");
        assertEquals("a", p.getMatchingProperty());
        assertFalse(p.matches());

        JsonPointer p2 = p.tail();
        assertEquals("b", p2.getMatchingProperty());
        assertEquals("/b/c", p2.toString());

        JsonPointer p3 = p2.tail();
        assertEquals("c", p3.getMatchingProperty());
        assertEquals("/c", p3.toString());

        JsonPointer p4 = p3.tail();
        assertSame(JsonPointer.EMPTY, p4);
        assertTrue(p4.matches());
    }

    // Tests array element index parsing with normal integer values
    @Test
    public void testCompile_numericIndexSegments_parsesIndicesCorrectly() {
        JsonPointer p0 = JsonPointer.compile("/0");
        assertEquals("0", p0.getMatchingProperty());
        assertEquals(0, p0.getMatchingIndex());
        assertTrue(p0.mayMatchElement());

        JsonPointer p123 = JsonPointer.compile("/123");
        assertEquals("123", p123.getMatchingProperty());
        assertEquals(123, p123.getMatchingIndex());
        assertTrue(p123.mayMatchElement());
    }

    // Tests boundary values for index parsing (Integer.MAX_VALUE and overflows)
    @Test
    public void testCompile_indexBoundaryValues_handlesLimits() {
        JsonPointer maxInt = JsonPointer.compile("/2147483647");
        assertEquals(Integer.MAX_VALUE, maxInt.getMatchingIndex());
        assertTrue(maxInt.mayMatchElement());

        // Overflow 10-digit number beyond Integer.MAX_VALUE
        JsonPointer overflow10 = JsonPointer.compile("/2147483648");
        assertEquals(-1, overflow10.getMatchingIndex());
        assertFalse(overflow10.mayMatchElement());

        // More than 10 digits
        JsonPointer over10Digits = JsonPointer.compile("/10000000000");
        assertEquals(-1, over10Digits.getMatchingIndex());
        assertFalse(over10Digits.mayMatchElement());

        // Non-digit characters in numeric-like segment
        JsonPointer negative = JsonPointer.compile("/-1");
        assertEquals(-1, negative.getMatchingIndex());
        assertFalse(negative.mayMatchElement());

        JsonPointer notAllDigits = JsonPointer.compile("/123a");
        assertEquals(-1, notAllDigits.getMatchingIndex());
        assertFalse(notAllDigits.mayMatchElement());
    }

    // Tests escaped characters ~0 (tilde) and ~1 (slash)
    @Test
    public void testCompile_escapedCharacters_unescapesProperly() {
        JsonPointer p1 = JsonPointer.compile("/~0");
        assertEquals("~", p1.getMatchingProperty());

        JsonPointer p2 = JsonPointer.compile("/~1");
        assertEquals("/", p2.getMatchingProperty());

        JsonPointer p3 = JsonPointer.compile("/a~1b");
        assertEquals("a/b", p3.getMatchingProperty());

        JsonPointer p4 = JsonPointer.compile("/m~0n");
        assertEquals("m~n", p4.getMatchingProperty());

        JsonPointer p5 = JsonPointer.compile("/~01");
        assertEquals("~1", p5.getMatchingProperty());

        JsonPointer p6 = JsonPointer.compile("/~0~1");
        assertEquals("~/", p6.getMatchingProperty());

        // Unrecognized escape sequence retains tilde
        JsonPointer p7 = JsonPointer.compile("/~2");
        assertEquals("~2", p7.getMatchingProperty());
    }

    // Tests escaped characters in multi-segment path
    @Test
    public void testCompile_escapedCharactersInMultipleSegments_parsesCorrectly() {
        JsonPointer p = JsonPointer.compile("/first~1seg/second~0seg/third");
        assertEquals("first/seg", p.getMatchingProperty());
        JsonPointer tail = p.tail();
        assertEquals("second~seg", tail.getMatchingProperty());
        JsonPointer tail2 = tail.tail();
        assertEquals("third", tail2.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, tail2.tail());
    }

    // Tests matchProperty matching and non-matching cases
    @Test
    public void testMatchProperty_matchingAndNonMatching_returnsExpectedResult() {
        JsonPointer p = JsonPointer.compile("/name/age");

        JsonPointer matched = p.matchProperty("name");
        assertNotNull(matched);
        assertEquals("age", matched.getMatchingProperty());

        assertNull(p.matchProperty("other"));
        assertNull(JsonPointer.EMPTY.matchProperty("name"));
    }

    // Tests matchElement matching, non-matching, and negative index cases
    @Test
    public void testMatchElement_matchingAndNonMatching_returnsExpectedResult() {
        JsonPointer p = JsonPointer.compile("/5/sub");

        JsonPointer matched = p.matchElement(5);
        assertNotNull(matched);
        assertEquals("sub", matched.getMatchingProperty());

        assertNull(p.matchElement(4));
        assertNull(p.matchElement(-1));
        assertNull(JsonPointer.EMPTY.matchElement(0));
    }

    // Tests empty segment representation (root and empty intermediate segments)
    @Test
    public void testCompile_emptySegments_parsesCorrectly() {
        JsonPointer p = JsonPointer.compile("/");
        assertEquals("", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertSame(JsonPointer.EMPTY, p.tail());

        JsonPointer p2 = JsonPointer.compile("//");
        assertEquals("", p2.getMatchingProperty());
        assertEquals("", p2.tail().getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p2.tail().tail());
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_variousCases_honorsContract() {
        JsonPointer p1 = JsonPointer.compile("/a/b");
        JsonPointer p2 = JsonPointer.compile("/a/b");
        JsonPointer p3 = JsonPointer.compile("/a/c");

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertEquals(p1.hashCode(), p2.hashCode());

        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("/a/b"));

        assertTrue(JsonPointer.EMPTY.equals(JsonPointer.compile("")));
        assertEquals(JsonPointer.EMPTY.hashCode(), JsonPointer.compile("").hashCode());
    }

    // Tests valueOf alias consistency with compile
    @Test
    public void testValueOf_validString_matchesCompileResult() {
        JsonPointer p1 = JsonPointer.compile("/test/path");
        JsonPointer p2 = JsonPointer.valueOf("/test/path");
        assertEquals(p1, p2);
        assertEquals(p1.toString(), p2.toString());
    }
}