package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    // Tests valueOf with null input
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullInput_throwsException() {
        Tag.valueOf(null);
    }

    // Tests valueOf with empty string
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyInput_throwsException() {
        Tag.valueOf("");
    }

    // Tests valueOf with blank string (whitespace only)
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_blankInput_throwsException() {
        Tag.valueOf("   ");
    }

    // Tests valueOf with known tag returns the same instance
    @Test
    public void testValueOf_knownTag_returnsSameInstance() {
        Tag first = Tag.valueOf("div");
        Tag second = Tag.valueOf("div");
        assertSame(first, second);
    }

    // Tests valueOf is case insensitive for known tags
    @Test
    public void testValueOf_caseInsensitive_returnsSameKnownTag() {
        Tag lower = Tag.valueOf("div");
        Tag upper = Tag.valueOf("DIV");
        assertSame(lower, upper);
    }

    // Tests valueOf with unknown tag returns a new generic tag (not same instance)
    @Test
    public void testValueOf_unknownTag_returnsNewGenericTag() {
        Tag first = Tag.valueOf("unknown");
        Tag second = Tag.valueOf("unknown");
        assertNotSame(first, second);
    }

    // Tests default properties of an unknown tag (generic)
    @Test
    public void testValueOf_unknownTagProperties_hasDefaultValues() {
        Tag t = Tag.valueOf("unknown");
        assertFalse("isBlock should be false", t.isBlock());
        assertTrue("canContainBlock should be true", t.canContainBlock());
        assertTrue("formatAsBlock should be true", t.formatAsBlock());
        assertTrue("isInline should be true (since !isBlock)", t.isInline());
        assertFalse("isEmpty should be false", t.isEmpty());
        assertFalse("isSelfClosing should be false (empty=false)", t.isSelfClosing());
        assertFalse("preserveWhitespace should be false", t.preserveWhitespace());
    }

    // Tests isKnownTag static method for a known tag
    @Test
    public void testIsKnownTag_static_knownTag_returnsTrue() {
        assertTrue(Tag.isKnownTag("div"));
    }

    // Tests isKnownTag static method for an unknown tag
    @Test
    public void testIsKnownTag_static_unknownTag_returnsFalse() {
        assertFalse(Tag.isKnownTag("unknown"));
    }

    // Tests isBlock for a known block tag
    @Test
    public void testIsBlock_knownBlockTag_returnsTrue() {
        assertTrue(Tag.valueOf("div").isBlock());
    }

    // Tests isBlock for a known inline tag
    @Test
    public void testIsBlock_knownInlineTag_returnsFalse() {
        assertFalse(Tag.valueOf("span").isBlock());
    }

    // Tests formatAsBlock for a known block tag
    @Test
    public void testFormatAsBlock_knownBlockTag_returnsTrue() {
        assertTrue(Tag.valueOf("div").formatAsBlock());
    }

    // Tests formatAsBlock for a tag that should be formatted as inline (e.g. <a>)
    @Test
    public void testFormatAsBlock_knownFormatAsInlineTag_returnsFalse() {
        assertFalse(Tag.valueOf("a").formatAsBlock());
    }

    // Tests canContainBlock for a known block tag
    @Test
    public void testCanContainBlock_knownBlockTag_returnsTrue() {
        assertTrue(Tag.valueOf("div").canContainBlock());
    }

    // Tests canContainBlock for a known inline tag
    @Test
    public void testCanContainBlock_knownInlineTag_returnsFalse() {
        assertFalse(Tag.valueOf("span").canContainBlock());
    }

    // Tests isInline for a known block tag
    @Test
    public void testIsInline_knownBlockTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").isInline());
    }

    // Tests isInline for a known inline tag
    @Test
    public void testIsInline_knownInlineTag_returnsTrue() {
        assertTrue(Tag.valueOf("span").isInline());
    }

    // Tests isEmpty for a known empty tag (img)
    @Test
    public void testIsEmpty_knownEmptyTag_returnsTrue() {
        assertTrue(Tag.valueOf("img").isEmpty());
    }

    // Tests isEmpty for a known non‑empty tag
    @Test
    public void testIsEmpty_knownNonEmptyTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").isEmpty());
    }

    // Tests isSelfClosing for a known empty tag (img)
    @Test
    public void testIsSelfClosing_knownEmptyTag_returnsTrue() {
        assertTrue(Tag.valueOf("img").isSelfClosing());
    }

    // Tests isSelfClosing for a known non‑empty tag that is not self‑closing
    @Test
    public void testIsSelfClosing_knownNonEmptyTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").isSelfClosing());
    }

    // Tests isSelfClosing after calling setSelfClosing on an unknown tag
    @Test
    public void testIsSelfClosing_unknownTagAfterSetSelfClosing_returnsTrue() {
        Tag t = Tag.valueOf("unknown");
        t.setSelfClosing();
        assertTrue(t.isSelfClosing());
    }

    // Tests setSelfClosing returns this (fluent API)
    @Test
    public void testSetSelfClosing_returnsThis() {
        Tag t = Tag.valueOf("unknown");
        assertSame(t, t.setSelfClosing());
    }

    // Tests preserveWhitespace for a known tag that preserves whitespace (pre)
    @Test
    public void testPreserveWhitespace_knownPreserveTag_returnsTrue() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
    }

    // Tests preserveWhitespace for a known tag that does not preserve whitespace
    @Test
    public void testPreserveWhitespace_knownNonPreserveTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").preserveWhitespace());
    }

    // Tests equals with the same object
    @Test
    public void testEquals_sameObject_returnsTrue() {
        Tag t = Tag.valueOf("div");
        assertTrue(t.equals(t));
    }

    // Tests equals for two unknown tags with the same name (should be equal)
    @Test
    public void testEquals_equalUnknownTags_returnsTrue() {
        Tag t1 = Tag.valueOf("xyz");
        Tag t2 = Tag.valueOf("xyz");
        assertTrue(t1.equals(t2));
    }

    // Tests equals for two different known tags
    @Test
    public void testEquals_differentTags_returnsFalse() {
        Tag t1 = Tag.valueOf("div");
        Tag t2 = Tag.valueOf("span");
        assertFalse(t1.equals(t2));
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_consistent() {
        Tag t = Tag.valueOf("div");
        int h1 = t.hashCode();
        assertEquals(h1, t.hashCode());
    }
}