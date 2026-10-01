package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TagTest {

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullTagName_throwsException() {
        Tag.valueOf(null);
    }

    // Tests empty string input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyTagName_throwsException() {
        Tag.valueOf("");
    }

    // Tests whitespace-only string input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_whitespaceOnlyTagName_throwsException() {
        Tag.valueOf("   ");
    }

    // Tests known block tag properties
    @Test
    public void testValueOf_knownBlockTag_returnsCorrectProperties() {
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertFalse(p.formatAsBlock());
        assertTrue(p.canContainBlock());
        assertFalse(p.isEmpty());
        assertFalse(p.isSelfClosing());
        assertTrue(p.isKnownTag());
    }

    // Tests known inline tag properties
    @Test
    public void testValueOf_knownInlineTag_returnsCorrectProperties() {
        Tag a = Tag.valueOf("a");
        assertEquals("a", a.getName());
        assertFalse(a.isBlock());
        assertTrue(a.isInline());
        assertFalse(a.formatAsBlock());
        assertFalse(a.canContainBlock());
        assertFalse(a.isEmpty());
        assertFalse(a.isSelfClosing());
        assertTrue(a.isKnownTag());
    }

    // Tests known empty self-closing tag properties
    @Test
    public void testValueOf_knownEmptyTag_returnsCorrectProperties() {
        Tag img = Tag.valueOf("img");
        assertEquals("img", img.getName());
        assertFalse(img.isBlock());
        assertTrue(img.isInline());
        assertFalse(img.canContainBlock());
        assertTrue(img.isEmpty());
        assertTrue(img.isSelfClosing());
        assertFalse(img.isData());
        assertTrue(img.isKnownTag());
    }

    // Tests unknown tag default properties
    @Test
    public void testValueOf_unknownTag_returnsDefaultProperties() {
        Tag custom = Tag.valueOf("custom-element");
        assertEquals("custom-element", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.isInline());
        assertTrue(custom.canContainBlock());
        assertFalse(custom.isEmpty());
        assertFalse(custom.isSelfClosing());
        assertFalse(custom.isKnownTag());
    }

    // Tests case insensitivity and whitespace trimming
    @Test
    public void testValueOf_mixedCaseAndWhitespace_returnsCanonicalTag() {
        Tag tag1 = Tag.valueOf("  DIV  ");
        Tag tag2 = Tag.valueOf("div");
        Tag tag3 = Tag.valueOf("DiV");

        assertSame(tag1, tag2);
        assertSame(tag2, tag3);
        assertEquals("div", tag1.getName());
    }

    // Tests static isKnownTag method
    @Test
    public void testIsKnownTag_knownAndUnknownTags_returnsCorrectBoolean() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("p"));
        assertTrue(Tag.isKnownTag("span"));
        assertFalse(Tag.isKnownTag("nonexistenttag"));
    }

    // Tests preserveWhitespace property for formatting tags
    @Test
    public void testPreserveWhitespace_preAndTextareaTags_returnsTrue() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
        assertTrue(Tag.valueOf("plaintext").preserveWhitespace());
        assertTrue(Tag.valueOf("title").preserveWhitespace());
        assertTrue(Tag.valueOf("textarea").preserveWhitespace());
        assertFalse(Tag.valueOf("div").preserveWhitespace());
        assertFalse(Tag.valueOf("p").preserveWhitespace());
    }

    // Tests setSelfClosing on unknown tag
    @Test
    public void testSetSelfClosing_unknownTag_setsSelfClosingTrue() {
        Tag custom = Tag.valueOf("foo");
        assertFalse(custom.isSelfClosing());
        Tag returned = custom.setSelfClosing();
        assertSame(custom, returned);
        assertTrue(custom.isSelfClosing());
    }

    // Tests equals contract
    @Test
    public void testEquals_sameAndDifferentTags_returnsExpectedResult() {
        Tag div1 = Tag.valueOf("div");
        Tag div2 = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");

        assertTrue(div1.equals(div1));
        assertTrue(div1.equals(div2));
        assertFalse(div1.equals(span));
        assertFalse(div1.equals(null));
        assertFalse(div1.equals("div"));
    }

    // Tests hashCode contract
    @Test
    public void testHashCode_equalTags_haveEqualHashCode() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");

        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    // Tests toString returns tag name
    @Test
    public void testToString_validTag_returnsTagName() {
        Tag tag = Tag.valueOf("blockquote");
        assertEquals("blockquote", tag.toString());
    }
}