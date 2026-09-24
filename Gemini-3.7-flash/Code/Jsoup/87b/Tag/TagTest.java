package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TagTest {

    // Tests retrieval of a known block tag and its default properties
    @Test
    public void testValueOf_knownBlockTag_returnsBlockProperties() {
        Tag tag = Tag.valueOf("div");
        assertEquals("div", tag.getName());
        assertEquals("div", tag.toString());
        assertTrue(tag.isBlock());
        assertTrue(tag.formatAsBlock());
        assertTrue(tag.canContainBlock());
        assertFalse(tag.isInline());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertTrue(tag.isKnownTag());
    }

    // Tests retrieval of a known inline tag
    @Test
    public void testValueOf_knownInlineTag_returnsInlineProperties() {
        Tag tag = Tag.valueOf("span");
        assertEquals("span", tag.getName());
        assertFalse(tag.isBlock());
        assertFalse(tag.formatAsBlock());
        assertFalse(tag.canContainBlock());
        assertTrue(tag.isInline());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertTrue(tag.isKnownTag());
    }

    // Tests an empty and self-closing tag
    @Test
    public void testValueOf_knownEmptyTag_returnsEmptyAndSelfClosing() {
        Tag tag = Tag.valueOf("img");
        assertTrue(tag.isEmpty());
        assertTrue(tag.isSelfClosing());
        assertFalse(tag.isBlock());
        assertTrue(tag.isInline());
        assertFalse(tag.isData());
    }

    // Tests tag that preserves whitespace
    @Test
    public void testValueOf_preserveWhitespaceTag_returnsPreserveWhitespaceTrue() {
        Tag pre = Tag.valueOf("pre");
        assertTrue(pre.preserveWhitespace());
        assertTrue(pre.isBlock());
        assertFalse(pre.formatAsBlock());

        Tag textarea = Tag.valueOf("textarea");
        assertTrue(textarea.preserveWhitespace());
    }

    // Tests form listed and submittable tags
    @Test
    public void testValueOf_formTags_returnsCorrectFormFlags() {
        Tag input = Tag.valueOf("input");
        assertTrue(input.isFormListed());
        assertTrue(input.isFormSubmittable());
        assertTrue(input.isEmpty());

        Tag button = Tag.valueOf("button");
        assertTrue(button.isFormListed());
        assertFalse(button.isFormSubmittable());
    }

    // Tests unknown tag creation with default properties
    @Test
    public void testValueOf_unknownTag_createsDefaultInlineTag() {
        Tag tag = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", tag.getName());
        assertFalse(tag.isBlock());
        assertTrue(tag.isInline());
        assertTrue(tag.formatAsBlock());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isKnownTag());
    }

    // Tests valueOf with ParseSettings.htmlDefault normalising tag case
    @Test
    public void testValueOf_uppercaseWithHtmlSettings_returnsNormalizedTag() {
        Tag tag = Tag.valueOf("P", ParseSettings.htmlDefault);
        assertEquals("p", tag.getName());
        assertTrue(tag.isBlock());
        assertTrue(tag.isKnownTag());
    }

    // Tests valueOf with ParseSettings.preserveCase keeping case
    @Test
    public void testValueOf_uppercaseWithPreserveCaseSettings_returnsCasePreservedTag() {
        Tag tag = Tag.valueOf("DIV", ParseSettings.preserveCase);
        assertEquals("DIV", tag.getName());
        assertFalse(tag.isKnownTag());
    }

    // Tests static and instance isKnownTag methods
    @Test
    public void testIsKnownTag_validAndInvalidNames_returnsExpected() {
        assertTrue(Tag.isKnownTag("p"));
        assertTrue(Tag.isKnownTag("a"));
        assertFalse(Tag.isKnownTag("unknown-tag"));
        assertFalse(Tag.isKnownTag("DIV"));
    }

    // Tests setSelfClosing on an unknown tag
    @Test
    public void testSetSelfClosing_unknownTag_becomesSelfClosing() {
        Tag tag = Tag.valueOf("custom").setSelfClosing();
        assertTrue(tag.isSelfClosing());
        assertFalse(tag.isEmpty());
    }

    // Tests null tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullTagName_throwsException() {
        Tag.valueOf(null);
    }

    // Tests empty tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyTagName_throwsException() {
        Tag.valueOf("  ", ParseSettings.htmlDefault);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentTags_returnsExpected() {
        Tag tag1 = Tag.valueOf("p");
        Tag tag2 = Tag.valueOf("p");
        Tag tag3 = Tag.valueOf("div");
        Tag custom1 = Tag.valueOf("custom");
        Tag custom2 = Tag.valueOf("custom");
        Tag customSelfClosing = Tag.valueOf("custom").setSelfClosing();

        assertTrue(tag1.equals(tag1));
        assertTrue(tag1.equals(tag2));
        assertEquals(tag1.hashCode(), tag2.hashCode());

        assertFalse(tag1.equals(tag3));
        assertFalse(tag1.equals(null));
        assertFalse(tag1.equals("p"));

        assertTrue(custom1.equals(custom2));
        assertEquals(custom1.hashCode(), custom2.hashCode());
        assertFalse(custom1.equals(customSelfClosing));
    }
}