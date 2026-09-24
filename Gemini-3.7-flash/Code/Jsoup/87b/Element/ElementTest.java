package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests normal case for element construction and basic properties
    @Test
    public void testElementConstructor_validTag_initializesCorrectly() {
        Element el = new Element("div");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());
        assertEquals(0, el.childNodeSize());
        assertEquals("", el.baseUri());
        assertFalse(el.hasText());
    }

    // Tests tagName change preserves/updates correctly
    @Test
    public void testTagName_changeTag_updatesSuccessfully() {
        Element el = new Element("span");
        assertEquals("span", el.tagName());
        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    // Tests empty tagName throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyTag_throwsException() {
        Element el = new Element("div");
        el.tagName("");
    }

    // Tests attribute setting and manipulation
    @Test
    public void testAttr_stringAndBoolean_setsAttributesCorrectly() {
        Element el = new Element("a");
        el.attr("href", "http://example.com");
        el.attr("download", true);

        assertTrue(el.hasAttr("href"));
        assertEquals("http://example.com", el.attr("href"));
        assertTrue(el.hasAttr("download"));

        el.attr("download", false);
        assertFalse(el.hasAttr("download"));
    }

    // Tests id retrieval and cssSelector generation
    @Test
    public void testIdAndCssSelector_withIdAndClasses_returnsCorrectSelector() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "main");
        assertEquals("main", el.id());
        assertEquals("#main", el.cssSelector());

        Element parent = new Element("div");
        Element child = parent.appendElement("span").addClass("highlight");
        assertEquals("div > span.highlight", child.cssSelector());
    }

    // Tests class name manipulation methods
    @Test
    public void testClassNames_manipulations_reflectsAccurately() {
        Element el = new Element("div");
        el.addClass("one");
        el.addClass("two");

        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("two"));
        assertTrue(el.hasClass("ONE")); // case-insensitive

        Set<String> classes = el.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("one"));
        assertTrue(classes.contains("two"));

        el.removeClass("one");
        assertFalse(el.hasClass("one"));
        assertTrue(el.hasClass("two"));

        el.toggleClass("two");
        assertFalse(el.hasClass("two"));

        el.toggleClass("three");
        assertTrue(el.hasClass("three"));

        Set<String> newClasses = new HashSet<>(Arrays.asList("alpha", "beta"));
        el.classNames(newClasses);
        assertTrue(el.hasClass("alpha"));
        assertTrue(el.hasClass("beta"));
        assertFalse(el.hasClass("three"));
    }

    // Tests appendChild, prependChild, and child indexing
    @Test
    public void testAppendAndPrependChild_hierarchy_structuredCorrectly() {
        Element root = new Element("div");
        Element p1 = new Element("p").text("First");
        Element p2 = new Element("p").text("Second");

        root.appendChild(p1);
        root.prependChild(p2);

        assertEquals(2, root.children().size());
        assertEquals(p2, root.child(0));
        assertEquals(p1, root.child(1));
        assertEquals(root, p1.parent());
        assertEquals(root, p2.parent());
    }

    // Tests insertChildren with collection and negative indexing boundary
    @Test
    public void testInsertChildren_validAndNegativeIndex_insertsAtCorrectPositions() {
        Element root = new Element("div");
        Element p1 = new Element("p").text("1");
        Element p2 = new Element("p").text("2");
        Element p3 = new Element("p").text("3");

        root.appendChild(p1);
        root.appendChild(p3);

        root.insertChildren(1, Arrays.asList(p2));
        assertEquals(3, root.children().size());
        assertEquals("2", root.child(1).text());

        Element p4 = new Element("p").text("4");
        root.insertChildren(-1, p4);
        assertEquals("4", root.child(3).text());
    }

    // Tests sibling element navigation methods
    @Test
    public void testSiblingNavigation_variousSiblings_navigatesCorrectly() {
        Element parent = new Element("div");
        Element c1 = parent.appendElement("span").text("1");
        Element c2 = parent.appendElement("span").text("2");
        Element c3 = parent.appendElement("span").text("3");

        assertEquals(c2, c1.nextElementSibling());
        assertNull(c3.nextElementSibling());

        assertEquals(c2, c3.previousElementSibling());
        assertNull(c1.previousElementSibling());

        assertEquals(c1, c2.firstElementSibling());
        assertEquals(c3, c2.lastElementSibling());

        assertEquals(0, c1.elementSiblingIndex());
        assertEquals(1, c2.elementSiblingIndex());
        assertEquals(2, c3.elementSiblingIndex());

        Elements siblings = c2.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(c1));
        assertTrue(siblings.contains(c3));
        assertFalse(siblings.contains(c2));
    }

    // Tests getElementsBy* query methods
    @Test
    public void testGetElementsByQueries_matchingCriteria_returnsExpectedElements() {
        Element root = new Element("div");
        Element p1 = root.appendElement("p").attr("class", "msg").attr("data-id", "101").text("Hello World");
        Element p2 = root.appendElement("p").attr("class", "alert").attr("data-id", "202").text("Alert text");

        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(1, root.getElementsByClass("msg").size());
        assertEquals(p1, root.getElementsByClass("msg").first());

        assertEquals(2, root.getElementsByAttribute("data-id").size());
        assertEquals(2, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("data-id", "101").size());
        assertEquals(1, root.getElementsByAttributeValueNot("data-id", "101").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("data-id", "10").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("data-id", "02").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("data-id", "0").size());
        assertEquals(2, root.getElementsByAttributeValueMatching("data-id", Pattern.compile("\\d+")).size());
        assertEquals(2, root.getElementsByAttributeValueMatching("data-id", "\\d+").size());

        assertEquals(1, root.getElementsByIndexEquals(0).size());
        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(0).size());

        assertEquals(1, root.getElementsContainingText("Hello").size());
        assertEquals(1, root.getElementsContainingOwnText("Hello").size());
        assertEquals(1, root.getElementsMatchingText(Pattern.compile("Hello.*")).size());
        assertEquals(1, root.getElementsMatchingText("Hello.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("Alert.*")).size());
        assertEquals(1, root.getElementsMatchingOwnText("Alert.*").size());
    }

    // Tests text(), ownText(), and wholeText() handling
    @Test
    public void testTextExtraction_nestedStructure_extractsProperText() {
        Element p = new Element("p");
        p.appendText("Hello ");
        p.appendElement("b").text("bold");
        p.appendText(" world!");

        assertEquals("Hello bold world!", p.text());
        assertEquals("Hello world!", p.ownText());
        assertEquals("Hello bold world!", p.wholeText());
        assertTrue(p.hasText());

        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals(" world!", textNodes.get(1).getWholeText());

        p.text("New text");
        assertEquals("New text", p.text());
        assertEquals(1, p.childNodes.size());
    }

    // Tests data() extraction on script and data nodes
    @Test
    public void testData_scriptAndDataNode_extractsDataCorrectly() {
        Element script = new Element("script");
        script.appendChild(new DataNode("var x = 1;", ""));
        assertEquals("var x = 1;", script.data());
        assertEquals(1, script.dataNodes().size());
    }

    // Tests val() on input and textarea
    @Test
    public void testVal_inputAndTextarea_getsAndSetsCorrectly() {
        Element input = new Element("input").attr("value", "username");
        assertEquals("username", input.val());
        input.val("admin");
        assertEquals("admin", input.attr("value"));

        Element textarea = new Element("textarea").text("sample content");
        assertEquals("sample content", textarea.val());
        textarea.val("updated content");
        assertEquals("updated content", textarea.text());
    }

    // Tests outerHtml, inner html, and empty()
    @Test
    public void testHtmlAndEmpty_manipulation_rendersAndClearsExpectedHtml() {
        Element div = new Element("div");
        div.html("<p>Hello</p>");

        assertEquals("<p>Hello</p>", div.html());
        assertEquals("<div>\n <p>Hello</p>\n</div>", div.outerHtml());

        div.empty();
        assertEquals(0, div.childNodeSize());
        assertEquals("", div.html());
    }

    // Tests cloning behavior (deep clone and shallow clone)
    @Test
    public void testCloneAndShallowClone_elementWithChildren_clonesAccurately() {
        Element parent = new Element("div");
        parent.attr("id", "main");
        parent.appendElement("span").text("Child");

        Element deepClone = parent.clone();
        assertEquals(parent.outerHtml(), deepClone.outerHtml());
        assertNotSame(parent, deepClone);
        assertNotSame(parent.child(0), deepClone.child(0));

        Element shallow = parent.shallowClone();
        assertEquals("main", shallow.id());
        assertEquals(0, shallow.childNodeSize());
    }

    // Tests preserveCase setting via html parsing and getElementsByTag case preservation/normalization
    @Test
    public void testPreserveCase_htmlAndXmlSyntax_preservesOrNormalizesCorrectly() {
        Document docHtml = Jsoup.parse("<HTML><DIV ID=1>Test</DIV></HTML>", "", org.jsoup.parser.Parser.htmlParser());
        Elements htmlDivs = docHtml.getElementsByTag("div");
        assertEquals(1, htmlDivs.size());
        assertEquals("div", htmlDivs.get(0).tagName());

        Document docXml = Jsoup.parse("<HTML><DIV ID=1>Test</DIV></HTML>", "", org.jsoup.parser.Parser.xmlParser());
        Elements xmlDivs = docXml.getElementsByTag("DIV");
        assertEquals(1, xmlDivs.size());
        assertEquals("DIV", xmlDivs.get(0).tagName());
    }
}