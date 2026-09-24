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
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests that siblingElements excludes the element itself and returns only actual siblings
    @Test
    public void testSiblingElements_hasSiblings_excludesSelf() {
        Document doc = Jsoup.parse("<div><p id='p1'>One</p><p id='p2'>Two</p><p id='p3'>Three</p></div>");
        Element p2 = doc.getElementById("p2");
        Elements siblings = p2.siblingElements();

        assertEquals(2, siblings.size());
        assertEquals("p1", siblings.get(0).id());
        assertEquals("p3", siblings.get(1).id());
        assertFalse(siblings.contains(p2));
    }

    // Tests nextElementSibling and previousElementSibling boundary returns
    @Test
    public void testNextAndPreviousElementSibling_adjacentElements_returnsCorrectSiblings() {
        Document doc = Jsoup.parse("<div><p id='p1'>One</p><p id='p2'>Two</p><p id='p3'>Three</p></div>");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        Element p3 = doc.getElementById("p3");

        assertNull(p1.previousElementSibling());
        assertEquals(p2, p1.nextElementSibling());
        assertEquals(p1, p2.previousElementSibling());
        assertEquals(p3, p2.nextElementSibling());
        assertNull(p3.nextElementSibling());
    }

    // Tests firstElementSibling and lastElementSibling
    @Test
    public void testFirstAndLastElementSibling_multipleSiblings_returnsFirstAndLast() {
        Document doc = Jsoup.parse("<div><p id='p1'>One</p><p id='p2'>Two</p><p id='p3'>Three</p></div>");
        Element p2 = doc.getElementById("p2");

        assertEquals("p1", p2.firstElementSibling().id());
        assertEquals("p3", p2.lastElementSibling().id());

        Element standalone = new Element(Tag.valueOf("div"), "");
        assertNull(standalone.firstElementSibling());
        assertNull(standalone.lastElementSibling());
    }

    // Tests elementSiblingIndex calculation
    @Test
    public void testElementSiblingIndex_variousPositions_returnsCorrectIndex() {
        Document doc = Jsoup.parse("<div><p id='p1'>One</p><p id='p2'>Two</p><p id='p3'>Three</p></div>");
        assertEquals(Integer.valueOf(0), doc.getElementById("p1").elementSiblingIndex());
        assertEquals(Integer.valueOf(1), doc.getElementById("p2").elementSiblingIndex());
        assertEquals(Integer.valueOf(2), doc.getElementById("p3").elementSiblingIndex());

        Element standalone = new Element(Tag.valueOf("div"), "");
        assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());
    }

    // Tests tagName changes and isBlock status
    @Test
    public void testTagNameAndIsBlock_changeTag_updatesTagAndBlockStatus() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertFalse(el.isBlock());
        assertEquals("span", el.tagName());

        el.tagName("div");
        assertTrue(el.isBlock());
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
    }

    // Tests exception on empty tagName
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyName_throwsException() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.tagName("");
    }

    // Tests class name manipulation methods: addClass, removeClass, toggleClass, hasClass
    @Test
    public void testClassNames_manipulation_updatesClassesCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "foo bar");

        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("BAR"));
        assertFalse(el.hasClass("baz"));

        el.addClass("baz");
        assertTrue(el.hasClass("baz"));
        assertEquals("foo bar baz", el.className());

        el.removeClass("bar");
        assertFalse(el.hasClass("bar"));
        assertEquals("foo baz", el.className());

        el.toggleClass("baz");
        assertFalse(el.hasClass("baz"));

        el.toggleClass("qux");
        assertTrue(el.hasClass("qux"));
    }

    // Tests text and ownText with nested mixed children
    @Test
    public void testTextAndOwnText_nestedContent_returnsCombinedAndDirectText() {
        Document doc = Jsoup.parse("<div>Hello <span>beautiful</span> <b>world</b>!</div>");
        Element div = doc.select("div").first();

        assertEquals("Hello beautiful world!", div.text());
        assertEquals("Hello !", div.ownText());
        assertTrue(div.hasText());

        List<TextNode> textNodes = div.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals("!", textNodes.get(1).getWholeText());
    }

    // Tests data and dataNodes extraction
    @Test
    public void testData_scriptContent_returnsDataString() {
        Document doc = Jsoup.parse("<script type='text/javascript'>var x = 10;</script>");
        Element script = doc.select("script").first();

        assertEquals("var x = 10;", script.data());
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 10;", dataNodes.get(0).getWholeData());
    }

    // Tests val method for input and textarea tags
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValues() {
        Element input = new Element(Tag.valueOf("input"), "").attr("value", "initial");
        assertEquals("initial", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("initial text");
        assertEquals("initial text", textarea.val());
        textarea.val("updated text");
        assertEquals("updated text", textarea.text());
    }

    // Tests dataset retrieval of HTML5 data-* attributes
    @Test
    public void testDataset_customDataAttributes_returnsFilteredMap() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-name", "jsoup");
        el.attr("data-version", "1.0");
        el.attr("id", "main");

        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("1.0", dataset.get("version"));
        assertNull(dataset.get("id"));
    }

    // Tests appending and prepending child nodes and elements
    @Test
    public void testAppendAndPrepend_elementsAndText_updatesChildrenOrder() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("Middle");
        div.prependElement("header").text("Top");
        div.appendElement("footer").text("Bottom");

        assertEquals(3, div.children().size());
        assertEquals("header", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
        assertEquals("footer", div.child(2).tagName());

        div.prependText("Start: ");
        div.appendText(" :End");
        assertEquals("Start: Top Middle Bottom :End", div.text());
    }

    // Tests parents accumulation up to document root
    @Test
    public void testParents_nestedHierarchy_returnsAncestorsInOrder() {
        Document doc = Jsoup.parse("<div><section><p><span>Test</span></p></section></div>");
        Element span = doc.select("span").first();
        Elements parents = span.parents();

        assertEquals(4, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("section", parents.get(1).tagName());
        assertEquals("div", parents.get(2).tagName());
        assertEquals("body", parents.get(3).tagName());
    }

    // Tests empty and html getter/setter
    @Test
    public void testHtmlAndEmpty_modifyInnerHtml_updatesContent() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Hello</p><span>World</span>");

        assertEquals(2, div.children().size());
        assertEquals("<p>Hello</p>\n<span>World</span>", div.html());

        div.empty();
        assertEquals(0, div.children().size());
        assertEquals("", div.html());
        assertFalse(div.hasText());
    }

    // Tests various DOM query methods on Element
    @Test
    public void testGetElements_queriesByAttributesAndText_returnsMatches() {
        Document doc = Jsoup.parse("<div id='root'><p class='intro' data-type='a'>First</p><p class='intro' data-type='b'>Second</p><span class='outro'>Third</span></div>");
        Element root = doc.getElementById("root");

        assertEquals("root", root.id());
        assertEquals(2, root.getElementsByClass("intro").size());
        assertEquals(2, root.getElementsByAttribute("data-type").size());
        assertEquals(1, root.getElementsByAttributeValue("data-type", "a").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size() > 0 ? 2 : 0);
        assertEquals(1, root.getElementsContainingText("First").size());
        assertEquals(1, root.getElementsContainingOwnText("Second").size());
        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(1, root.getElementsByIndexEquals(0).size());
    }

    // Tests clone creates independent copy with preserved attributes
    @Test
    public void testClone_elementWithAttributes_createsDeepCopy() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "main");
        div.addClass("active");
        div.appendElement("span").text("Content");

        Element clone = div.clone();

        assertEquals(div.outerHtml(), clone.outerHtml());
        clone.attr("id", "secondary");
        clone.removeClass("active");

        assertEquals("main", div.id());
        assertTrue(div.hasClass("active"));
        assertEquals("secondary", clone.id());
        assertFalse(clone.hasClass("active"));
    }

    // Tests cssSelector generation for elements
    @Test
    public void testCssSelector_variousElements_generatesValidSelector() {
        Document doc = Jsoup.parse("<div id='container'><p class='one'>Text</p><p class='two'>Text 2</p></div>");
        Element p1 = doc.select(".one").first();
        Element p2 = doc.select(".two").first();

        assertEquals("#container > p.one", p1.cssSelector());
        assertEquals("#container > p.two", p2.cssSelector());
        assertEquals("#container", doc.getElementById("container").cssSelector());
    }

    // Tests classNames set getter and setter
    @Test
    public void testClassNames_setOperations_updatesClasses() {
        Element el = new Element(Tag.valueOf("div"), "");
        Set<String> initialClasses = el.classNames();
        assertTrue(initialClasses.isEmpty());

        Set<String> newClasses = new HashSet<String>(Arrays.asList("alpha", "beta", "gamma"));
        el.classNames(newClasses);

        assertEquals(3, el.classNames().size());
        assertTrue(el.hasClass("alpha"));
        assertTrue(el.hasClass("beta"));
        assertTrue(el.hasClass("gamma"));
    }

    // Tests wrap and unwrap structure modifications
    @Test
    public void testWrapAndUnwrap_modifiesStructure() {
        Document doc = Jsoup.parse("<div><p>Inner text</p></div>");
        Element p = doc.select("p").first();

        p.wrap("<div class='wrapper'></div>");
        assertEquals("<div class=\"wrapper\">\n <p>Inner text</p>\n</div>", doc.body().children().first().html());

        p.unwrap();
        assertEquals("<div class=\"wrapper\">\n Inner text\n</div>", doc.body().children().first().outerHtml());
    }

    // Tests before and after HTML/Node insertions
    @Test
    public void testBeforeAndAfter_insertContent_updatesSiblingNodes() {
        Document doc = Jsoup.parse("<div><p id='target'>Target</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span id='before-html'>Before</span>");
        target.after("<span id='after-html'>After</span>");

        target.before(new Element(Tag.valueOf("b"), "").text("BoldBefore"));
        target.after(new Element(Tag.valueOf("i"), "").text("ItalicAfter"));

        Element div = doc.select("div").first();
        assertEquals(5, div.childNodeSize());
        assertEquals("before-html", div.child(0).id());
        assertEquals("b", div.child(1).tagName());
        assertEquals("target", div.child(2).id());
        assertEquals("i", div.child(3).tagName());
        assertEquals("after-html", div.child(4).id());
    }

    // Tests append, prepend with raw HTML strings and child nodes
    @Test
    public void testAppendAndPrependHtml_stringAndNodes_updatesChildren() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<span>Appended 1</span>");
        div.prepend("<span>Prepended 1</span>");
        div.appendChild(new Element(Tag.valueOf("b"), "").text("Child"));
        div.prependChild(new Element(Tag.valueOf("i"), "").text("FirstChild"));

        assertEquals(4, div.children().size());
        assertEquals("i", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
        assertEquals("span", div.child(2).tagName());
        assertEquals("b", div.child(3).tagName());
    }

    // Tests insertChildren at specific index
    @Test
    public void testInsertChildren_atIndex_preservesOrder() {
        Document doc = Jsoup.parse("<div id='root'><p>First</p><p>Last</p></div>");
        Element root = doc.getElementById("root");

        Element middle1 = new Element(Tag.valueOf("span"), "").text("Middle 1");
        Element middle2 = new Element(Tag.valueOf("span"), "").text("Middle 2");

        root.insertChildren(1, Arrays.asList(middle1, middle2));

        assertEquals(4, root.children().size());
        assertEquals("p", root.child(0).tagName());
        assertEquals("Middle 1", root.child(1).text());
        assertEquals("Middle 2", root.child(2).text());
        assertEquals("p", root.child(3).tagName());
    }

    // Tests advanced query selector methods on Element
    @Test
    public void testAdvancedGetElements_variousSelectors_returnsExpectedResults() {
        Document doc = Jsoup.parse("<div id='container'>" +
                "<p title='apple-fruit' class='item first'>One 100</p>" +
                "<p title='banana-fruit' class='item'>Two 200</p>" +
                "<p title='grape-juice' class='item'>Three 300</p>" +
                "<span>Extra text</span>" +
                "</div>");
        Element container = doc.getElementById("container");

        assertEquals(4, container.getAllElements().size()); // container, p, p, p, span (excluding doc/html/body since query starts at container)
        assertEquals(2, container.getElementsByAttributeValueEnding("title", "-fruit").size());
        assertEquals(1, container.getElementsByAttributeValueStarting("title", "banana").size());
        assertEquals(1, container.getElementsByAttributeValueContaining("title", "juice").size());
        assertEquals(1, container.getElementsByAttributeValueMatching("title", Pattern.compile("^apple.*")).size());
        assertEquals(1, container.getElementsByAttributeValueMatching("title", "^grape.*").size());
        assertEquals(3, container.getElementsByAttributeValueNot("title", "banana-fruit").size()); // includes container & span which don't have that exact title value

        assertEquals(1, container.getElementsByIndexLessThan(1).size());
        assertEquals(2, container.getElementsByIndexGreaterThan(1).size());

        assertEquals(1, container.getElementsMatchingText(Pattern.compile("\\b200\\b")).size());
        assertEquals(1, container.getElementsMatchingText("\\b300\\b").size());
        assertEquals(1, container.getElementsMatchingOwnText(Pattern.compile("^Two 200$")).size());
        assertEquals(1, container.getElementsMatchingOwnText("^Three 300$").size());
    }

    // Tests preserveWhitespace for pre and plaintext elements
    @Test
    public void testPreserveWhitespace_preTag_returnsTrue() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        assertTrue(pre.preserveWhitespace());

        Element div = new Element(Tag.valueOf("div"), "");
        assertFalse(div.preserveWhitespace());

        div.appendChild(pre);
        assertFalse(div.preserveWhitespace());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentElements() {
        Element el1 = new Element(Tag.valueOf("p"), "").attr("id", "test");
        Element el2 = new Element(Tag.valueOf("p"), "").attr("id", "test");

        assertEquals(el1, el1);
        assertNotEquals(el1, el2);
        assertNotEquals(el1, "some string");
        assertNotEquals(el1, null);
    }
}