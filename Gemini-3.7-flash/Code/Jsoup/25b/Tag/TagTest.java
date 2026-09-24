package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    // Tests known block tag properties
    @Test
    public void testValueOf_knownBlockTag_returnsCorrectProperties() {
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertTrue(p.canContainBlock());
        assertFalse(p.isInline());
        assertFalse(p.isEmpty());
        assertFalse(p.isSelfClosing());
        assertTrue(p.isKnownTag());
        assertFalse(p.formatAsBlock());
    }

    // Tests known inline tag properties
    @Test
    public void testValueOf_knownInlineTag_returnsCorrectProperties() {
        Tag span = Tag.valueOf("span");
        assertEquals("span", span.getName());
        assertFalse(span.isBlock());
        assertFalse(span.canContainBlock());
        assertTrue(span.isInline());
        assertFalse(span.isEmpty());
        assertFalse(span.isSelfClosing());
        assertTrue(span.isKnownTag());
        assertFalse(span.formatAsBlock());
    }

    // Tests empty self-closing known tag properties
    @Test
    public void testValueOf_knownEmptyTag_returnsCorrectProperties() {
        Tag img = Tag.valueOf("img");
        assertEquals("img", img.getName());
        assertTrue(img.isEmpty());
        assertTrue(img.isSelfClosing());
        assertFalse(img.canContainBlock());
        assertFalse(img.isData());
    }

    // Tests tags that preserve whitespace (including textarea)
    @Test
    public void testPreserveWhitespace_preserveWhitespaceTags_returnsTrue() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
        assertTrue(Tag.valueOf("plaintext").preserveWhitespace());
        assertTrue(Tag.valueOf("title").preserveWhitespace());
        assertTrue(Tag.valueOf("textarea").preserveWhitespace());
    }

    // Tests tag that does not preserve whitespace
    @Test
    public void testPreserveWhitespace_normalTag_returnsFalse() {
        assertFalse(Tag.valueOf("p").preserveWhitespace());
        assertFalse(Tag.valueOf("div").preserveWhitespace());
        assertFalse(Tag.valueOf("span").preserveWhitespace());
    }

    // Tests unknown tag creation and default properties
    @Test
    public void testValueOf_unknownTag_createsDefaultTag() {
        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.canContainBlock());
        assertTrue(custom.isInline());
        assertFalse(custom.isEmpty());
        assertFalse(custom.isSelfClosing());
        assertFalse(custom.isKnownTag());
        assertFalse(Tag.isKnownTag("custom-tag"));
    }

    // Tests case insensitivity and trimming of tag names
    @Test
    public void testValueOf_caseInsensitiveAndWhitespace_normalizesTagName() {
        Tag tag1 = Tag.valueOf("  DIV  ");
        Tag tag2 = Tag.valueOf("div");
        assertSame(tag1, tag2);
        assertEquals("div", tag1.getName());
    }

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullInput_throwsException() {
        Tag.valueOf(null);
    }

    // Tests empty string input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyString_throwsException() {
        Tag.valueOf("");
    }

    // Tests whitespace-only input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_whitespaceOnlyString_throwsException() {
        Tag.valueOf("   ");
    }

    // Tests isKnownTag static method
    @Test
    public void testIsKnownTag_knownAndUnknownTags_returnsCorrectBoolean() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("a"));
        assertTrue(Tag.isKnownTag("img"));
        assertFalse(Tag.isKnownTag("unknownTagXYZ"));
    }

    // Tests setSelfClosing on unknown tag
    @Test
    public void testSetSelfClosing_unknownTag_setsSelfClosing() {
        Tag tag = Tag.valueOf("my-tag");
        assertFalse(tag.isSelfClosing());
        tag.setSelfClosing();
        assertTrue(tag.isSelfClosing());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentTags_worksCorrectly() {
        Tag div1 = Tag.valueOf("div");
        Tag div2 = Tag.valueOf("DIV");
        Tag p = Tag.valueOf("p");
        Tag custom1 = Tag.valueOf("custom");
        Tag custom2 = Tag.valueOf("custom");

        assertTrue(div1.equals(div1));
        assertTrue(div1.equals(div2));
        assertEquals(div1.hashCode(), div2.hashCode());

        assertFalse(div1.equals(p));
        assertFalse(div1.equals(null));
        assertFalse(div1.equals("div"));

        assertTrue(custom1.equals(custom2));
        assertEquals(custom1.hashCode(), custom2.hashCode());

        Tag customSelfClosing = Tag.valueOf("custom").setSelfClosing();
        assertFalse(custom1.equals(customSelfClosing));
    }

    // Tests toString returns tag name
    @Test
    public void testToString_validTag_returnsTagName() {
        Tag div = Tag.valueOf("div");
        assertEquals("div", div.toString());

        Tag custom = Tag.valueOf("foo");
        assertEquals("foo", custom.toString());
    }
}