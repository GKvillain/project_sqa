package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    private Element docElement;

    @Before
    public void setUp() {
        docElement = new Element("div");
        docElement.attr("id", "root");
    }

    // Tests tag name initialization, tag retrieval, and tag renaming
    @Test
    public void testTagName_validName_updatesTagCorrectly() {
        Element el = new Element("p");
        assertEquals("p", el.tagName());
        assertEquals("p", el.nodeName());
        assertFalse(el.isBlock());

        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    // Tests empty tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyName_throwsException() {
        Element el = new Element("span");
        el.tagName("");
    }

    // Tests constructor with null tag throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTag_throwsException() {
        new Element((Tag) null, "");
    }

    // Tests attribute get, set, boolean attribute and dataset
    @Test
    public void testAttr_setAndGet_returnsCorrectValues() {
        Element el = new Element("input");
        el.attr("type", "text");
        el.attr("data-custom", "value123");
        el.attr("disabled", true);

        assertEquals("text", el.attr("type"));
        assertTrue(el.hasAttr("disabled"));
        
        Map<String, String> dataset = el.dataset();
        assertEquals(1, dataset.size());
        assertEquals("value123", dataset.get("custom"));

        el.attr("disabled", false);
        assertFalse(el.hasAttr("disabled"));
    }

    // Tests class name manipulation: hasClass, addClass, removeClass, toggleClass
    @Test
    public void testClassNames_manipulateClasses_updatesClassesCorrectly() {
        Element el = new Element("div");
        el.attr("class", "btn active");

        assertTrue(el.hasClass("btn"));
        assertTrue(el.hasClass("ACTIVE"));
        assertFalse(el.hasClass("hidden"));

        el.addClass("btn-primary");
        assertTrue(el.hasClass("btn-primary"));

        el.removeClass("active");
        assertFalse(el.hasClass("active"));

        el.toggleClass("toggled");
        assertTrue(el.hasClass("toggled"));
        el.toggleClass("toggled");
        assertFalse(el.hasClass("toggled"));

        Set<String> customClasses = new HashSet<>(Arrays.asList("one", "two"));
        el.classNames(customClasses);
        assertEquals("one two", el.className());
    }

    // Tests child and sibling navigation methods
    @Test
    public void testSiblingsAndChildren_navigatingTree_returnsExpectedElements() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("p").attr("id", "p1");
        Element child2 = parent.appendElement("span").attr("id", "s2");
        Element child3 = parent.appendElement("p").attr("id", "p3");

        assertEquals(3, parent.children().size());
        assertEquals(child1, parent.child(0));
        assertEquals(child2, parent.child(1));
        assertEquals(child3, parent.child(2));

        assertEquals(child1, parent.firstElementSibling());
        assertEquals(child3, parent.lastElementSibling());

        assertEquals(child2, child1.nextElementSibling());
        assertNull(child1.previousElementSibling());
        assertEquals(child1, child2.previousElementSibling());
        assertEquals(child3, child2.nextElementSibling());
        assertNull(child3.nextElementSibling());

        assertEquals(0, child1.elementSiblingIndex());
        assertEquals(1, child2.elementSiblingIndex());
        assertEquals(2, child3.elementSiblingIndex());

        Elements siblingsOf2 = child2.siblingElements();
        assertEquals(2, siblingsOf2.size());
        assertTrue(siblingsOf2.contains(child1));
        assertTrue(siblingsOf2.contains(child3));
    }

    // Tests parent and parents hierarchy retrieval
    @Test
    public void testParents_nestedElements_returnsAncestorStack() {
        Element grandParent = new Element("div").attr("id", "gp");
        Element parent = grandParent.appendElement("section").attr("id", "p");
        Element child = parent.appendElement("span").attr("id", "c");

        assertEquals(parent, child.parent());
        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertEquals(parent, parents.get(0));
        assertEquals(grandParent, parents.get(1));
    }

    // Tests text() and ownText() normalization and separation
    @Test
    public void testText_nestedTextAndElements_returnsNormalizedText() {
        Element p = new Element("p");
        p.appendText("Hello ");
        p.appendElement("b").text("world");
        p.appendText(" !");

        assertEquals("Hello world !", p.text());
        assertEquals("Hello !", p.ownText());
        assertTrue(p.hasText());

        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
    }

    // Tests element cloning behavior including child nodes and sibling traversal
    @Test
    public void testClone_clonedTree_maintainsStructureAndIndependence() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("span").text("First");
        Element child2 = parent.appendElement("span").text("Second");

        Element clonedParent = parent.clone();
        assertNotSame(parent, clonedParent);
        assertEquals(2, clonedParent.children().size());

        Element clonedChild1 = clonedParent.child(0);
        Element clonedChild2 = clonedParent.child(1);

        assertNotSame(child1, clonedChild1);
        assertNotSame(child2, clonedChild2);
        assertEquals(clonedParent, clonedChild1.parent());
        assertEquals("First", clonedChild1.text());

        assertEquals(clonedChild2, clonedChild1.nextElementSibling());
        assertEquals(clonedChild1, clonedChild2.previousElementSibling());

        clonedChild1.text("Modified");
        assertEquals("Modified", clonedChild1.text());
        assertEquals("First", child1.text());
    }

    // Tests DOM query methods like getElementsByTag, getElementById, getElementsByClass
    @Test
    public void testGetElements_variousQueries_findsMatchingElements() {
        Element root = new Element("div");
        Element p1 = root.appendElement("p").attr("id", "first").addClass("intro active");
        Element p2 = root.appendElement("p").attr("id", "second").addClass("body");
        Element span = p2.appendElement("span").attr("data-type", "label");

        assertEquals(p1, root.getElementById("first"));
        assertNull(root.getElementById("non-existent"));

        Elements pTags = root.getElementsByTag("p");
        assertEquals(2, pTags.size());

        Elements intros = root.getElementsByClass("intro");
        assertEquals(1, intros.size());
        assertEquals(p1, intros.get(0));

        Elements byAttr = root.getElementsByAttribute("data-type");
        assertEquals(1, byAttr.size());
        assertEquals(span, byAttr.get(0));

        Elements byAttrPrefix = root.getElementsByAttributeStarting("data-");
        assertEquals(1, byAttrPrefix.size());

        Elements byAttrVal = root.getElementsByAttributeValue("id", "first");
        assertEquals(1, byAttrVal.size());
        assertEquals(p1, byAttrVal.get(0));
    }

    // Tests select, selectFirst and is query methods
    @Test
    public void testSelect_cssSelectors_matchesCorrectElements() {
        Element root = new Element("div");
        root.appendElement("p").attr("id", "p1").text("One");
        root.appendElement("p").attr("id", "p2").text("Two");

        Elements selected = root.select("p");
        assertEquals(2, selected.size());

        Element firstP = root.selectFirst("p");
        assertNotNull(firstP);
        assertEquals("p1", firstP.id());

        assertTrue(firstP.is("p"));
        assertTrue(firstP.is("#p1"));
        assertFalse(firstP.is("#p2"));
    }

    // Tests append, prepend, insertChildren and empty methods
    @Test
    public void testHtmlManipulation_appendPrependInsertEmpty_modifiesChildren() {
        Element div = new Element("div");
        div.append("<p>Paragraph</p>");
        assertEquals(1, div.children().size());
        assertEquals("Paragraph", div.child(0).text());

        div.prepend("<h1>Title</h1>");
        assertEquals(2, div.children().size());
        assertEquals("h1", div.child(0).tagName());

        Element span = new Element("span").text("Inserted");
        div.insertChildren(1, span);
        assertEquals(3, div.children().size());
        assertEquals("span", div.child(1).tagName());

        div.empty();
        assertEquals(0, div.childNodeSize());
        assertEquals(0, div.children().size());
    }

    // Tests cssSelector generation for root and nested elements
    @Test
    public void testCssSelector_variousElements_generatesExpectedSelector() {
        Element div = new Element("div").attr("id", "main");
        Element p = div.appendElement("p").addClass("article").addClass("lead");
        Element span = p.appendElement("span");

        assertEquals("#main", div.cssSelector());
        assertEquals("#main > p.article.lead", p.cssSelector());
        assertEquals("#main > p.article.lead > span", span.cssSelector());
    }

    // Tests val() getter and setter for normal and textarea elements
    @Test
    public void testVal_inputAndTextarea_readsAndSetsValue() {
        Element input = new Element("input").attr("value", "initial");
        assertEquals("initial", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));

        Element textarea = new Element("textarea");
        textarea.text("comment content");
        assertEquals("comment content", textarea.val());
        textarea.val("new content");
        assertEquals("new content", textarea.text());
    }

    // Tests text matching filters using regex
    @Test
    public void testGetElementsMatchingText_regex_findsMatches() {
        Element div = new Element("div");
        div.appendElement("p").text("Order 1234");
        div.appendElement("p").text("Order 5678");
        div.appendElement("span").text("User profile");

        Elements orderElements = div.getElementsMatchingText(Pattern.compile("Order \\d+"));
        assertEquals(2, orderElements.size());

        Elements containing = div.getElementsContainingText("1234");
        assertEquals(1, containing.size());
    }

    // Tests outerHtml formatting for block and self-closing tags
    @Test
    public void testOuterHtml_formatting_producesExpectedHtml() {
        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\">", img.outerHtml());

        Element div = new Element("div");
        div.attr("id", "box");
        div.appendElement("span").text("text");
        assertEquals("<div id=\"box\">\n <span>text</span>\n</div>", div.outerHtml());
    }
}