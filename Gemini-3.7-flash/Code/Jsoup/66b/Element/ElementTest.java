package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ElementTest {

    // Tests cloning behavior and ensures cloned children reference the clone parent
    @Test
    public void testClone_clonedElement_deepClonesChildrenAndUpdatesParentRef() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p1 = div.appendElement("p").text("One");
        Element p2 = div.appendElement("p").text("Two");

        Element clone = div.clone();

        assertNotSame(div, clone);
        assertEquals(2, clone.children().size());
        assertNotSame(div.child(0), clone.child(0));
        assertEquals(clone, clone.child(0).parent());
        assertEquals("One", clone.child(0).text());
        assertEquals("Two", clone.child(1).text());
    }

    // Tests sibling navigation on cloned elements
    @Test
    public void testClone_siblingNavigation_navigatesWithinClone() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("span").text("First");
        div.appendElement("span").text("Second");

        Element clone = div.clone();
        Element firstChild = clone.child(0);
        Element next = firstChild.nextElementSibling();

        assertNotNull(next);
        assertEquals("Second", next.text());
        assertEquals(firstChild, next.previousElementSibling());
    }

    // Tests tagName modification preserving case and returning element
    @Test
    public void testTagName_validName_updatesTag() {
        Element el = new Element("span");
        assertEquals("span", el.tagName());
        assertFalse(el.isBlock());

        el.tagName("DIV");
        assertEquals("DIV", el.tagName());
        assertEquals("DIV", el.nodeName());
    }

    // Tests exception path when setting empty tag name
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyName_throwsException() {
        Element el = new Element("div");
        el.tagName("");
    }

    // Tests id and val methods for elements and textareas
    @Test
    public void testIdAndVal_variousInputs_returnsExpectedValues() {
        Element el = new Element("input");
        el.attr("id", "myId");
        el.attr("value", "initial");

        assertEquals("myId", el.id());
        assertEquals("initial", el.val());

        el.val("updated");
        assertEquals("updated", el.val());

        Element textarea = new Element("textarea");
        textarea.text("comment text");
        assertEquals("comment text", textarea.val());
        textarea.val("new text");
        assertEquals("new text", textarea.val());
    }

    // Tests dataset attributes extraction
    @Test
    public void testDataset_dataAttributes_returnsMapOfDataAttributes() {
        Element el = new Element("div");
        el.attr("data-test", "val1");
        el.attr("data-other-name", "val2");
        el.attr("class", "regular");

        Map<String, String> data = el.dataset();
        assertEquals(2, data.size());
        assertEquals("val1", data.get("test"));
        assertEquals("val2", data.get("other-name"));
    }

    // Tests class attribute operations: add, remove, toggle, hasClass, and classNames set
    @Test
    public void testClassManipulation_variousOperations_maintainsClassSet() {
        Element el = new Element("div");
        assertFalse(el.hasClass("foo"));
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());

        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("FOO"));
        assertEquals("foo", el.className());

        el.addClass("bar");
        assertTrue(el.hasClass("bar"));
        assertEquals(2, el.classNames().size());

        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));

        el.toggleClass("bar");
        assertFalse(el.hasClass("bar"));

        el.toggleClass("baz");
        assertTrue(el.hasClass("baz"));

        Set<String> newClasses = new HashSet<>(Arrays.asList("c1", "c2"));
        el.classNames(newClasses);
        assertTrue(el.hasClass("c1"));
        assertTrue(el.hasClass("c2"));
        assertFalse(el.hasClass("baz"));
    }

    // Tests parent and ancestors retrieval
    @Test
    public void testParents_nestedStructure_returnsCorrectAncestorStack() {
        Element doc = new Element("#root");
        Element body = doc.appendElement("body");
        Element section = body.appendElement("section");
        Element p = section.appendElement("p");

        assertEquals(section, p.parent());
        Elements parents = p.parents();
        assertEquals(2, parents.size());
        assertEquals(section, parents.get(0));
        assertEquals(body, parents.get(1));
    }

    // Tests children, child, and childNodeSize filtering
    @Test
    public void testChildren_mixedNodes_filtersOnlyElementChildren() {
        Element div = new Element("div");
        div.appendText("Text Before ");
        Element span = div.appendElement("span").text("Inside Span");
        div.appendText(" Text After");

        assertEquals(3, div.childNodeSize());
        assertEquals(1, div.children().size());
        assertEquals(span, div.child(0));
        assertEquals(2, div.textNodes().size());
    }

    // Tests element sibling queries and indices
    @Test
    public void testSiblings_multipleSiblings_navigatesCorrectly() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("p").text("1");
        Element child2 = parent.appendElement("span").text("2");
        Element child3 = parent.appendElement("a").text("3");

        assertEquals(0, child1.elementSiblingIndex());
        assertEquals(1, child2.elementSiblingIndex());
        assertEquals(2, child3.elementSiblingIndex());

        assertEquals(child2, child1.nextElementSibling());
        assertNull(child1.previousElementSibling());
        assertEquals(child1, child2.previousElementSibling());
        assertEquals(child3, child2.nextElementSibling());
        assertNull(child3.nextElementSibling());

        assertEquals(child1, child2.firstElementSibling());
        assertEquals(child3, child2.lastElementSibling());

        Elements siblingsOfChild2 = child2.siblingElements();
        assertEquals(2, siblingsOfChild2.size());
        assertEquals(child1, siblingsOfChild2.get(0));
        assertEquals(child3, siblingsOfChild2.get(1));
    }

    // Tests text and ownText extraction with nested tags and whitespace
    @Test
    public void testTextAndOwnText_nestedElements_extractsExpectedText() {
        Element p = new Element("p");
        p.appendText("Hello ");
        p.appendElement("b").text("world");
        p.appendText(" !");

        assertEquals("Hello world !", p.text());
        assertEquals("Hello !", p.ownText());
        assertTrue(p.hasText());

        p.text("New text content");
        assertEquals("New text content", p.text());
        assertEquals(0, p.children().size());
    }

    // Tests data and dataNodes extraction
    @Test
    public void testData_scriptAndDataNodes_extractsData() {
        Element script = new Element("script");
        DataNode dataNode = new DataNode("var x = 1;", "");
        script.appendChild(dataNode);

        assertEquals("var x = 1;", script.data());
        assertEquals(1, script.dataNodes().size());
        assertEquals("var x = 1;", script.dataNodes().get(0).getWholeData());
    }

    // Tests DOM insertion methods: prependChild, insertChildren, append, and prepend
    @Test
    public void testInsertAndAppend_validNodes_modifiesChildrenCorrectly() {
        Element div = new Element("div");
        Element child2 = new Element("span").text("2");
        div.appendChild(child2);

        Element child1 = new Element("span").text("1");
        div.prependChild(child1);
        assertEquals(child1, div.child(0));
        assertEquals(child2, div.child(1));

        Element child3 = new Element("span").text("3");
        div.insertChildren(2, child3);
        assertEquals(child3, div.child(2));

        Element child0 = new Element("span").text("0");
        div.insertChildren(0, Arrays.asList(child0));
        assertEquals(child0, div.child(0));

        div.append("<b>bold</b>");
        assertEquals("bold", div.child(4).text());

        div.prepend("<i>italic</i>");
        assertEquals("italic", div.child(0).text());
    }

    // Tests empty and html methods
    @Test
    public void testHtmlAndEmpty_modifiesInnerHtml() {
        Element div = new Element("div");
        div.html("<p>Paragraph 1</p><p>Paragraph 2</p>");
        assertEquals(2, div.children().size());
        assertEquals("Paragraph 1", div.child(0).text());

        div.empty();
        assertEquals(0, div.childNodeSize());
        assertEquals(0, div.children().size());
        assertFalse(div.hasText());
    }

    // Tests DOM query methods: select, selectFirst, is, and getElementsBy*
    @Test
    public void testQueryAndSelectorMethods_validQueries_returnsMatchedElements() {
        Element root = new Element("div");
        root.attr("id", "container");
        Element header = root.appendElement("h1").attr("class", "title main").text("Heading");
        Element link = root.appendElement("a").attr("href", "http://example.com/test").attr("data-id", "link1").text("Link");
        Element span = root.appendElement("span").text("Hello World");

        assertEquals(header, root.selectFirst("h1.title"));
        assertNull(root.selectFirst("table"));

        Elements links = root.select("a[href*='example']");
        assertEquals(1, links.size());
        assertEquals(link, links.get(0));

        assertTrue(header.is("h1.main"));
        assertFalse(header.is("p"));

        assertEquals(1, root.getElementsByTag("h1").size());
        assertEquals(header, root.getElementById("container").child(0));
        assertEquals(1, root.getElementsByClass("title").size());
        assertEquals(1, root.getElementsByAttribute("data-id").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("href", "http://example.com/test").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("href", "http://").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("href", "/test").size());
        assertEquals(1, root.getElementsByAttributeValueContaining("href", "example").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("href", ".*example.*").size());
        assertEquals(1, root.getElementsContainingText("World").size());
        assertEquals(1, root.getElementsContainingOwnText("Heading").size());
        assertEquals(1, root.getElementsMatchingText(".*World.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(".*Heading.*").size());
        assertEquals(4, root.getAllElements().size());
    }

    // Tests cssSelector generation
    @Test
    public void testCssSelector_variousElements_generatesExpectedSelector() {
        Element div = new Element("div").attr("id", "main");
        assertEquals("#main", div.cssSelector());

        Element section = new Element("section");
        Element p1 = section.appendElement("p").attr("class", "text");
        Element p2 = section.appendElement("p").attr("class", "text");

        assertEquals("p.text:nth-child(1)", p1.cssSelector());
        assertEquals("p.text:nth-child(2)", p2.cssSelector());
    }

    // Tests boolean attribute setting and removal
    @Test
    public void testAttr_booleanAttribute_setsAndRemovesProperly() {
        Element input = new Element("input");
        input.attr("disabled", true);
        assertTrue(input.hasAttr("disabled"));

        input.attr("disabled", false);
        assertFalse(input.hasAttr("disabled"));
    }
}