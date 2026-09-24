package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    // Tests retrieving standard predefined tags
    @Test
    public void testValueOf_standardTag_returnsCanonicalTag() {
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertFalse(p.canContainBlock());
        assertTrue(p.isInline() == false);
    }

    // Tests case insensitivity and whitespace trimming
    @Test
    public void testValueOf_caseInsensitiveAndTrimmed_returnsTag() {
        Tag tag1 = Tag.valueOf("  DIV  ");
        Tag tag2 = Tag.valueOf("div");
        assertSame(tag1, tag2);
        assertEquals("div", tag1.getName());
    }

    // Tests creating dynamic/unknown tag with default properties
    @Test
    public void testValueOf_unknownTag_createsDefaultInlineTag() {
        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.isInline());
        assertTrue(custom.canContainBlock());
        assertEquals("body", custom.getImplicitParent().getName());
    }

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullTag_throwsException() {
        Tag.valueOf(null);
    }

    // Tests empty string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyTag_throwsException() {
        Tag.valueOf("   ");
    }

    // Tests isBlock and isInline query methods
    @Test
    public void testIsBlockAndIsInline_blockAndInlineTags_returnsCorrectBooleans() {
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");

        assertTrue(div.isBlock());
        assertFalse(div.isInline());
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
    }

    // Tests isEmpty for empty tags vs non-empty tags
    @Test
    public void testIsEmpty_emptyAndNonEmptyTags_returnsCorrectBoolean() {
        Tag img = Tag.valueOf("img");
        Tag div = Tag.valueOf("div");

        assertTrue(img.isEmpty());
        assertFalse(div.isEmpty());
    }

    // Tests isData and preserveWhitespace properties
    @Test
    public void testIsDataAndPreserveWhitespace_scriptAndPre_returnsExpectedFlags() {
        Tag script = Tag.valueOf("script");
        Tag pre = Tag.valueOf("pre");
        Tag p = Tag.valueOf("p");

        assertTrue(script.isData());
        assertTrue(script.preserveWhitespace());
        assertFalse(pre.isData());
        assertTrue(pre.preserveWhitespace());
        assertFalse(p.isData());
        assertFalse(p.preserveWhitespace());
    }

    // Tests containment rules when child is block but parent cannot contain block
    @Test
    public void testCanContain_blockInInlineOrCannotContainBlock_returnsFalse() {
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");

        assertFalse(p.canContain(div));
        assertTrue(p.canContain(span));
        assertTrue(div.canContain(p));
    }

    // Tests containment rules for optional closing tags with identical child
    @Test
    public void testCanContain_selfOptionalClosing_returnsFalse() {
        Tag li = Tag.valueOf("li");
        Tag a = Tag.valueOf("a");
        Tag div = Tag.valueOf("div");

        assertFalse(li.canContain(li));
        assertFalse(a.canContain(a));
        assertTrue(div.canContain(div));
    }

    // Tests containment rules on empty or data tags
    @Test
    public void testCanContain_emptyOrDataTag_returnsFalse() {
        Tag img = Tag.valueOf("img");
        Tag script = Tag.valueOf("script");
        Tag span = Tag.valueOf("span");

        assertFalse(img.canContain(span));
        assertFalse(script.canContain(span));
    }

    // Tests specific containment rules for head tag
    @Test
    public void testCanContain_headTagChildren_returnsExpectedResults() {
        Tag head = Tag.valueOf("head");
        Tag title = Tag.valueOf("title");
        Tag meta = Tag.valueOf("meta");
        Tag script = Tag.valueOf("script");
        Tag div = Tag.valueOf("div");

        assertTrue(head.canContain(title));
        assertTrue(head.canContain(meta));
        assertTrue(head.canContain(script));
        assertFalse(head.canContain(div));
    }

    // Tests dt and dd containment restrictions
    @Test
    public void testCanContain_dtAndDd_returnsFalse() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");

        assertFalse(dt.canContain(dd));
        assertFalse(dd.canContain(dt));
    }

    // Tests null child in canContain throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCanContain_nullChild_throwsException() {
        Tag.valueOf("div").canContain(null);
    }

    // Tests getImplicitParent for various tags
    @Test
    public void testGetImplicitParent_variousTags_returnsExpectedParentOrNull() {
        Tag html = Tag.valueOf("html");
        Tag head = Tag.valueOf("head");
        Tag li = Tag.valueOf("li");

        assertNull(html.getImplicitParent());
        assertEquals("html", head.getImplicitParent().getName());
        assertEquals("ul", li.getImplicitParent().getName());
    }

    // Tests isValidParent for valid and invalid parent combinations
    @Test
    public void testIsValidParent_validAndInvalidParents_returnsCorrectBoolean() {
        Tag html = Tag.valueOf("html");
        Tag head = Tag.valueOf("head");
        Tag ul = Tag.valueOf("ul");
        Tag ol = Tag.valueOf("ol");
        Tag li = Tag.valueOf("li");
        Tag div = Tag.valueOf("div");

        assertTrue(html.isValidParent(head));
        assertTrue(div.isValidParent(html));
        assertTrue(ul.isValidParent(li));
        assertTrue(ol.isValidParent(li));
        assertFalse(div.isValidParent(li));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentTags_handlesEqualityCorrectly() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("P");
        Tag div = Tag.valueOf("div");
        Tag custom1 = Tag.valueOf("custom");
        Tag custom2 = Tag.valueOf("custom");

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(div));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("p"));
        assertTrue(custom1.equals(custom2));

        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals(custom1.hashCode(), custom2.hashCode());
    }

    // Tests toString method
    @Test
    public void testToString_returnsTagName() {
        Tag tag = Tag.valueOf("SPAN");
        assertEquals("span", tag.toString());
    }
}