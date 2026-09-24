package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests preserveWhitespace with deep nesting inside pre tag
    @Test
    public void testText_nestedInPreElements_preservesWhitespace() {
        Element pre = new Element("pre");
        Element div = pre.appendElement("div");
        Element p = div.appendElement("p");
        Element span = p.appendElement("span");
        span.appendText("  deeply  nested   text  ");

        assertEquals("  deeply  nested   text  ", span.text());
        assertEquals("  deeply  nested   text  ", pre.text());
    }

    // Tests text normalization across block and inline children
    @Test
    public void testText_mixedInlineAndBlockNodes_normalizesWhitespace() {
        Element div = new Element("div");
        div.append("<p>Hello <b>there</b> </p><p>world!</p>");

        assertEquals("Hello there world!", div.text());
    }

    // Tests ownText extracting text of parent element only
    @Test
    public void testOwnText_withDirectAndChildText_returnsDirectTextOnly() {
        Element p = new Element("p");
        p.appendText("Hello ");
        p.appendElement("b").appendText("bold");
        p.appendText(" world!");

        assertEquals("Hello world!", p.ownText());
        assertEquals("Hello bold world!", p.text());
    }

    // Tests hasText with blank whitespace and non-blank content
    @Test
    public void testHasText_blankAndNonBlank_returnsCorrectBoolean() {
        Element emptyEl = new Element("div");
        assertFalse(emptyEl.hasText());

        Element whitespaceEl = new Element("div");
        whitespaceEl.appendText("   \n\t  ");
        assertFalse(whitespaceEl.hasText());

        Element textEl = new Element("div");
        textEl.appendText("content");
        assertTrue(textEl.hasText());
    }

    // Tests data extraction from DataNode, Comment and child elements
    @Test
    public void testData_variousDataAndCommentNodes_returnsCombinedData() {
        Element script = new Element("script");
        script.appendChild(new DataNode("var x = 1;"));
        script.appendChild(new Comment(" a comment "));
        Element child = script.appendElement("nested");
        child.appendChild(new DataNode("var y = 2;"));

        assertEquals("var x = 1; a comment var y = 2;", script.data());
    }

    // Tests class attribute manipulation and classNames set operations
    @Test
    public void testClassNames_addClassRemoveClassToggleClass_managesClasses() {
        Element el = new Element("div");
        el.attr("class", "one two");

        Set<String> classes = el.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("one"));
        assertTrue(classes.contains("two"));

        el.addClass("three");
        assertTrue(el.hasClass("three"));
        assertEquals("one two three", el.className());

        el.removeClass("two");
        assertFalse(el.hasClass("two"));
        assertEquals("one three", el.className());

        el.toggleClass("four");
        assertTrue(el.hasClass("four"));
        el.toggleClass("four");
        assertFalse(el.hasClass("four"));

        el.classNames(Collections.<String>emptySet());
        assertEquals("", el.className());
        assertFalse(el.hasAttributes());
    }

    // Tests hasClass region matching and case-insensitivity
    @Test
    public void testHasClass_variousClassPositions_returnsAccurateMatch() {
        Element el = new Element("div");
        el.attr("class", "Header First Middle Last");

        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("First"));
        assertTrue(el.hasClass("middle"));
        assertTrue(el.hasClass("LAST"));
        assertFalse(el.hasClass("Mid"));
        assertFalse(el.hasClass("Firs"));
        assertFalse(el.hasClass("Extra"));
    }

    // Tests CSS selector generation for elements with and without ID
    @Test
    public void testCssSelector_withAndWithoutId_generatesExpectedSelector() {
        Element root = new Element("div");
        Element child1 = root.appendElement("p").attr("class", "lead highlight");
        Element child2 = root.appendElement("p").attr("class", "body");
        Element innerWithId = child2.appendElement("span").attr("id", "target");

        assertEquals("#target", innerWithId.cssSelector());
        assertEquals("div > p.lead.highlight:nth-child(1)", child1.cssSelector());
        assertEquals("div > p.body:nth-child(2)", child2.cssSelector());
    }

    // Tests sibling navigation and sibling index calculation
    @Test
    public void testSiblingElements_traversalMethods_navigatesCorrectly() {
        Element parent = new Element("ul");
        Element li1 = parent.appendElement("li").attr("id", "1");
        Element li2 = parent.appendElement("li").attr("id", "2");
        Element li3 = parent.appendElement("li").attr("id", "3");

        assertEquals(2, li2.siblingElements().size());
        assertEquals(li1, li2.previousElementSibling());
        assertEquals(li3, li2.nextElementSibling());
        assertEquals(li1, li2.firstElementSibling());
        assertEquals(li3, li2.lastElementSibling());
        assertEquals(1, li2.elementSiblingIndex());
        assertNull(li1.previousElementSibling());
        assertNull(li3.nextElementSibling());
    }

    // Tests element selection by tag and by id
    @Test
    public void testGetElements_byTagAndId_returnsMatchingElements() {
        Element doc = new Element("html");
        Element body = doc.appendElement("body");
        Element p1 = body.appendElement("p").attr("id", "p1");
        Element p2 = body.appendElement("p").attr("id", "p2");

        assertEquals(2, doc.getElementsByTag("p").size());
        assertEquals(p1, doc.getElementById("p1"));
        assertEquals(p2, doc.getElementById("p2"));
        assertNull(doc.getElementById("nonexistent"));
    }

    // Tests element selection by attribute criteria
    @Test
    public void testGetElementsByAttribute_variousCriteria_matchesExpected() {
        Element div = new Element("div");
        Element a1 = div.appendElement("a").attr("href", "http://example.com/one").attr("data-test", "val1");
        Element a2 = div.appendElement("a").attr("href", "https://jsoup.org/two").attr("data-test", "val2");

        assertEquals(2, div.getElementsByAttribute("href").size());
        assertEquals(2, div.getElementsByAttributeStarting("data-").size());
        assertEquals(1, div.getElementsByAttributeValue("href", "https://jsoup.org/two").size());
        assertEquals(1, div.getElementsByAttributeValueNot("data-test", "val1").size());
        assertEquals(1, div.getElementsByAttributeValueStarting("href", "http:").size());
        assertEquals(1, div.getElementsByAttributeValueEnding("href", "two").size());
        assertEquals(2, div.getElementsByAttributeValueContaining("href", "://").size());
        assertEquals(2, div.getElementsByAttributeValueMatching("href", Pattern.compile("https?://.*")).size());
    }

    // Tests append, prepend, appendElement, and prependElement
    @Test
    public void testAppendAndPrepend_elementAndHtml_modifiesHierarchy() {
        Element div = new Element("div");
        Element middle = div.appendElement("span").text("middle");
        Element start = div.prependElement("header").text("start");
        Element end = div.appendElement("footer").text("end");

        assertEquals("start", div.child(0).text());
        assertEquals("middle", div.child(1).text());
        assertEquals("end", div.child(2).text());

        div.empty();
        assertEquals(0, div.childNodeSize());
        div.append("<b>bold</b>");
        div.prepend("<i>italic</i>");
        assertEquals("italic", div.child(0).text());
        assertEquals("bold", div.child(1).text());
    }

    // Tests insertChildren at valid index and roll-around index
    @Test
    public void testInsertChildren_validAndNegativeIndex_insertsProperly() {
        Element div = new Element("div");
        Element c1 = new Element("p").text("1");
        Element c2 = new Element("p").text("2");
        div.appendChild(c1);
        div.appendChild(c2);

        Element insertedStart = new Element("span").text("0");
        div.insertChildren(0, insertedStart);
        assertEquals("0", div.child(0).text());

        Element insertedEnd = new Element("span").text("3");
        div.insertChildren(-1, insertedEnd);
        assertEquals("3", div.child(div.children().size() - 1).text());
    }

    // Tests val method on textarea and standard input elements
    @Test
    public void testVal_inputAndTextarea_readsAndWritesValue() {
        Element input = new Element("input").attr("value", "initial");
        assertEquals("initial", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));

        Element textarea = new Element("textarea").text("content");
        assertEquals("content", textarea.val());
        textarea.val("new text");
        assertEquals("new text", textarea.text());
    }

    // Tests outerHtml for normal, self-closing, and block tags
    @Test
    public void testOuterHtml_formattingAndSelfClosing_outputsCorrectHtml() {
        Element img = new Element(Tag.valueOf("img"), "");
        assertEquals("<img>", img.outerHtml());

        Element div = new Element("div").attr("id", "main");
        div.appendElement("p").text("Hello");
        assertEquals("<div id=\"main\">\n <p>Hello</p>\n</div>", div.outerHtml());
    }

    // Tests clone and shallowClone creating separate instances
    @Test
    public void testClone_deepAndShallow_createsDetachedCopies() {
        Element parent = new Element("div").attr("class", "container");
        Element child = parent.appendElement("span").text("text");

        Element clone = parent.clone();
        assertEquals(parent.outerHtml(), clone.outerHtml());
        clone.attr("class", "modified");
        clone.child(0).text("changed");
        assertNotEquals(parent.attr("class"), clone.attr("class"));
        assertNotEquals(parent.child(0).text(), clone.child(0).text());

        Element shallow = parent.shallowClone();
        assertEquals("container", shallow.attr("class"));
        assertEquals(0, shallow.childNodeSize());
    }

    // Tests validation exception on empty tag name
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyString_throwsException() {
        Element el = new Element("div");
        el.tagName("");
    }

    // Tests out of bounds child retrieval throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testChild_invalidIndex_throwsException() {
        Element el = new Element("div");
        el.child(0);
    }
}