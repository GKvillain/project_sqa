package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests elementSiblingIndex with identical sibling elements to catch identity vs equals bug
    @Test
    public void testElementSiblingIndex_identicalSiblings_returnsCorrectIndex() {
        Document doc = Jsoup.parse("<div><p>same</p><p>same</p><p>same</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);
        Element p2 = doc.select("p").get(2);

        assertEquals(Integer.valueOf(0), p0.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), p1.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), p2.elementSiblingIndex());
    }

    // Tests nextElementSibling with identical sibling elements
    @Test
    public void testNextElementSibling_identicalSiblings_returnsNextElement() {
        Document doc = Jsoup.parse("<div><p>same</p><p>same</p><p>same</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);
        Element p2 = doc.select("p").get(2);

        assertSame(p1, p0.nextElementSibling());
        assertSame(p2, p1.nextElementSibling());
        assertNull(p2.nextElementSibling());
    }

    // Tests previousElementSibling with identical sibling elements
    @Test
    public void testPreviousElementSibling_identicalSiblings_returnsPreviousElement() {
        Document doc = Jsoup.parse("<div><p>same</p><p>same</p><p>same</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);
        Element p2 = doc.select("p").get(2);

        assertNull(p0.previousElementSibling());
        assertSame(p0, p1.previousElementSibling());
        assertSame(p1, p2.previousElementSibling());
    }

    // Tests firstElementSibling and lastElementSibling
    @Test
    public void testFirstAndLastElementSibling_multipleSiblings_returnsFirstAndLast() {
        Document doc = Jsoup.parse("<div><span>First</span><span>Middle</span><span>Last</span></div>");
        Element middle = doc.select("span").get(1);

        assertEquals("First", middle.firstElementSibling().text());
        assertEquals("Last", middle.lastElementSibling().text());
    }

    // Tests siblingElements when element has no parent or no other siblings
    @Test
    public void testSiblingElements_standaloneOrSingle_returnsEmptyOrOtherSiblings() {
        Element standalone = new Element(Tag.valueOf("p"), "");
        assertTrue(standalone.siblingElements().isEmpty());
        assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());
        assertNull(standalone.nextElementSibling());
        assertNull(standalone.previousElementSibling());

        Document doc = Jsoup.parse("<div><h1>Heading</h1><p>Paragraph</p></div>");
        Element h1 = doc.select("h1").first();
        Elements siblings = h1.siblingElements();
        assertEquals(1, siblings.size());
        assertEquals("p", siblings.get(0).tagName());
    }

    // Tests constructor validation with null tag throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTag_throwsException() {
        new Element(null, "http://example.com");
    }

    // Tests tagName modification
    @Test
    public void testTagName_validName_updatesTagName() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.tagName("div");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
    }

    // Tests tagName validation with empty string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyString_throwsException() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.tagName("");
    }

    // Tests id and dataset handling
    @Test
    public void testIdAndDataset_validAttributes_returnsValues() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "main-content");
        el.attr("data-role", "admin");
        el.attr("data-user-id", "42");

        assertEquals("main-content", el.id());
        assertEquals(2, el.dataset().size());
        assertEquals("admin", el.dataset().get("role"));
        assertEquals("42", el.dataset().get("user-id"));
    }

    // Tests parents traversal up to document root
    @Test
    public void testParents_nestedHierarchy_returnsAncestorChain() {
        Document doc = Jsoup.parse("<div id='grand'><div id='parent'><span id='child'>text</span></div></div>");
        Element child = doc.getElementById("child");
        Elements parents = child.parents();

        assertEquals(4, parents.size()); // div#parent, div#grand, body, html
        assertEquals("parent", parents.get(0).id());
        assertEquals("grand", parents.get(1).id());
    }

    // Tests child, children, textNodes, dataNodes access
    @Test
    public void testChildFiltering_mixedChildren_filtersCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append("Text1 <span>Span</span> Text2");
        DataNode data = new DataNode("var x = 1;", "");
        el.appendChild(data);

        assertEquals(1, el.children().size());
        assertEquals("span", el.child(0).tagName());
        assertEquals(2, el.textNodes().size());
        assertEquals(1, el.dataNodes().size());
        assertEquals("var x = 1;", el.data());
    }

    // Tests insertChildren at valid index and negative wrap-around index
    @Test
    public void testInsertChildren_validAndNegativeIndex_insertsAtExpectedPosition() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>One</p><p>Three</p>");

        Element two = new Element(Tag.valueOf("p"), "").text("Two");
        div.insertChildren(1, Collections.singletonList(two));
        assertEquals("One", div.child(0).text());
        assertEquals("Two", div.child(1).text());
        assertEquals("Three", div.child(2).text());

        Element four = new Element(Tag.valueOf("p"), "").text("Four");
        div.insertChildren(-1, Collections.singletonList(four));
        assertEquals("Four", div.child(3).text());
    }

    // Tests append, prepend, appendText, prependText
    @Test
    public void testAppendAndPrependTextAndElements_validInputs_modifiesChildren() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("span").text("Middle");
        div.prependElement("b").text("Start");
        div.appendElement("i").text("End");

        assertEquals("<b>Start</b><span>Middle</span><i>End</i>", div.html());

        div.empty();
        div.appendText("Middle");
        div.prependText("Start ");
        div.appendText(" End");
        assertEquals("Start Middle End", div.text());
    }

    // Tests cssSelector generation for ID, class, and nth-child selectors
    @Test
    public void testCssSelector_withAndWithoutId_generatesCorrectSelector() {
        Document doc = Jsoup.parse("<div id='main'><div class='item'>1</div><div class='item'>2</div></div>");
        Element item1 = doc.select(".item").get(0);
        Element item2 = doc.select(".item").get(1);

        assertEquals("#main", doc.getElementById("main").cssSelector());
        assertEquals("#main > div.item:nth-child(1)", item1.cssSelector());
        assertEquals("#main > div.item:nth-child(2)", item2.cssSelector());
    }

    // Tests DOM query methods by tag, id, class, and attributes
    @Test
    public void testDomQueries_variousEvaluators_findsMatchingElements() {
        Document doc = Jsoup.parse("<div id='root'><p class='c1' title='greeting'>Hello</p><p class='c2' title='farewell'>Bye</p></div>");
        Element root = doc.getElementById("root");

        assertNotNull(root.getElementById("root"));
        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(1, root.getElementsByClass("c1").size());
        assertEquals(2, root.getElementsByAttribute("title").size());
        assertEquals(1, root.getElementsByAttributeValue("title", "greeting").size());
        assertEquals(1, root.getElementsByAttributeValueNot("title", "greeting").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("title", "greet").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("title", "well").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("title", "e").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("title", Pattern.compile("^greet.*")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("title", "^fare.*").size());
        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(0).size());
        assertEquals(1, root.getElementsByIndexEquals(0).size());
        assertEquals(1, root.getElementsContainingText("Hello").size());
        assertEquals(1, root.getElementsContainingOwnText("Bye").size());
        assertEquals(1, root.getElementsMatchingText("^Hello$").size());
        assertEquals(1, root.getElementsMatchingOwnText("^Bye$").size());
        assertEquals(3, root.getAllElements().size());
    }

    // Tests text(), ownText(), hasText()
    @Test
    public void testTextAndOwnText_mixedContent_returnsCorrectText() {
        Document doc = Jsoup.parse("<p>Hello <b>bold</b> world<br>new line</p>");
        Element p = doc.select("p").first();

        assertEquals("Hello bold world new line", p.text());
        assertEquals("Hello world", p.ownText());
        assertTrue(p.hasText());

        Element empty = new Element(Tag.valueOf("div"), "");
        assertFalse(empty.hasText());
        empty.text("new text");
        assertEquals("new text", empty.text());
    }

    // Tests class attribute manipulations: hasClass, addClass, removeClass, toggleClass, classNames
    @Test
    public void testClassManipulations_addRemoveToggle_updatesClassAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertFalse(el.hasClass("active"));

        el.addClass("active");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("ACTIVE")); // case insensitive

        el.addClass("highlight");
        Set<String> expectedClasses = new HashSet<String>();
        expectedClasses.add("active");
        expectedClasses.add("highlight");
        assertEquals(expectedClasses, el.classNames());

        el.removeClass("active");
        assertFalse(el.hasClass("active"));
        assertTrue(el.hasClass("highlight"));

        el.toggleClass("visible");
        assertTrue(el.hasClass("visible"));
        el.toggleClass("visible");
        assertFalse(el.hasClass("visible"));
    }

    // Tests val() and val(value) on standard element and textarea
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValue() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-value");
        assertEquals("test-value", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("content in textarea");
        assertEquals("content in textarea", textarea.val());
    }

    // Tests html(), outerHtml(), toString(), equals(), hashCode(), clone()
    @Test
    public void testHtmlAndCloneAndEquals_validElement_matchesBehavior() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<span>Hello</span>");
        assertEquals("<span>Hello</span>", div.html());
        assertEquals("<div>\n <span>Hello</span>\n</div>", div.outerHtml());
        assertEquals(div.outerHtml(), div.toString());

        Element clone = div.clone();
        assertEquals(div.outerHtml(), clone.outerHtml());
        assertTrue(div.equals(clone));
        assertEquals(div.hashCode(), clone.hashCode());
    }

    // Tests before and after DOM insertions
    @Test
    public void testBeforeAndAfter_siblingInsertions_placesNodesCorrectly() {
        Document doc = Jsoup.parse("<div><p id='target'>Target</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span id='before'>Before</span>");
        target.after("<span id='after'>After</span>");

        Element parent = doc.select("div").first();
        assertEquals(3, parent.children().size());
        assertEquals("before", parent.child(0).id());
        assertEquals("target", parent.child(1).id());
        assertEquals("after", parent.child(2).id());
    }
}