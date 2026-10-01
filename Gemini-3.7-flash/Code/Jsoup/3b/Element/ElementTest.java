package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests element construction and basic tag/name/block properties
    @Test
    public void testConstruct_validTag_returnsCorrectProperties() {
        Tag divTag = Tag.valueOf("div");
        Element el = new Element(divTag, "http://example.com");

        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertEquals(divTag, el.tag());
        assertTrue(el.isBlock());
        assertEquals("", el.id());
    }

    // Tests element id retrieval and attribute setting
    @Test
    public void testAttrAndId_setIdAttribute_returnsId() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.attr("id", "main-header");
        el.attr("title", "Header Title");

        assertEquals("main-header", el.id());
        assertEquals("Header Title", el.attr("title"));
        assertFalse(el.isBlock());
    }

    // Tests child element and node manipulation (append, prepend child/element/text)
    @Test
    public void testAppendAndPrependChild_mixedNodes_ordersCorrectly() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p").text("Paragraph 1");
        Element child0 = parent.prependElement("span").text("Span 0");
        parent.appendText(" End Text");
        parent.prependText("Start Text ");

        assertEquals(2, parent.children().size());
        assertEquals(child0, parent.child(0));
        assertEquals(child1, parent.child(1));
        assertEquals("Start Text Span 0 Paragraph 1 End Text", parent.text());
    }

    // Tests parent and ancestors retrieval
    @Test
    public void testParents_nestedElements_returnsAncestorStack() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element mid = root.appendElement("section");
        Element leaf = mid.appendElement("p");

        Elements parents = leaf.parents();
        assertEquals(2, parents.size());
        assertEquals(mid, parents.get(0));
        assertEquals(root, parents.get(1));
        assertEquals(mid, leaf.parent());
    }

    // Tests append and prepend HTML fragments into element
    @Test
    public void testAppendAndPrepend_htmlStrings_parsesAndAppendsCorrectly() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>Two</p><p>Three</p>");
        assertEquals(2, div.children().size());
        assertEquals("Two Three", div.text());

        div.prepend("<p>One</p>");
        assertEquals(3, div.children().size());
        assertEquals("One Two Three", div.text());
    }

    // Tests wrap method on element inside parent
    @Test
    public void testWrap_validHtml_wrapsElementCorrectly() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = div.appendElement("p").text("Hello");

        p.wrap("<div class=\"wrapper\"><div class=\"inner\"></div></div>");

        assertEquals("div", p.parent().tagName());
        assertEquals("inner", p.parent().className());
        assertEquals("wrapper", p.parent().parent().className());
        assertEquals(div, p.parent().parent().parent());
    }

    // Tests sibling element navigation methods
    @Test
    public void testSiblingElements_multipleSiblings_returnsExpectedRelatives() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element p1 = parent.appendElement("p").text("1");
        Element p2 = parent.appendElement("p").text("2");
        Element p3 = parent.appendElement("p").text("3");

        assertEquals(3, p2.siblingElements().size());
        assertEquals(p3, p2.nextElementSibling());
        assertEquals(p1, p2.previousElementSibling());
        assertEquals(p1, p2.firstElementSibling());
        assertEquals(p3, p2.lastElementSibling());
        assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());
        assertNull(p3.nextElementSibling());
        assertNull(p1.previousElementSibling());
    }

    // Tests element sibling index when element has no parent
    @Test
    public void testElementSiblingIndex_noParent_returnsZero() {
        Element standalone = new Element(Tag.valueOf("p"), "");
        assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());
    }

    // Tests DOM selection by tag, id, and class
    @Test
    public void testGetElementsByTagIdClass_nestedTree_findsMatches() {
        Element doc = new Element(Tag.valueOf("div"), "");
        Element section = doc.appendElement("section").attr("id", "sec1").attr("class", "container highlight");
        Element p = section.appendElement("p").attr("class", "highlight").text("Text");

        assertEquals(1, doc.getElementsByTag("section").size());
        assertEquals(section, doc.getElementById("sec1"));
        assertEquals(2, doc.getElementsByClass("highlight").size());
        assertEquals(p, doc.getElementsByClass("highlight").get(1));
    }

    // Tests DOM selection by attributes with different value matchers
    @Test
    public void testGetElementsByAttribute_variousMatchers_returnsMatchingElements() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element a1 = div.appendElement("a").attr("href", "http://example.com/one").attr("target", "_blank");
        Element a2 = div.appendElement("a").attr("href", "http://example.com/two");

        assertEquals(2, div.getElementsByAttribute("href").size());
        assertEquals(1, div.getElementsByAttributeValue("target", "_blank").size());
        assertEquals(a1, div.getElementsByAttributeValue("target", "_blank").get(0));
        assertEquals(2, div.getElementsByAttributeValueStarting("href", "http://example.com").size());
        assertEquals(1, div.getElementsByAttributeValueEnding("href", "/two").size());
        assertEquals(a2, div.getElementsByAttributeValueEnding("href", "/two").get(0));
        assertEquals(2, div.getElementsByAttributeValueContaining("href", "example").size());
        assertEquals(1, div.getElementsByAttributeValueNot("target", "_blank").size());
    }

    // Tests DOM selection by sibling index matchers
    @Test
    public void testGetElementsByIndex_indexComparisons_returnsMatchingElements() {
        Element list = new Element(Tag.valueOf("ul"), "");
        Element li0 = list.appendElement("li").text("A");
        Element li1 = list.appendElement("li").text("B");
        Element li2 = list.appendElement("li").text("C");

        Elements lessThan1 = list.getElementsByIndexLessThan(1);
        assertEquals(1, lessThan1.size());
        assertEquals(li0, lessThan1.get(0));

        Elements eq1 = list.getElementsByIndexEquals(1);
        assertEquals(1, eq1.size());
        assertEquals(li1, eq1.get(0));

        Elements gt1 = list.getElementsByIndexGreaterThan(1);
        assertEquals(1, gt1.size());
        assertEquals(li2, gt1.get(0));

        assertEquals(4, list.getAllElements().size());
    }

    // Tests text methods, empty, and hasText checks
    @Test
    public void testTextAndHasText_manipulations_returnsExpectedTextState() {
        Element div = new Element(Tag.valueOf("div"), "");
        assertFalse(div.hasText());
        assertEquals("", div.text());

        div.text("Hello World");
        assertTrue(div.hasText());
        assertEquals("Hello World", div.text());

        div.empty();
        assertFalse(div.hasText());
        assertEquals("", div.text());
        assertEquals(0, div.childNodes().size());
    }

    // Tests class name manipulation methods (addClass, removeClass, toggleClass)
    @Test
    public void testClassNames_manipulations_updatesClassAttribute() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("class", "one two");

        Set<String> classSet = div.classNames();
        assertTrue(classSet.contains("one"));
        assertTrue(classSet.contains("two"));
        assertTrue(div.hasClass("one"));

        div.addClass("three");
        assertTrue(div.hasClass("three"));
        assertEquals("one two three", div.className());

        div.removeClass("two");
        assertFalse(div.hasClass("two"));
        assertEquals("one three", div.className());

        div.toggleClass("three");
        assertFalse(div.hasClass("three"));
        div.toggleClass("three");
        assertTrue(div.hasClass("three"));

        Set<String> customClasses = new HashSet<String>();
        customClasses.add("custom");
        div.classNames(customClasses);
        assertEquals("custom", div.className());
    }

    // Tests form element val method on input and textarea
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValue() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-value");
        assertEquals("test-value", input.val());
        assertEquals("test-value", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("content text");
        assertEquals("content text", textarea.val());
        assertEquals("content text", textarea.text());
    }

    // Tests html getting and setting methods
    @Test
    public void testHtml_getAndSet_modifiesInnerHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<span>Inner Content</span>");
        assertEquals("<span>Inner Content</span>", div.html());
        assertEquals(1, div.children().size());
        assertEquals("span", div.child(0).tagName());
    }

    // Tests outerHtml generation for empty self-closing tag vs normal container tag
    @Test
    public void testOuterHtml_differentTags_rendersExpectedMarkup() {
        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\" />", img.outerHtml().trim());

        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("Hello");
        assertEquals("<div>\n <p>Hello</p>\n</div>", div.outerHtml().trim());
        assertEquals(div.outerHtml(), div.toString());
    }

    // Tests script element data extraction
    @Test
    public void testData_scriptElement_extractsDataNodeContent() {
        Element script = new Element(Tag.valueOf("script"), "");
        DataNode data = new DataNode("var x = 1;", "");
        script.appendChild(data);

        assertEquals("var x = 1;", script.data());
    }

    // Tests selector query evaluation on element
    @Test
    public void testSelect_cssSelector_findsMatchingElements() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p class=\"intro\">One</p><p>Two</p>");

        Elements matches = div.select("p.intro");
        assertEquals(1, matches.size());
        assertEquals("One", matches.get(0).text());
    }

    // Tests equality and hash code contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferent_verifiesContract() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el3 = new Element(Tag.valueOf("span"), "http://example.com");

        assertEquals(el1, el1);
        assertEquals(el1, el2);
        assertEquals(el1.hashCode(), el2.hashCode());
        assertFalse(el1.equals(el3));
        assertFalse(el1.equals("other type"));
        assertFalse(el1.equals(null));
    }

    // Tests null tag argument in constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTag_throwsException() {
        new Element(null, "");
    }
}