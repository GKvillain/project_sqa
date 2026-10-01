package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests tag name modification and retrieval
    @Test
    public void testTagName_validNewTag_updatesTagName() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());

        el.tagName("p");
        assertEquals("p", el.tagName());
        assertEquals("p", el.nodeName());
    }

    // Tests exception on empty tag name
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyTag_throwsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    // Tests isBlock method for block and inline elements
    @Test
    public void testIsBlock_blockAndInlineTags_returnsCorrectBoolean() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        assertTrue(div.isBlock());
        assertFalse(span.isBlock());
    }

    // Tests id retrieval when set and not set
    @Test
    public void testId_withAndWithoutAttribute_returnsExpectedId() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-content");
        assertEquals("main-content", el.id());
    }

    // Tests dataset retrieval for HTML5 data-* attributes
    @Test
    public void testDataset_customDataAttributes_returnsFilteredMap() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-name", "jsoup");
        el.attr("data-version", "1.0");
        el.attr("class", "container");

        assertEquals(2, el.dataset().size());
        assertEquals("jsoup", el.dataset().get("name"));
        assertEquals("1.0", el.dataset().get("version"));
        assertNull(el.dataset().get("class"));
    }

    // Tests parent, parents, and child hierarchy
    @Test
    public void testParentsAndChildren_nestedHierarchy_returnsCorrectNodes() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = root.appendElement("p");
        Element child2 = root.appendElement("span");
        Element grandChild = child1.appendElement("b");

        assertEquals(root, child1.parent());
        assertEquals(2, root.children().size());
        assertEquals(child1, root.child(0));
        assertEquals(child2, root.child(1));

        Elements parents = grandChild.parents();
        assertEquals(2, parents.size());
        assertEquals(child1, parents.get(0));
        assertEquals(root, parents.get(1));
    }

    // Tests insertChildren with normal and negative index
    @Test
    public void testInsertChildren_validIndexes_insertsAtCorrectPositions() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("span"), "");
        Element child2 = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child1);

        parent.insertChildren(0, Collections.singletonList(child2));
        assertEquals(2, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());

        Element child3 = new Element(Tag.valueOf("b"), "");
        parent.insertChildren(-1, Collections.singletonList(child3));
        assertEquals(3, parent.children().size());
        assertEquals("b", parent.child(2).tagName());
    }

    // Tests insertChildren with invalid out of bounds index
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_outOfBoundsIndex_throwsException() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.insertChildren(5, Collections.singletonList(new Element(Tag.valueOf("span"), "")));
    }

    // Tests appending and prepending text and elements
    @Test
    public void testAppendAndPrepend_textAndElements_orderIsCorrect() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendElement("p").text("center");
        el.prependElement("header").text("top");
        el.appendElement("footer").text("bottom");

        assertEquals(3, el.children().size());
        assertEquals("header", el.child(0).tagName());
        assertEquals("p", el.child(1).tagName());
        assertEquals("footer", el.child(2).tagName());

        el.appendText(" -end-");
        el.prependText("-start- ");
        List<TextNode> textNodes = el.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("-start- ", textNodes.get(0).getWholeText());
        assertEquals(" -end-", textNodes.get(1).getWholeText());
    }

    // Tests sibling navigation methods
    @Test
    public void testSiblingNavigation_multipleChildren_returnsExpectedSiblings() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("h1");
        Element child2 = parent.appendElement("p");
        Element child3 = parent.appendElement("span");

        assertEquals(2, child2.siblingElements().size());
        assertEquals(child1, child2.previousElementSibling());
        assertEquals(child3, child2.nextElementSibling());
        assertEquals(child1, child2.firstElementSibling());
        assertEquals(child3, child2.lastElementSibling());
        assertEquals(Integer.valueOf(1), child2.elementSiblingIndex());
        assertNull(child1.previousElementSibling());
        assertNull(child3.nextElementSibling());
    }

    // Tests getElementsBy* methods
    @Test
    public void testGetElementsByMethods_matchingConditions_findsElements() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element p1 = root.appendElement("p").attr("class", "lead highlight").attr("id", "first");
        Element p2 = root.appendElement("p").attr("class", "highlight");
        Element a = root.appendElement("a").attr("href", "http://example.com");

        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(p1, root.getElementById("first"));
        assertEquals(2, root.getElementsByClass("highlight").size());
        assertEquals(1, root.getElementsByClass("lead").size());
        assertEquals(1, root.getElementsByAttribute("href").size());
        assertEquals(1, root.getElementsByAttributeValue("href", "http://example.com").size());
        assertEquals(3, root.getElementsByIndexLessThan(3).size());
    }

    // Tests text() and ownText()
    @Test
    public void testTextAndOwnText_nestedNodes_extractsTextCorrectly() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("One ");
        p.appendElement("b").text("Two");
        p.appendText(" Three");

        assertEquals("One Two Three", p.text());
        assertEquals("One Three", p.ownText());
        assertTrue(p.hasText());
    }

    // Tests hasText false on empty or whitespace element
    @Test
    public void testHasText_emptyElement_returnsFalse() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertFalse(el.hasText());

        el.appendText("   ");
        assertFalse(el.hasText());
    }

    // Tests class attribute manipulations (add, remove, toggle, has)
    @Test
    public void testClassManipulations_addRemoveToggle_updatesClassAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "one two");

        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("TWO"));
        assertFalse(el.hasClass("three"));

        el.addClass("three");
        assertTrue(el.hasClass("three"));

        el.removeClass("one");
        assertFalse(el.hasClass("one"));

        el.toggleClass("two");
        assertFalse(el.hasClass("two"));

        el.toggleClass("two");
        assertTrue(el.hasClass("two"));
    }

    // Tests form element val() method on input and textarea
    @Test
    public void testVal_inputAndTextarea_readsAndSetsValue() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-input");
        assertEquals("test-input", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("test-area");
        assertEquals("test-area", textarea.val());
        assertEquals("test-area", textarea.text());
    }

    // Tests inner HTML getting and setting
    @Test
    public void testHtml_nestedContent_returnsAndSetsHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Hello <b>World</b></p>");

        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("<p>Hello <b>World</b></p>", div.html());

        div.empty();
        assertEquals(0, div.children().size());
        assertEquals("", div.html());
    }

    // Tests outerHtml for self-closing and regular tags
    @Test
    public void testOuterHtml_regularAndSelfClosingTags_rendersCorrectly() {
        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\" />", img.outerHtml());

        Element span = new Element(Tag.valueOf("span"), "");
        span.text("content");
        assertEquals("<span>content</span>", span.outerHtml());
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_clonedElement_independentFromOriginal() {
        Element original = new Element(Tag.valueOf("div"), "");
        original.addClass("first");
        original.appendElement("span").text("text");

        Element clone = original.clone();
        assertEquals(original.html(), clone.html());
        assertEquals(original.className(), clone.className());

        clone.addClass("second");
        assertFalse(original.hasClass("second"));
        assertTrue(clone.hasClass("second"));
    }
}