package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests indexInList / elementSiblingIndex with identical sibling elements (Defects4J 43b)
    @Test
    public void testElementSiblingIndex_identicalSiblings_returnsCorrectIndex() {
        Document doc = Jsoup.parse("<div><p>text</p><p>text</p><p>text</p></div>");
        Elements ps = doc.select("p");
        assertEquals(3, ps.size());
        assertEquals(Integer.valueOf(0), ps.get(0).elementSiblingIndex());
        assertEquals(Integer.valueOf(1), ps.get(1).elementSiblingIndex());
        assertEquals(Integer.valueOf(2), ps.get(2).elementSiblingIndex());
    }

    // Tests nextElementSibling and previousElementSibling with identical sibling elements
    @Test
    public void testSiblingNavigation_identicalSiblings_returnsCorrectSibling() {
        Document doc = Jsoup.parse("<div><p>same</p><p>same</p><p>same</p></div>");
        Elements ps = doc.select("p");
        Element p0 = ps.get(0);
        Element p1 = ps.get(1);
        Element p2 = ps.get(2);

        assertSame(p1, p0.nextElementSibling());
        assertNull(p0.previousElementSibling());

        assertSame(p2, p1.nextElementSibling());
        assertSame(p0, p1.previousElementSibling());

        assertNull(p2.nextElementSibling());
        assertSame(p1, p2.previousElementSibling());
    }

    // Tests elementSiblingIndex when parent is null
    @Test
    public void testElementSiblingIndex_nullParent_returnsZero() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals(Integer.valueOf(0), el.elementSiblingIndex());
    }

    // Tests first and last element siblings navigation
    @Test
    public void testFirstAndLastElementSibling_multipleSiblings_returnsFirstAndLast() {
        Document doc = Jsoup.parse("<div><h1>1</h1><h2>2</h2><h3>3</h3></div>");
        Elements children = doc.select("div").first().children();
        Element h1 = children.get(0);
        Element h2 = children.get(1);

        assertSame(h1, h2.firstElementSibling());
        assertSame(children.get(2), h2.lastElementSibling());

        Element standalone = new Element(Tag.valueOf("div"), "");
        assertNull(standalone.firstElementSibling());
        assertNull(standalone.lastElementSibling());
    }

    // Tests siblingElements excludes self
    @Test
    public void testSiblingElements_multipleChildren_returnsOtherSiblings() {
        Document doc = Jsoup.parse("<div><p>1</p><span>2</span><b>3</b></div>");
        Element span = doc.select("span").first();
        Elements siblings = span.siblingElements();

        assertEquals(2, siblings.size());
        assertEquals("p", siblings.get(0).tagName());
        assertEquals("b", siblings.get(1).tagName());
    }

    // Tests element creation and basic properties
    @Test
    public void testElementProperties_standardCreation_gettersAndSettersCorrect() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());

        el.tagName("span");
        assertEquals("span", el.tagName());
        assertFalse(el.isBlock());
    }

    // Tests tagName empty validation exception
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyString_throwsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    // Tests id and dataset attributes manipulation
    @Test
    public void testIdAndDataset_validDataAttributes_retrievesAndModifiesCorrectly() {
        Document doc = Jsoup.parse("<div id=\"main\" data-user-id=\"123\" data-role=\"admin\"></div>");
        Element div = doc.select("div").first();

        assertEquals("main", div.id());
        Map<String, String> dataset = div.dataset();
        assertEquals("123", dataset.get("user-id"));
        assertEquals("admin", dataset.get("role"));
    }

    // Tests child element retrieval and hierarchy methods
    @Test
    public void testChildAndChildren_mixedNodes_filtersElementsOnly() {
        Document doc = Jsoup.parse("<div>Text 1<p>Para</p>Text 2<span>Span</span></div>");
        Element div = doc.select("div").first();

        assertEquals(2, div.children().size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
        assertEquals(2, div.textNodes().size());
        assertEquals(0, div.dataNodes().size());
    }

    // Tests parents traversal up to document root
    @Test
    public void testParents_nestedElement_returnsAncestorsInOrder() {
        Document doc = Jsoup.parse("<div><ul><li><a href='#'>Link</a></li></ul></div>");
        Element a = doc.select("a").first();
        Elements parents = a.parents();

        assertEquals(4, parents.size());
        assertEquals("li", parents.get(0).tagName());
        assertEquals("ul", parents.get(1).tagName());
        assertEquals("div", parents.get(2).tagName());
        assertEquals("body", parents.get(3).tagName());
    }

    // Tests append, prepend, appendChild, and prependChild
    @Test
    public void testAppendAndPrependNodes_validInputs_correctOrder() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("span").text("middle");
        div.prependElement("b").text("start");
        div.appendElement("i").text("end");

        assertEquals("<b>start</b><span>middle</span><i>end</i>", div.html());

        div.prependText("pre-");
        div.appendText("-post");
        assertEquals("pre-<b>start</b><span>middle</span><i>end</i>-post", div.html());
    }

    // Tests insertChildren with positive and negative index
    @Test
    public void testInsertChildren_validIndices_insertsAtExpectedPositions() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("a");
        div.appendElement("c");

        Element b = new Element(Tag.valueOf("b"), "");
        div.insertChildren(1, Collections.singletonList(b));
        assertEquals("a", div.child(0).tagName());
        assertEquals("b", div.child(1).tagName());
        assertEquals("c", div.child(2).tagName());

        Element d = new Element(Tag.valueOf("d"), "");
        div.insertChildren(-1, Collections.singletonList(d));
        assertEquals("d", div.child(3).tagName());
    }

    // Tests text() and ownText() normalization with nested elements and br tags
    @Test
    public void testTextAndOwnText_nestedElementsAndBr_normalizesWhitespace() {
        Document doc = Jsoup.parse("<div> Hello  <b>there</b> <br> world! </div>");
        Element div = doc.select("div").first();

        assertEquals("Hello there world!", div.text());
        assertEquals("Hello world!", div.ownText());
        assertTrue(div.hasText());

        Element empty = new Element(Tag.valueOf("p"), "");
        assertFalse(empty.hasText());
        assertEquals("", empty.text());
        assertEquals("", empty.ownText());
    }

    // Tests class manipulation methods: className, classNames, addClass, removeClass, toggleClass, hasClass
    @Test
    public void testClassManipulations_classOperations_modifiesCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "one two");

        assertEquals("one two", el.className());
        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("TWO"));
        assertFalse(el.hasClass("three"));

        el.addClass("three");
        assertTrue(el.hasClass("three"));

        el.removeClass("two");
        assertFalse(el.hasClass("two"));

        el.toggleClass("four");
        assertTrue(el.hasClass("four"));
        el.toggleClass("four");
        assertFalse(el.hasClass("four"));
    }

    // Tests val() on input and textarea elements
    @Test
    public void testVal_formElements_getsAndSetsValue() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("testValue");
        assertEquals("testValue", input.val());
        assertEquals("testValue", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("content text");
        assertEquals("content text", textarea.val());
        assertEquals("content text", textarea.text());
    }

    // Tests DOM query methods by tag, id, class, and attribute
    @Test
    public void testGetElementsByQueries_existingAttributesAndTags_findsMatches() {
        Document doc = Jsoup.parse("<div id='wrap'><p class='msg' data-attr='val1'>One</p><p class='msg' data-attr='val2'>Two</p><span>Other</span></div>");
        Element wrap = doc.getElementById("wrap");

        assertNotNull(wrap);
        assertEquals(2, wrap.getElementsByTag("p").size());
        assertEquals(2, wrap.getElementsByClass("msg").size());
        assertEquals(2, wrap.getElementsByAttribute("data-attr").size());
        assertEquals(1, wrap.getElementsByAttributeStarting("data-").size() >= 1 ? 1 : 0);
        assertEquals(1, wrap.getElementsByAttributeValue("data-attr", "val1").size());
        assertEquals(1, wrap.getElementsByAttributeValueStarting("data-attr", "val2").size());
        assertEquals(2, wrap.getElementsContainingText("One").size());
        assertEquals(1, wrap.getElementsContainingOwnText("One").size());
    }

    // Tests getElementsByIndex methods
    @Test
    public void testGetElementsByIndex_validIndexBounds_returnsElements() {
        Document doc = Jsoup.parse("<ol><li>0</li><li>1</li><li>2</li></ol>");
        Element ol = doc.select("ol").first();

        assertEquals(1, ol.getElementsByIndexEquals(1).size());
        assertEquals(1, ol.getElementsByIndexLessThan(1).size());
        assertEquals(1, ol.getElementsByIndexGreaterThan(1).size());
    }

    // Tests cssSelector generation for id and nested paths
    @Test
    public void testCssSelector_variousHierarchy_generatesValidSelector() {
        Document doc = Jsoup.parse("<html><body><div id='content'><p class='intro'>First</p><p class='intro'>Second</p></div></body></html>");
        Element div = doc.getElementById("content");
        assertEquals("#content", div.cssSelector());

        Element p2 = div.select("p").get(1);
        assertEquals("#content > p.intro:nth-child(2)", p2.cssSelector());
    }

    // Tests empty, before, after, wrap and html manipulation
    @Test
    public void testDomManipulation_emptyAndWrapAndSiblingInsertion_correctStructure() {
        Document doc = Jsoup.parse("<div id='target'><p>Inner</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span>Before</span>");
        target.after("<span>After</span>");
        assertEquals("<span>Before</span><div id=\"target\"><p>Inner</p></div><span>After</span>", doc.body().html());

        target.empty();
        assertEquals("", target.html());

        target.html("<b>Bold</b>");
        assertEquals("<b>Bold</b>", target.html());
    }

    // Tests equals, hashCode and clone
    @Test
    public void testEqualsHashCodeClone_sameAndClonedElements_correctContract() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        el1.attr("id", "main");
        Element el2 = el1.clone();

        assertEquals(el1, el2);
        assertEquals(el1.hashCode(), el2.hashCode());

        el2.attr("id", "other");
        assertFalse(el1.equals(el2));
    }
}