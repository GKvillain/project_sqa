package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeVisitor;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    private Element element;

    @Before
    public void setUp() {
        element = new Element("div");
    }

    // Tests element construction, tag name, id, and attributes handling
    @Test
    public void testConstructAndAttributes_validData_returnsExpectedValues() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        assertEquals("p", el.tagName());
        assertEquals("http://example.com", el.baseUri());
        assertFalse(el.hasAttributes());

        el.attr("id", "main");
        el.attr("data-test", "val");
        el.attr("disabled", true);

        assertEquals("main", el.id());
        assertTrue(el.hasAttributes());
        assertEquals("val", el.attr("data-test"));

        Map<String, String> dataset = el.dataset();
        assertEquals(1, dataset.size());
        assertEquals("val", dataset.get("test"));

        el.attr("disabled", false);
        assertFalse(el.hasAttr("disabled"));
    }

    // Tests changing tag name and validation
    @Test
    public void testTagName_changeTag_updatesTag() {
        Element el = new Element("span");
        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    // Tests exception on empty tag name
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyName_throwsException() {
        element.tagName("");
    }

    // Tests parent and ancestor hierarchy traversal
    @Test
    public void testParents_nestedElements_returnsCorrectHierarchy() {
        Document doc = Jsoup.parse("<html><body><div><p><span>Text</span></p></div></body></html>");
        Element span = doc.selectFirst("span");
        assertNotNull(span);

        Elements parents = span.parents();
        assertEquals(3, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
        assertEquals("body", parents.get(2).tagName());
    }

    // Tests child elements and child nodes filtering
    @Test
    public void testChildrenAndTextNodes_mixedContent_filtersCorrectly() {
        Document doc = Jsoup.parse("<p>One <span>Two</span> Three <br> Four</p>");
        Element p = doc.selectFirst("p");
        assertNotNull(p);

        assertEquals(2, p.children().size());
        assertEquals("span", p.child(0).tagName());
        assertEquals("br", p.child(1).tagName());

        List<TextNode> textNodes = p.textNodes();
        assertEquals(3, textNodes.size());
        assertEquals("One ", textNodes.get(0).getWholeText());

        assertEquals(0, p.dataNodes().size());
    }

    // Tests inserting and appending child nodes
    @Test
    public void testChildMutations_insertAndAppend_ordersChildrenCorrectly() {
        element.appendElement("span").text("Middle");
        element.prependElement("header").text("Top");
        element.appendElement("footer").text("Bottom");

        assertEquals(3, element.children().size());
        assertEquals("header", element.child(0).tagName());
        assertEquals("span", element.child(1).tagName());
        assertEquals("footer", element.child(2).tagName());

        Element newChild = new Element("nav");
        element.insertChildren(1, newChild);
        assertEquals("nav", element.child(1).tagName());
        assertEquals(4, element.children().size());

        Element appended = new Element("aside");
        appended.appendTo(element);
        assertEquals("aside", element.child(4).tagName());
    }

    // Tests insertChildren out of bounds exception
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_indexOutOfBounds_throwsException() {
        element.insertChildren(5, new Element("span"));
    }

    // Tests sibling navigation methods
    @Test
    public void testSiblingNavigation_multipleSiblings_navigatesProperly() {
        Document doc = Jsoup.parse("<ol><li id='1'>1</li><li id='2'>2</li><li id='3'>3</li></ol>");
        Element li2 = doc.getElementById("2");
        assertNotNull(li2);

        assertEquals(1, li2.elementSiblingIndex());
        assertEquals("1", li2.previousElementSibling().id());
        assertEquals("3", li2.nextElementSibling().id());

        assertEquals(2, li2.siblingElements().size());
        assertEquals("1", li2.firstElementSibling().id());
        assertEquals("3", li2.lastElementSibling().id());

        assertEquals(1, li2.previousElementSiblings().size());
        assertEquals(1, li2.nextElementSiblings().size());
    }

    // Tests DOM selection and query methods
    @Test
    public void testDomQueryMethods_variousQueries_findsElements() {
        Document doc = Jsoup.parse("<div class='content' id='root'><p class='intro' data-val='1'>Hello</p><p class='body'>World</p></div>");
        Element root = doc.getElementById("root");
        assertNotNull(root);

        assertTrue(root.is("div#root"));
        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(1, root.getElementsByClass("intro").size());
        assertEquals(1, root.getElementsByAttribute("data-val").size());
        assertEquals(1, root.getElementsByAttributeValue("data-val", "1").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("class", "tro").size());
        assertEquals(1, root.getElementsByAttributeValueContaining("class", "ntr").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("class", Pattern.compile("^int.*")).size());
        assertEquals(1, root.getElementsByIndexEquals(0).size());
        assertEquals(1, root.getElementsContainingText("Hello").size());
        assertEquals(1, root.getElementsContainingOwnText("World").size());
        assertEquals(1, root.getElementsMatchingText(Pattern.compile("Hello")).size());
        assertEquals(3, root.getAllElements().size()); // div, p.intro, p.body
    }

    // Tests text extraction: text(), wholeText(), and ownText()
    @Test
    public void testTextExtraction_nestedTags_extractsCorrectly() {
        Document doc = Jsoup.parse("<p>Hello  <b>there</b> now! <br>Line 2</p>");
        Element p = doc.selectFirst("p");
        assertNotNull(p);

        assertEquals("Hello there now! Line 2", p.text());
        assertEquals("Hello now! Line 2", p.ownText());
        assertTrue(p.hasText());

        Element empty = new Element("div");
        assertFalse(empty.hasText());
        assertEquals("", empty.text());

        empty.text("New text");
        assertEquals("New text", empty.text());
    }

    // Tests data element extraction (e.g. script/style tags)
    @Test
    public void testData_scriptTag_extractsRawData() {
        Document doc = Jsoup.parse("<script>var x = 1 < 2;</script>");
        Element script = doc.selectFirst("script");
        assertNotNull(script);
        assertEquals("var x = 1 < 2;", script.data());
    }

    // Tests class manipulation methods: classNames, addClass, removeClass, toggleClass, hasClass
    @Test
    public void testClassManipulation_variousOperations_modifiesClassAttribute() {
        element.addClass("header");
        assertTrue(element.hasClass("header"));
        assertTrue(element.hasClass("HEADER"));
        assertEquals("header", element.className());

        element.addClass("highlight");
        assertTrue(element.hasClass("header"));
        assertTrue(element.hasClass("highlight"));

        element.removeClass("header");
        assertFalse(element.hasClass("header"));
        assertTrue(element.hasClass("highlight"));

        element.toggleClass("highlight");
        assertFalse(element.hasClass("highlight"));

        element.toggleClass("active");
        assertTrue(element.hasClass("active"));

        Set<String> customClasses = new HashSet<>(Arrays.asList("a", "b"));
        element.classNames(customClasses);
        assertEquals(2, element.classNames().size());
        assertTrue(element.hasClass("a"));
        assertTrue(element.hasClass("b"));

        element.classNames(new HashSet<String>());
        assertEquals("", element.className());
    }

    // Tests form element value handling for input and textarea
    @Test
    public void testVal_inputAndTextarea_readsAndSetsValues() {
        Element input = new Element("input");
        input.val("test-value");
        assertEquals("test-value", input.val());
        assertEquals("test-value", input.attr("value"));

        Element textarea = new Element("textarea");
        textarea.val("multi\nline");
        assertEquals("multi\nline", textarea.val());
        assertEquals("multi\nline", textarea.text());
    }

    // Tests CSS selector path generation
    @Test
    public void testCssSelector_withAndWithoutId_generatesValidSelector() {
        Document doc = Jsoup.parse("<div id='main'><div class='sub'><span>First</span><span>Second</span></div></div>");
        Element main = doc.getElementById("main");
        assertNotNull(main);
        assertEquals("#main", main.cssSelector());

        Element secondSpan = doc.select("span").get(1);
        assertEquals("#main > div.sub > span:nth-child(2)", secondSpan.cssSelector());
    }

    // Tests html manipulation: inner html, outer html, append, prepend, wrap, empty
    @Test
    public void testHtmlManipulation_appendWrapAndEmpty_modifiesStructure() {
        element.html("<p>Initial</p>");
        assertEquals("<p>Initial</p>", element.html());

        element.append("<span>Appended</span>");
        element.prepend("<b>Prepended</b>");
        assertEquals(3, element.children().size());
        assertEquals("b", element.child(0).tagName());
        assertEquals("span", element.child(2).tagName());

        Element p = element.selectFirst("p");
        assertNotNull(p);
        p.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", p.parent().className());

        element.empty();
        assertEquals(0, element.childNodeSize());
        assertEquals("", element.html());
    }

    // Tests deep clone and shallow clone functionality
    @Test
    public void testCloneAndShallowClone_clonesCorrectly() {
        element.attr("id", "orig");
        element.appendElement("span").text("Child");

        Element clone = element.clone();
        assertEquals("orig", clone.id());
        assertEquals(1, clone.children().size());
        assertEquals("Child", clone.child(0).text());

        // Modifying clone does not affect original
        clone.attr("id", "modified");
        clone.child(0).text("NewChild");
        assertEquals("orig", element.id());
        assertEquals("Child", element.child(0).text());

        Element shallow = element.shallowClone();
        assertEquals("orig", shallow.id());
        assertEquals(0, shallow.childNodeSize());
    }

    // Tests invalid regex pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatching_invalidRegex_throwsException() {
        element.getElementsByAttributeValueMatching("data", "[unclosed");
    }

    // Tests null child in appendChild throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAppendChild_nullChild_throwsException() {
        element.appendChild(null);
    }

    // Tests closest ancestor matching with string and Evaluator
    @Test
    public void testClosest_matchingAncestor_returnsClosestElement() {
        Document doc = Jsoup.parse("<div id='outer' class='wrap'><div id='inner' class='wrap'><p><span>Text</span></p></div></div>");
        Element span = doc.selectFirst("span");
        assertNotNull(span);

        Element closestWrap = span.closest(".wrap");
        assertNotNull(closestWrap);
        assertEquals("inner", closestWrap.id());

        Element closestDiv = span.closest(new Evaluator.Tag("div"));
        assertNotNull(closestDiv);
        assertEquals("inner", closestDiv.id());

        assertNull(span.closest("table"));
        assertEquals(span, span.closest("span"));
    }

    // Tests expectFirst returning matched element or throwing exception when not found
    @Test
    public void testExpectFirst_validAndInvalidQuery_returnsOrThrows() {
        Document doc = Jsoup.parse("<div><p class='target'>Hello</p></div>");
        Element p = doc.expectFirst("p.target");
        assertEquals("Hello", p.text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpectFirst_notFound_throwsException() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        doc.expectFirst("span.missing");
    }

    // Tests root element retrieval
    @Test
    public void testRoot_nestedHierarchy_returnsRootElement() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Element span = doc.selectFirst("span");
        assertNotNull(span);
        assertEquals(doc, span.root());

        Element orphan = new Element("div");
        assertEquals(orphan, orphan.root());
    }

    // Tests additional query methods
    @Test
    public void testAdditionalDomQueries_variousFilters_matchesExpectedElements() {
        Document doc = Jsoup.parse("<div class='container' id='root'><p class='intro' data-val='1'>Hello</p><p class='body' data-val='2'>World</p></div>");
        Element root = doc.getElementById("root");
        assertNotNull(root);

        assertEquals(1, root.getElementsByAttributeValueNot("class", "intro").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("class", "in").size());
        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(0).size());
        assertEquals(1, root.getElementsMatchingText("^Hello.*").size());
        assertEquals(1, root.getElementsMatchingOwnText("^Hello.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("^World.*")).size());
    }

    // Tests appendText, prependText, wholeText and childElementSize
    @Test
    public void testTextAndChildHelpers_manipulation_updatesCorrectly() {
        element.appendText("World");
        element.prependText("Hello ");
        assertEquals("Hello World", element.text());

        assertEquals(0, element.childElementSize());
        element.appendElement("span");
        assertEquals(1, element.childElementSize());

        Document doc = Jsoup.parse("<p> First \n Second <span>Third</span> </p><pre> Keep \n Whitespace </pre>");
        Element p = doc.selectFirst("p");
        assertNotNull(p);
        assertEquals(" First \n Second Third ", p.wholeText());

        Element pre = doc.selectFirst("pre");
        assertNotNull(pre);
        assertEquals(" Keep \n Whitespace ", pre.wholeText());
    }

    // Tests clearAttributes, id setter, and normalName
    @Test
    public void testAttributesAndTagHelpers_modifications_behaveCorrectly() {
        element.attr("id", "origId");
        element.attr("class", "c1");
        element.id("newId");
        assertEquals("newId", element.id());

        element.clearAttributes();
        assertFalse(element.hasAttributes());
        assertEquals("", element.id());

        Element upper = new Element(Tag.valueOf("SPAN"), "");
        assertEquals("span", upper.normalName());
        assertEquals(Tag.valueOf("span"), upper.tag());
    }

    // Tests sibling operations on element without parent
    @Test
    public void testSiblingOperations_orphanElement_returnsSafeDefaults() {
        Element orphan = new Element("div");
        assertNull(orphan.previousElementSibling());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.firstElementSibling());
        assertNull(orphan.lastElementSibling());
        assertEquals(0, orphan.elementSiblingIndex());
        assertTrue(orphan.siblingElements().isEmpty());
        assertTrue(orphan.parents().isEmpty());
    }

    // Tests insertChildren with collection of nodes
    @Test
    public void testInsertChildren_collection_insertsAllNodes() {
        List<Node> nodes = Arrays.asList(new Element("p").text("One"), new TextNode("Two"));
        element.insertChildren(0, nodes);
        assertEquals(2, element.childNodeSize());
        assertEquals("One", element.child(0).text());
        assertEquals(1, element.children().size());

        element.insertChildren(-1, Collections.singletonList(new Element("span").text("Three")));
        assertEquals("span", element.child(1).tagName());
    }

    // Tests element visitor traversal and selector with Evaluator
    @Test
    public void testTraverseAndEvaluator_nestedElements_visitsAllNodes() {
        Document doc = Jsoup.parse("<div><p class='target'><span>Text</span></p></div>");
        final int[] visitCount = {0};
        doc.body().child(0).traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                visitCount[0]++;
            }

            @Override
            public void tail(Node node, int depth) {
            }
        });
        assertTrue(visitCount[0] >= 3);

        Evaluator eval = new Evaluator.Class("target");
        Elements matched = doc.body().select(eval);
        assertEquals(1, matched.size());
        assertTrue(matched.first().is(eval));
    }

    // Tests outerHtml output formatting
    @Test
    public void testOuterHtml_prettyPrintSettings_formatsCorrectly() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        doc.outputSettings().prettyPrint(false);
        Element div = doc.body().child(0);
        assertEquals("<div><p>Hello</p></div>", div.outerHtml());
        assertEquals("<div><p>Hello</p></div>", div.toString());
    }
}