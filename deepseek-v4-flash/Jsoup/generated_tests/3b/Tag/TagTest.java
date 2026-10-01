package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for Tag, targeting Defects4J bug 3b.
 * Tests focus on canContain, isValidParent, valueOf, and property methods.
 */
public class TagTest {

    // ---- valueOf tests ----
    // Tests null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_null_throwsException() {
        Tag.valueOf(null);
    }

    // Tests empty string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_empty_throwsException() {
        Tag.valueOf(" ");
    }

    // Tests known tag returns the same instance (predefined)
    @Test
    public void testValueOf_knownTag_returnsDefinedTag() {
        Tag p = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("P");
        assertSame(p, p2); // same instance from map
        assertTrue(p.isBlock());
        assertFalse(p.canContainBlock());
    }

    // Tests unknown tag returns a generic tag (not predefined)
    @Test
    public void testValueOf_unknownTag_returnsGenericTag() {
        Tag custom = Tag.valueOf("myCustomTag");
        assertNotNull(custom);
        assertEquals("mycustomtag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.canContainBlock());
        // generic tag should not be the same as any predefined tag
        assertNotSame(custom, Tag.valueOf("div"));
    }

    // ---- canContain tests ----
    // Tests block parent cannot contain block child (e.g., p cannot contain div)
    @Test
    public void testCanContain_blockParentCannotContainBlockChild_returnsFalse() {
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        assertFalse(p.canContain(div));
    }

    // Tests inline parent cannot contain block child (e.g., font cannot contain div)
    @Test
    public void testCanContain_inlineParentCannotContainBlockChild_returnsFalse() {
        Tag font = Tag.valueOf("font");
        Tag div = Tag.valueOf("div");
        assertFalse(font.canContain(div));
    }

    // Tests inline parent can contain inline child (e.g., font can contain span)
    @Test
    public void testCanContain_inlineParentCanContainInlineChild_returnsTrue() {
        Tag font = Tag.valueOf("font");
        Tag span = Tag.valueOf("span");
        assertTrue(font.canContain(span));
    }

    // Tests optional closing tag cannot contain itself (e.g., a cannot contain a)
    @Test
    public void testCanContain_optionalClosingSelf_returnsFalse() {
        Tag a = Tag.valueOf("a");
        assertFalse(a.canContain(a));
    }

    // Tests empty tag cannot contain anything (e.g., img)
    @Test
    public void testCanContain_emptyTag_returnsFalse() {
        Tag img = Tag.valueOf("img");
        Tag span = Tag.valueOf("span");
        assertFalse(img.canContain(span));
    }

    // Tests data-only tag cannot contain inline children (e.g., style cannot contain span)
    @Test
    public void testCanContain_dataTagCannotContainInlineChild_returnsFalse() {
        Tag style = Tag.valueOf("style");
        Tag span = Tag.valueOf("span");
        assertFalse(style.canContain(span));
    }

    // Tests head tag allows specific children (script, meta) but not general block
    @Test
    public void testCanContain_headTag_allowedChild_returnsTrue() {
        Tag head = Tag.valueOf("head");
        Tag script = Tag.valueOf("script");
        Tag meta = Tag.valueOf("meta");
        assertTrue(head.canContain(script));
        assertTrue(head.canContain(meta));
    }

    @Test
    public void testCanContain_headTag_disallowedBlockChild_returnsFalse() {
        Tag head = Tag.valueOf("head");
        Tag div = Tag.valueOf("div");
        assertFalse(head.canContain(div));
    }

    // Tests dt cannot contain dd, and dd cannot contain dt
    @Test
    public void testCanContain_dtCannotContaindd_returnsFalse() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");
        assertFalse(dt.canContain(dd));
    }

    @Test
    public void testCanContain_ddCannotContaindt_returnsFalse() {
        Tag dd = Tag.valueOf("dd");
        Tag dt = Tag.valueOf("dt");
        assertFalse(dd.canContain(dt));
    }

    // Tests null child throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCanContain_nullChild_throwsException() {
        Tag div = Tag.valueOf("div");
        div.canContain(null);
    }

    // ---- isValidParent tests ----
    // Tests valid parent (child's ancestor matches parent)
    @Test
    public void testIsValidParent_validAncestor_returnsTrue() {
        Tag body = Tag.valueOf("body");
        Tag div = Tag.valueOf("div");
        assertTrue(body.isValidParent(div)); // div ancestor includes body
    }

    // Tests invalid parent (child's ancestor does not match)
    @Test
    public void testIsValidParent_invalidAncestor_returnsFalse() {
        Tag div = Tag.valueOf("div");
        Tag title = Tag.valueOf("title"); // title ancestors: HEAD, BODY; not DIV
        assertFalse(div.isValidParent(title));
    }

    // Defect: HTML tag has empty ancestors, so isValidParent returns true for any parent.
    // This should be false because HTML should not be placed inside other tags.
    @Test
    public void testIsValidParent_htmlTagInsideDiv_returnsFalse() {
        Tag div = Tag.valueOf("div");
        Tag html = Tag.valueOf("html");
        // Currently returns true due to empty ancestors check; this is the defect.
        assertFalse("HTML should not be valid inside div", div.isValidParent(html));
    }

    // ---- Property tests ----
    @Test
    public void testIsBlock_knownBlock_returnsTrue() {
        assertTrue(Tag.valueOf("div").isBlock());
    }

    @Test
    public void testIsBlock_knownInline_returnsFalse() {
        assertFalse(Tag.valueOf("font").isBlock());
    }

    @Test
    public void testIsInline_knownInline_returnsTrue() {
        assertTrue(Tag.valueOf("font").isInline());
    }

    @Test
    public void testIsData_dataTag_returnsTrue() {
        assertTrue(Tag.valueOf("script").isData());
    }

    @Test
    public void testIsEmpty_emptyTag_returnsTrue() {
        assertTrue(Tag.valueOf("img").isEmpty());
    }

    @Test
    public void testPreserveWhitespace_preserveTag_returnsTrue() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
    }

    // ---- equals and hashCode ----
    @Test
    public void testEquals_sameTagInstance_returnsTrue() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1, p2);
    }

    @Test
    public void testEquals_differentTag_returnsFalse() {
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        assertNotEquals(p, div);
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // ========== New test cases to increase coverage ==========

    // ---- Additional canContain tests for various parent/child combinations ----
    @Test
    public void testCanContain_divCanContainBlockChild_returnsTrue() {
        Tag div = Tag.valueOf("div");
        Tag childDiv = Tag.valueOf("div");
        assertTrue(div.canContain(childDiv));
    }

    @Test
    public void testCanContain_divCanContainInlineChild_returnsTrue() {
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        assertTrue(div.canContain(span));
    }

    @Test
    public void testCanContain_spanCannotContainBlockChild_returnsFalse() {
        Tag span = Tag.valueOf("span");
        Tag div = Tag.valueOf("div");
        assertFalse(span.canContain(div));
    }

    @Test
    public void testCanContain_spanCanContainInlineChild_returnsTrue() {
        Tag span = Tag.valueOf("span");
        Tag font = Tag.valueOf("font");
        assertTrue(span.canContain(font));
    }

    @Test
    public void testCanContain_selectCanContainOption_returnsTrue() {
        Tag select = Tag.valueOf("select");
        Tag option = Tag.valueOf("option");
        assertTrue(select.canContain(option));
    }

    @Test
    public void testCanContain_selectCannotContainBlockChild_returnsFalse() {
        Tag select = Tag.valueOf("select");
        Tag div = Tag.valueOf("div");
        assertFalse(select.canContain(div));
    }

    @Test
    public void testCanContain_ulCanContainLi_returnsTrue() {
        Tag ul = Tag.valueOf("ul");
        Tag li = Tag.valueOf("li");
        assertTrue(ul.canContain(li));
    }

    @Test
    public void testCanContain_ulCannotContainBlockChild_returnsFalse() {
        Tag ul = Tag.valueOf("ul");
        Tag div = Tag.valueOf("div");
        assertFalse(ul.canContain(div));
    }

    @Test
    public void testCanContain_olCanContainLi_returnsTrue() {
        Tag ol = Tag.valueOf("ol");
        Tag li = Tag.valueOf("li");
        assertTrue(ol.canContain(li));
    }

    @Test
    public void testCanContain_liCanContainUl_returnsTrue() {
        Tag li = Tag.valueOf("li");
        Tag ul = Tag.valueOf("ul");
        assertTrue(li.canContain(ul));
    }

    @Test
    public void testCanContain_liCanContainDiv_returnsTrue() {
        Tag li = Tag.valueOf("li");
        Tag div = Tag.valueOf("div");
        assertTrue(li.canContain(div)); // li can contain flow content including div
    }

    @Test
    public void testCanContain_optionCannotContainAnyChild_returnsFalse() {
        Tag option = Tag.valueOf("option");
        Tag span = Tag.valueOf("span");
        assertFalse(option.canContain(span));
    }

    @Test
    public void testCanContain_tableCanContainThead_returnsTrue() {
        Tag table = Tag.valueOf("table");
        Tag thead = Tag.valueOf("thead");
        assertTrue(table.canContain(thead));
    }

    @Test
    public void testCanContain_tableCanContainTbody_returnsTrue() {
        Tag table = Tag.valueOf("table");
        Tag tbody = Tag.valueOf("tbody");
        assertTrue(table.canContain(tbody));
    }

    @Test
    public void testCanContain_tableCanContainTfoot_returnsTrue() {
        Tag table = Tag.valueOf("table");
        Tag tfoot = Tag.valueOf("tfoot");
        assertTrue(table.canContain(tfoot));
    }

    @Test
    public void testCanContain_tableCanContainTr_returnsTrue() {
        Tag table = Tag.valueOf("table");
        Tag tr = Tag.valueOf("tr");
        assertTrue(table.canContain(tr));
    }

    @Test
    public void testCanContain_tableCannotContainDiv_returnsFalse() {
        Tag table = Tag.valueOf("table");
        Tag div = Tag.valueOf("div");
        assertFalse(table.canContain(div));
    }

    @Test
    public void testCanContain_theadCanContainTh_returnsTrue() {
        Tag thead = Tag.valueOf("thead");
        Tag th = Tag.valueOf("th");
        assertTrue(thead.canContain(th));
    }

    @Test
    public void testCanContain_theadCannotContainTd_returnsFalse() {
        Tag thead = Tag.valueOf("thead");
        Tag td = Tag.valueOf("td");
        assertFalse(thead.canContain(td));
    }

    @Test
    public void testCanContain_tbodyCanContainTr_returnsTrue() {
        Tag tbody = Tag.valueOf("tbody");
        Tag tr = Tag.valueOf("tr");
        assertTrue(tbody.canContain(tr));
    }

    @Test
    public void testCanContain_trCanContainTd_returnsTrue() {
        Tag tr = Tag.valueOf("tr");
        Tag td = Tag.valueOf("td");
        assertTrue(tr.canContain(td));
    }

    @Test
    public void testCanContain_trCanContainTh_returnsTrue() {
        Tag tr = Tag.valueOf("tr");
        Tag th = Tag.valueOf("th");
        assertTrue(tr.canContain(th));
    }

    @Test
    public void testCanContain_trCannotContainDiv_returnsFalse() {
        Tag tr = Tag.valueOf("tr");
        Tag div = Tag.valueOf("div");
        assertFalse(tr.canContain(div));
    }

    @Test
    public void testCanContain_tdCanContainDiv_returnsTrue() {
        Tag td = Tag.valueOf("td");
        Tag div = Tag.valueOf("div");
        assertTrue(td.canContain(div));
    }

    @Test
    public void testCanContain_tdCanContainSpan_returnsTrue() {
        Tag td = Tag.valueOf("td");
        Tag span = Tag.valueOf("span");
        assertTrue(td.canContain(span));
    }

    @Test
    public void testCanContain_thCanContainDiv_returnsTrue() {
        Tag th = Tag.valueOf("th");
        Tag div = Tag.valueOf("div");
        assertTrue(th.canContain(div));
    }

    @Test
    public void testCanContain_formCanContainInput_returnsTrue() {
        Tag form = Tag.valueOf("form");
        Tag input = Tag.valueOf("input");
        assertTrue(form.canContain(input));
    }

    @Test
    public void testCanContain_formCannotContainForm_returnsFalse() {
        Tag form = Tag.valueOf("form");
        Tag form2 = Tag.valueOf("form");
        assertFalse(form.canContain(form2));
    }

    @Test
    public void testCanContain_buttonCanContainSpan_returnsTrue() {
        Tag button = Tag.valueOf("button");
        Tag span = Tag.valueOf("span");
        assertTrue(button.canContain(span));
    }

    @Test
    public void testCanContain_buttonCannotContainButton_returnsFalse() {
        Tag button = Tag.valueOf("button");
        Tag button2 = Tag.valueOf("button");
        assertFalse(button.canContain(button2));
    }

    @Test
    public void testCanContain_bodyCanContainDiv_returnsTrue() {
        Tag body = Tag.valueOf("body");
        Tag div = Tag.valueOf("div");
        assertTrue(body.canContain(div));
    }

    @Test
    public void testCanContain_htmlCanContainHead_returnsTrue() {
        Tag html = Tag.valueOf("html");
        Tag head = Tag.valueOf("head");
        assertTrue(html.canContain(head));
    }

    @Test
    public void testCanContain_htmlCanContainBody_returnsTrue() {
        Tag html = Tag.valueOf("html");
        Tag body = Tag.valueOf("body");
        assertTrue(html.canContain(body));
    }

    @Test
    public void testCanContain_htmlCannotContainDiv_returnsFalse() {
        Tag html = Tag.valueOf("html");
        Tag div = Tag.valueOf("div");
        assertFalse(html.canContain(div));
    }

    @Test
    public void testCanContain_dlCanContainDt_returnsTrue() {
        Tag dl = Tag.valueOf("dl");
        Tag dt = Tag.valueOf("dt");
        assertTrue(dl.canContain(dt));
    }

    @Test
    public void testCanContain_dlCanContainDd_returnsTrue() {
        Tag dl = Tag.valueOf("dl");
        Tag dd = Tag.valueOf("dd");
        assertTrue(dl.canContain(dd));
    }

    @Test
    public void testCanContain_ddCanContainDiv_returnsTrue() {
        Tag dd = Tag.valueOf("dd");
        Tag div = Tag.valueOf("div");
        assertTrue(dd.canContain(div));
    }

    @Test
    public void testCanContain_dtCanContainSpan_returnsTrue() {
        Tag dt = Tag.valueOf("dt");
        Tag span = Tag.valueOf("span");
        assertTrue(dt.canContain(span));
    }

    @Test
    public void testCanContain_aCanContainSpan_returnsTrue() {
        Tag a = Tag.valueOf("a");
        Tag span = Tag.valueOf("span");
        assertTrue(a.canContain(span));
    }

    @Test
    public void testCanContain_aCannotContainDiv_returnsFalse() {
        Tag a = Tag.valueOf("a");
        Tag div = Tag.valueOf("div");
        assertFalse(a.canContain(div));
    }

    @Test
    public void testCanContain_pCanContainSpan_returnsTrue() {
        Tag p = Tag.valueOf("p");
        Tag span = Tag.valueOf("span");
        assertTrue(p.canContain(span));
    }

    @Test
    public void testCanContain_brCannotContainAny_returnsFalse() {
        Tag br = Tag.valueOf("br");
        Tag span = Tag.valueOf("span");
        assertFalse(br.canContain(span));
    }

    @Test
    public void testCanContain_hrCannotContainAny_returnsFalse() {
        Tag hr = Tag.valueOf("hr");
        Tag span = Tag.valueOf("span");
        assertFalse(hr.canContain(span));
    }

    // ---- Additional isValidParent tests ----
    @Test
    public void testIsValidParent_bodyIsValidParentForSpan_returnsTrue() {
        Tag body = Tag.valueOf("body");
        Tag span = Tag.valueOf("span");
        assertTrue(body.isValidParent(span));
    }

    @Test
    public void testIsValidParent_divIsValidParentForSpan_returnsTrue() {
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        assertTrue(div.isValidParent(span));
    }

    @Test
    public void testIsValidParent_pIsValidParentForSpan_returnsTrue() {
        Tag p = Tag.valueOf("p");
        Tag span = Tag.valueOf("span");
        assertTrue(p.isValidParent(span));
    }

    @Test
    public void testIsValidParent_divIsNotValidParentForTitle_returnsFalse() {
        Tag div = Tag.valueOf("div");
        Tag title = Tag.valueOf("title");
        assertFalse(div.isValidParent(title));
    }

    @Test
    public void testIsValidParent_headIsNotValidParentForDiv_returnsFalse() {
        Tag head = Tag.valueOf("head");
        Tag div = Tag.valueOf("div");
        assertFalse(head.isValidParent(div));
    }

    @Test
    public void testIsValidParent_htmlIsNotValidParentForDiv_returnsFalse() {
        Tag html = Tag.valueOf("html");
        Tag div = Tag.valueOf("div");
        // html should not be valid parent for div according to HTML spec
        assertFalse("HTML should not be valid parent for div", html.isValidParent(div));
    }

    @Test
    public void testIsValidParent_htmlIsValidParentForHead_returnsTrue() {
        Tag html = Tag.valueOf("html");
        Tag head = Tag.valueOf("head");
        assertTrue(html.isValidParent(head));
    }

    @Test
    public void testIsValidParent_htmlIsValidParentForBody_returnsTrue() {
        Tag html = Tag.valueOf("html");
        Tag body = Tag.valueOf("body");
        assertTrue(html.isValidParent(body));
    }

    @Test
    public void testIsValidParent_tableIsValidParentForTr_returnsTrue() {
        Tag table = Tag.valueOf("table");
        Tag tr = Tag.valueOf("tr");
        assertTrue(table.isValidParent(tr));
    }

    @Test
    public void testIsValidParent_trIsValidParentForTd_returnsTrue() {
        Tag tr = Tag.valueOf("tr");
        Tag td = Tag.valueOf("td");
        assertTrue(tr.isValidParent(td));
    }

    @Test
    public void testIsValidParent_tbodyIsValidParentForTr_returnsTrue() {
        Tag tbody = Tag.valueOf("tbody");
        Tag tr = Tag.valueOf("tr");
        assertTrue(tbody.isValidParent(tr));
    }

    @Test
    public void testIsValidParent_ulIsValidParentForLi_returnsTrue() {
        Tag ul = Tag.valueOf("ul");
        Tag li = Tag.valueOf("li");
        assertTrue(ul.isValidParent(li));
    }

    @Test
    public void testIsValidParent_liIsValidParentForUl_returnsTrue() {
        Tag li = Tag.valueOf("li");
        Tag ul = Tag.valueOf("ul");
        assertTrue(li.isValidParent(ul));
    }

    @Test
    public void testIsValidParent_dlIsValidParentForDt_returnsTrue() {
        Tag dl = Tag.valueOf("dl");
        Tag dt = Tag.valueOf("dt");
        assertTrue(dl.isValidParent(dt));
    }

    @Test
    public void testIsValidParent_selectIsValidParentForOption_returnsTrue() {
        Tag select = Tag.valueOf("select");
        Tag option = Tag.valueOf("option");
        assertTrue(select.isValidParent(option));
    }

    // ---- Additional valueOf tests ----
    @Test
    public void testValueOf_unknownTagWithHyphen_returnsGenericTag() {
        Tag custom = Tag.valueOf("my-custom-tag");
        assertNotNull(custom);
        assertEquals("my-custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.canContainBlock());
    }

    @Test
    public void testValueOf_unknownTagUpperCase_returnsLowercase() {
        Tag custom = Tag.valueOf("CUSTOM_TAG");
        assertEquals("custom_tag", custom.getName());
    }

    // ---- Additional property tests ----
    @Test
    public void testFormatAsBlock_blockTag_returnsTrue() {
        Tag div = Tag.valueOf("div");
        assertTrue(div.formatAsBlock());
    }

    @Test
    public void testFormatAsBlock_inlineTag_returnsFalse() {
        Tag span = Tag.valueOf("span");
        assertFalse(span.formatAsBlock());
    }

    @Test
    public void testIsFormListed_inputTag_returnsTrue() {
        Tag input = Tag.valueOf("input");
        assertTrue(input.isFormListed());
    }

    @Test
    public void testIsFormListed_divTag_returnsFalse() {
        Tag div = Tag.valueOf("div");
        assertFalse(div.isFormListed());
    }

    @Test
    public void testIsFormSubmittable_inputTag_returnsTrue() {
        Tag input = Tag.valueOf("input");
        assertTrue(input.isFormSubmittable());
    }

    @Test
    public void testIsFormSubmittable_divTag_returnsFalse() {
        Tag div = Tag.valueOf("div");
        assertFalse(div.isFormSubmittable());
    }

    // ---- Equals with null ----
    @Test
    public void testEquals_null_returnsFalse() {
        Tag p = Tag.valueOf("p");
        assertNotNull(p);
        assertFalse(p.equals(null));
    }

    // ---- Additional toString? (optional) ----
    @Test
    public void testToString_knownTag_returnsName() {
        Tag div = Tag.valueOf("div");
        assertEquals("div", div.toString());
    }
}