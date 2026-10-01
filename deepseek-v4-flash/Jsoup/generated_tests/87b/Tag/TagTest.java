package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    // Test that known block tag is properly identified with correct properties
    @Test
    public void testValueOf_knownBlockTag_returnsKnownTagWithCorrectProperties() {
        Tag tag = Tag.valueOf("div");
        assertNotNull(tag);
        assertTrue(tag.isBlock());
        assertTrue(tag.isKnownTag());
        assertEquals("div", tag.getName());
    }

    // Test that unknown tag returns a generic inline tag
    @Test
    public void testValueOf_unknownTag_returnsGenericTag() {
        Tag tag = Tag.valueOf("unknown");
        assertNotNull(tag);
        assertFalse(tag.isBlock());
        assertFalse(tag.isKnownTag());
    }

    // Test that null input to valueOf throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullInput_throwsException() {
        Tag.valueOf(null);
    }

    // Test that empty string input throws IllegalArgumentException after normalization
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyString_throwsException() {
        Tag.valueOf("");
    }

    // Defect detection: uppercase tag name should be recognized as block (case‑insensitivity)
    @Test
    public void testValueOf_uppercasePTag_shouldBeRecognizedAsBlock() {
        Tag tag = Tag.valueOf("P");
        assertTrue("Uppercase tag should be recognized as block", tag.isBlock());
    }

    // Test that known inline tag is correctly identified
    @Test
    public void testValueOf_knownInlineTag_returnsInlineTag() {
        Tag tag = Tag.valueOf("a");
        assertNotNull(tag);
        assertFalse(tag.isBlock());
        assertFalse(tag.formatAsBlock());
    }

    // Test isBlock returns true for a known block tag
    @Test
    public void testIsBlock_knownBlockTag_returnsTrue() {
        assertTrue(Tag.valueOf("section").isBlock());
    }

    // Test isInline returns true for a known inline tag
    @Test
    public void testIsInline_knownInlineTag_returnsTrue() {
        assertTrue(Tag.valueOf("span").isInline());
    }

    // Test isData returns false for a tag that can contain inline content
    @Test
    public void testIsData_knownTag_returnsFalse() {
        assertFalse(Tag.valueOf("p").isData());
    }

    // Test isEmpty returns true for a known void element
    @Test
    public void testIsEmpty_knownEmptyTag_returnsTrue() {
        assertTrue(Tag.valueOf("br").isEmpty());
    }

    // Test isSelfClosing returns true for a known empty (void) element
    @Test
    public void testIsSelfClosing_knownEmptyTag_returnsTrue() {
        assertTrue(Tag.valueOf("img").isSelfClosing());
    }

    // Test isSelfClosing after setSelfClosing() is called
    @Test
    public void testIsSelfClosing_setSelfClosing_returnsTrue() {
        Tag tag = Tag.valueOf("custom");
        assertFalse(tag.isSelfClosing());
        tag.setSelfClosing();
        assertTrue(tag.isSelfClosing());
    }

    // Test that two unknown tags with same name are equal and have same hash code
    @Test
    public void testEquals_equalUnknownTags_returnsTrue() {
        Tag t1 = Tag.valueOf("custom");
        Tag t2 = Tag.valueOf("custom");
        assertTrue(t1.equals(t2));
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    // Test that unknown tags with different names are not equal
    @Test
    public void testEquals_differentUnknownTags_returnsFalse() {
        Tag t1 = Tag.valueOf("custom1");
        Tag t2 = Tag.valueOf("custom2");
        assertFalse(t1.equals(t2));
    }

    // Test equals returns false when compared to null
    @Test
    public void testEquals_comparedWithNull_returnsFalse() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.equals(null));
    }

    // Test isFormListed for a known form-associated element
    @Test
    public void testIsFormListed_knownFormListedTag_returnsTrue() {
        assertTrue(Tag.valueOf("input").isFormListed());
    }

    // Test isFormSubmittable for a known form-submittable element
    @Test
    public void testIsFormSubmittable_knownFormSubmitTag_returnsTrue() {
        assertTrue(Tag.valueOf("textarea").isFormSubmittable());
    }

    // Test preserveWhitespace for tags that preserve whitespace and those that do not
    @Test
    public void testPreserveWhitespace_knownPreserveTag_returnsTrue() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
        assertFalse(Tag.valueOf("p").preserveWhitespace());
    }

    // Test toString returns the tag name
    @Test
    public void testToString_returnsTagName() {
        assertEquals("div", Tag.valueOf("div").toString());
        assertEquals("custom", Tag.valueOf("custom").toString());
    }

    // Test getName returns the tag name
    @Test
    public void testGetName_returnsTagName() {
        assertEquals("div", Tag.valueOf("div").getName());
    }

    // Test static isKnownTag for a known tag
    @Test
    public void testIsKnownTag_static_knownTag_returnsTrue() {
        assertTrue(Tag.isKnownTag("p"));
    }

    // Test static isKnownTag for an unknown tag
    @Test
    public void testIsKnownTag_static_unknownTag_returnsFalse() {
        assertFalse(Tag.isKnownTag("unknown"));
    }

    // Test canContainBlock returns same as isBlock for a block tag
    @Test
    public void testCanContainBlock_knownBlockTag_returnsSameAsIsBlock() {
        Tag tag = Tag.valueOf("div");
        assertEquals(tag.isBlock(), tag.canContainBlock());
    }

    // Test formatAsBlock for tags that should be formatted inline
    @Test
    public void testFormatAsBlock_knownFormatAsInlineTag_returnsFalse() {
        assertFalse(Tag.valueOf("title").formatAsBlock());
        assertTrue(Tag.valueOf("div").formatAsBlock());
    }

    // ===== Additional tests for uncovered coverage =====

    @Test
    public void testValueOf_knownTagMultipleCalls_returnsSameInstance() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");
        assertSame(tag1, tag2);
    }

    @Test
    public void testEquals_sameReference_returnsTrue() {
        Tag tag = Tag.valueOf("div");
        assertTrue(tag.equals(tag));
    }

    @Test
    public void testStaticIsKnownTag_null_returnsFalse() {
        assertFalse(Tag.isKnownTag(null));
    }

    @Test
    public void testStaticIsKnownTag_empty_returnsFalse() {
        assertFalse(Tag.isKnownTag(""));
    }

    @Test
    public void testIsEmpty_nonVoidTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").isEmpty());
        assertFalse(Tag.valueOf("span").isEmpty());
    }

    @Test
    public void testCanContainBlock_inlineTag_returnsFalse() {
        assertFalse(Tag.valueOf("span").canContainBlock());
    }

    @Test
    public void testIsFormListed_nonFormTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").isFormListed());
    }

    @Test
    public void testIsFormSubmittable_nonFormTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").isFormSubmittable());
    }

    @Test
    public void testPreserveWhitespace_blockTag_returnsFalse() {
        assertFalse(Tag.valueOf("div").preserveWhitespace());
    }

    @Test
    public void testHashCode_consistentForKnownTag() {
        Tag tag = Tag.valueOf("div");
        int h1 = tag.hashCode();
        int h2 = tag.hashCode();
        assertEquals(h1, h2);
    }

    @Test
    public void testValueOf_uppercaseSameAsLowercase_returnsSameInstance() {
        Tag lower = Tag.valueOf("div");
        Tag upper = Tag.valueOf("DIV");
        assertSame(lower, upper);
    }

    @Test
    public void testValueOf_withLeadingTrailingSpaces_returnsNormalizedTag() {
        Tag tag = Tag.valueOf("  div  ");
        assertNotNull(tag);
        assertEquals("div", tag.getName());
        assertTrue(tag.isKnownTag());
    }
}