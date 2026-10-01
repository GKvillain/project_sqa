package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    // Tests retrieving predefined block tag with case-insensitivity
    @Test
    public void testValueOf_predefinedBlockTag_returnsCorrectTag() {
        Tag p = Tag.valueOf("P");
        Tag pLower = Tag.valueOf("p");
        assertSame(p, pLower);
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertFalse(p.canContainBlock());
    }

    // Tests retrieving predefined inline tag
    @Test
    public void testValueOf_predefinedInlineTag_returnsCorrectTag() {
        Tag span = Tag.valueOf("span");
        assertEquals("span", span.getName());
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
        assertFalse(span.canContainBlock());
    }

    // Tests creating unknown tag with generic properties
    @Test
    public void testValueOf_unknownTag_createsGenericTag() {
        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.isInline());
        assertTrue(custom.canContainBlock());
    }

    // Tests null tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_nullInput_throwsException() {
        Tag.valueOf(null);
    }

    // Tests empty tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_emptyInput_throwsException() {
        Tag.valueOf("");
    }

    // Tests whitespace-only tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_whitespaceInput_throwsException() {
        Tag.valueOf("   ");
    }

    // Tests empty elements properties
    @Test
    public void testIsEmpty_imgAndBr_returnsTrue() {
        Tag img = Tag.valueOf("img");
        Tag br = Tag.valueOf("br");
        Tag div = Tag.valueOf("div");

        assertTrue(img.isEmpty());
        assertTrue(br.isEmpty());
        assertFalse(div.isEmpty());
    }

    // Tests data-only tag properties
    @Test
    public void testIsData_scriptAndStyle_returnsTrue() {
        Tag script = Tag.valueOf("script");
        Tag style = Tag.valueOf("style");
        Tag div = Tag.valueOf("div");

        assertTrue(script.isData());
        assertTrue(style.isData());
        assertFalse(div.isData());
    }

    // Tests preserve whitespace property
    @Test
    public void testPreserveWhitespace_preAndTextarea_returnsTrue() {
        Tag pre = Tag.valueOf("pre");
        Tag textarea = Tag.valueOf("textarea");
        Tag p = Tag.valueOf("p");

        assertTrue(pre.preserveWhitespace());
        assertTrue(textarea.preserveWhitespace());
        assertFalse(p.preserveWhitespace());
    }

    // Tests canContain when parent cannot contain block tags
    @Test
    public void testCanContain_blockInsideInline_returnsFalse() {
        Tag span = Tag.valueOf("span");
        Tag div = Tag.valueOf("div");
        assertFalse(span.canContain(div));
    }

    // Tests canContain when parent can contain block tags
    @Test
    public void testCanContain_blockInsideDiv_returnsTrue() {
        Tag div = Tag.valueOf("div");
        Tag p = Tag.valueOf("p");
        assertTrue(div.canContain(p));
    }

    // Tests canContain for optional closing tag containing itself
    @Test
    public void testCanContain_optionalClosingSameChild_returnsFalse() {
        Tag li = Tag.valueOf("li");
        assertFalse(li.canContain(li));
        Tag tr = Tag.valueOf("tr");
        assertFalse(tr.canContain(tr));
    }

    // Tests canContain for empty or data tag as parent
    @Test
    public void testCanContain_emptyOrDataParent_returnsFalse() {
        Tag img = Tag.valueOf("img");
        Tag script = Tag.valueOf("script");
        Tag span = Tag.valueOf("span");

        assertFalse(img.canContain(span));
        assertFalse(script.canContain(span));
    }

    // Tests canContain rules specific to head element
    @Test
    public void testCanContain_headTagSpecificChildren_returnsExpected() {
        Tag head = Tag.valueOf("head");
        Tag meta = Tag.valueOf("meta");
        Tag script = Tag.valueOf("script");
        Tag link = Tag.valueOf("link");
        Tag div = Tag.valueOf("div");

        assertTrue(head.canContain(meta));
        assertTrue(head.canContain(script));
        assertTrue(head.canContain(link));
        assertFalse(head.canContain(div));
    }

    // Tests canContain rules specific to dt and dd in dl
    @Test
    public void testCanContain_dtAndDd_returnsFalse() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");

        assertFalse(dt.canContain(dd));
        assertFalse(dd.canContain(dt));
    }

    // Tests canContain with null child throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCanContain_nullChild_throwsException() {
        Tag div = Tag.valueOf("div");
        div.canContain(null);
    }

    // Tests isValidParent for valid and invalid parent hierarchy
    @Test
    public void testIsValidParent_hierarchyCheck_returnsExpected() {
        Tag body = Tag.valueOf("body");
        Tag tr = Tag.valueOf("tr");
        Tag td = Tag.valueOf("td");
        Tag table = Tag.valueOf("table");
        Tag html = Tag.valueOf("html");

        assertTrue(tr.isValidParent(td));
        assertFalse(table.isValidParent(td));
        assertTrue(body.isValidParent(html)); // HTML has empty ancestors
    }

    // Tests getImplicitParent returns configured first ancestor
    @Test
    public void testGetImplicitParent_returnsFirstAncestor() {
        Tag td = Tag.valueOf("td");
        assertEquals("tr", td.getImplicitParent().getName());

        Tag li = Tag.valueOf("li");
        assertEquals("ul", li.getImplicitParent().getName());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_returnsExpected() {
        Tag div1 = Tag.valueOf("div");
        Tag div2 = Tag.valueOf("DIV");
        Tag p = Tag.valueOf("p");

        assertTrue(div1.equals(div2));
        assertTrue(div1.equals(div1));
        assertFalse(div1.equals(p));
        assertFalse(div1.equals(null));
        assertFalse(div1.equals("div"));
        assertEquals(div1.hashCode(), div2.hashCode());
    }

    // Tests toString returns tag name
    @Test
    public void testToString_returnsTagName() {
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.toString());
    }

    // Tests trimming tag name on valueOf
    @Test
    public void testValueOf_trimmedTagName_returnsCorrectTag() {
        Tag div = Tag.valueOf("  div  ");
        assertEquals("div", div.getName());
        assertSame(Tag.valueOf("div"), div);
    }

    // Tests getImplicitParent for tag without ancestors returns null
    @Test
    public void testGetImplicitParent_noAncestors_returnsNull() {
        Tag div = Tag.valueOf("div");
        assertNull(div.getImplicitParent());

        Tag custom = Tag.valueOf("custom-tag");
        assertNull(custom.getImplicitParent());
    }

    // Tests canContain with remaining valid head elements
    @Test
    public void testCanContain_headTagOtherAllowedChildren_returnsTrue() {
        Tag head = Tag.valueOf("head");
        Tag title = Tag.valueOf("title");
        Tag style = Tag.valueOf("style");
        Tag base = Tag.valueOf("base");

        assertTrue(head.canContain(title));
        assertTrue(head.canContain(style));
        assertTrue(head.canContain(base));
    }

    // Tests canContain for inline element containing another inline element
    @Test
    public void testCanContain_inlineInsideInline_returnsTrue() {
        Tag span = Tag.valueOf("span");
        Tag b = Tag.valueOf("b");
        Tag a = Tag.valueOf("a");

        assertTrue(span.canContain(b));
        assertTrue(span.canContain(a));
    }

    // Tests canContain for unknown/custom tag
    @Test
    public void testCanContain_customTag_canContainBlockAndInline() {
        Tag custom = Tag.valueOf("custom-tag");
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");

        assertTrue(custom.canContain(div));
        assertTrue(custom.canContain(span));
    }

    // Tests isValidParent with custom and top-level elements
    @Test
    public void testIsValidParent_elementsWithoutAncestors_returnsTrue() {
        Tag div = Tag.valueOf("div");
        Tag custom = Tag.valueOf("custom-tag");
        Tag p = Tag.valueOf("p");

        assertTrue(div.isValidParent(custom));
        assertTrue(div.isValidParent(p));
    }

    // Tests isValidParent with null child throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsValidParent_nullChild_throwsException() {
        Tag div = Tag.valueOf("div");
        div.isValidParent(null);
    }

    // Tests equals and hashCode with custom/unknown tags
    @Test
    public void testEqualsAndHashCode_customTags_returnsExpected() {
        Tag custom1 = Tag.valueOf("custom-tag");
        Tag custom2 = Tag.valueOf("custom-tag");
        Tag custom3 = Tag.valueOf("other-tag");
        Tag div = Tag.valueOf("div");

        assertTrue(custom1.equals(custom2));
        assertFalse(custom1.equals(custom3));
        assertFalse(custom1.equals(div));
        assertEquals(custom1.hashCode(), custom2.hashCode());
    }
}