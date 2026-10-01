package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests that siblingElements excludes this element and returns correct sibling count
    @Test
    public void testSiblingElements_hasParentAndSiblings_returnsSiblingsExcludingSelf() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        Element child3 = parent.appendElement("a");

        Elements siblings = child2.siblingElements();
        assertEquals(2, siblings.size());
        assertEquals("p", siblings.get(0).tagName());
        assertEquals("a", siblings.get(1).tagName());
        assertFalse(siblings.contains(child2));
    }

    // Tests sibling navigation: next, previous, first, last, and index
    @Test
    public void testSiblingNavigation_multipleChildren_returnsCorrectSiblingsAndIndices() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element c1 = parent.appendElement("h1");
        Element c2 = parent.appendElement("p");
        Element c3 = parent.appendElement("span");

        assertEquals(c2, c1.nextElementSibling());
        assertNull(c1.previousElementSibling());
        assertEquals(c1, c2.previousElementSibling());
        assertEquals(c3, c2.nextElementSibling());
        assertNull(c3.nextElementSibling());

        assertEquals(c1, c2.firstElementSibling());
        assertEquals(c3, c2.lastElementSibling());

        assertEquals(Integer.valueOf(0), c1.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), c2.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), c3.elementSiblingIndex());
    }

    // Tests elementSiblingIndex when element has no parent
    @Test
    public void testElementSiblingIndex_orphanElement_returnsZero() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());
    }

    // Tests tag name retrieval, updating, and validation
    @Test
    public void testTagName_changeTag_updatesTagSuccessfully() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());

        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    // Tests empty tag name exception
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyTag_throwsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    // Tests id and attribute manipulation
    @Test
    public void testIdAndAttributes_setAndGet_returnsExpectedValues() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-content");
        assertEquals("main-content", el.id());

        el.attr("data-test", "val");
        Map<String, String> dataset = el.dataset();
        assertEquals("val", dataset.get("test"));
    }

    // Tests children filtering and child node extraction
    @Test
    public void testChildrenAndTextNodes_mixedContent_filtersCorrectly() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("Hello ");
        Element span = p.appendElement("span");
        span.text("World");
        p.appendText("!");

        Elements children = p.children();
        assertEquals(1, children.size());
        assertEquals("span", children.get(0).tagName());
        assertEquals(span, p.child(0));

        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals("!", textNodes.get(1).getWholeText());
    }

    // Tests parent and ancestors hierarchy accumulation
    @Test
    public void testParents_nestedElements_accumulatesAllAncestors() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element body = root.appendElement("body");
        Element div = body.appendElement("div");
        Element span = div.appendElement("span");

        Elements parents = span.parents();
        assertEquals(2, parents.size());
        assertEquals(div, parents.get(0));
        assertEquals(body, parents.get(1));
    }

    // Tests append, prepend, empty, and text content updates
    @Test
    public void testAppendPrependAndEmpty_manipulateChildren_updatesStructure() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("Middle");
        div.prependElement("header").text("Top");
        div.appendElement("footer").text("Bottom");

        assertEquals(3, div.children().size());
        assertEquals("header", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
        assertEquals("footer", div.child(2).tagName());

        div.empty();
        assertEquals(0, div.children().size());
        assertEquals("", div.text());
    }

    // Tests text(), ownText(), and hasText() behavior with whitespace and br
    @Test
    public void testTextAndOwnText_nestedElements_returnsCorrectTextHierarchy() {
        Element p = new Element(Tag.valueOf("p"), "");
        assertFalse(p.hasText());

        p.appendText("One ");
        p.appendElement("span").text("Two");
        p.appendText(" Three");
        p.appendElement("br");
        p.appendText("Four");

        assertTrue(p.hasText());
        assertEquals("One Two Three Four", p.text());
        assertEquals("One Three Four", p.ownText());
    }

    // Tests class manipulation methods: add, remove, toggle, hasClass
    @Test
    public void testClassNames_manipulateClasses_updatesAttributeCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("FOO"));

        el.addClass("bar");
        assertEquals("foo bar", el.className());

        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));

        el.toggleClass("bar");
        assertFalse(el.hasClass("bar"));

        el.toggleClass("baz");
        assertTrue(el.hasClass("baz"));

        Set<String> classes = el.classNames();
        assertTrue(classes.contains("baz"));
    }

    // Tests val() on input and textarea
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

    // Tests DOM selection and query methods
    @Test
    public void testDOMSearchMethods_validHierarchy_findsMatchingElements() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p1 = div.appendElement("p").attr("id", "first").attr("class", "intro active").text("Hello World");
        Element p2 = div.appendElement("p").attr("id", "second").attr("class", "body").text("Jsoup testing");

        assertEquals(p1, div.getElementById("first"));
        assertNull(div.getElementById("unknown"));

        assertEquals(2, div.getElementsByTag("p").size());
        assertEquals(1, div.getElementsByClass("intro").size());
        assertEquals(1, div.getElementsByAttribute("id").size() + 1); // 2 total
        assertEquals(1, div.getElementsByAttributeValue("id", "second").size());
        assertEquals(1, div.getElementsByAttributeValueStarting("class", "in").size());
        assertEquals(1, div.getElementsByAttributeValueEnding("class", "active").size());
        assertEquals(1, div.getElementsByAttributeValueContaining("class", "tro").size());
        assertEquals(1, div.getElementsByAttributeValueMatching("id", Pattern.compile("^f.*t$")).size());

        assertEquals(1, div.getElementsByIndexEquals(0).size());
        assertEquals(1, div.getElementsByIndexLessThan(1).size());
        assertEquals(1, div.getElementsByIndexGreaterThan(0).size());

        assertEquals(1, div.getElementsContainingText("World").size());
        assertEquals(1, div.getElementsContainingOwnText("Hello").size());
        assertEquals(1, div.getElementsMatchingText("(?i)world").size());
        assertEquals(1, div.getElementsMatchingOwnText("(?i)hello").size());

        assertEquals(3, div.getAllElements().size()); // div, p1, p2
        assertEquals(1, div.select("p.intro").size());
    }

    // Tests HTML rendering and inner/outer HTML formatting
    @Test
    public void testHtmlAndOuterHtml_nestedTags_rendersExpectedMarkup() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "box");
        div.appendElement("span").text("inner");

        assertEquals("<span>inner</span>", div.html());
        assertEquals("<div id=\"box\"><span>inner</span></div>", div.outerHtml().replaceAll("\\s+", " ").trim());

        div.html("<b>bold</b>");
        assertEquals("<b>bold</b>", div.html());
        assertEquals(1, div.children().size());
        assertEquals("b", div.child(0).tagName());
    }

    // Tests data() extraction from DataNodes
    @Test
    public void testData_scriptTag_extractsData() {
        Element script = new Element(Tag.valueOf("script"), "");
        DataNode dataNode = new DataNode("var x = 1;", "");
        script.appendChild(dataNode);

        assertEquals(1, script.dataNodes().size());
        assertEquals("var x = 1;", script.data());
    }

    // Tests clone method independence
    @Test
    public void testClone_clonedElement_isDeepCopy() {
        Element original = new Element(Tag.valueOf("div"), "");
        original.attr("class", "orig");
        original.appendElement("p").text("child");

        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.outerHtml(), clone.outerHtml());

        clone.addClass("extra");
        assertFalse(original.hasClass("extra"));
        assertTrue(clone.hasClass("extra"));
    }

    @Test
    public void testSiblingNavigation_orphanElement_returnsNullAndEmptySiblings() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertNull(orphan.firstElementSibling());
        assertNull(orphan.lastElementSibling());
        assertEquals(0, orphan.siblingElements().size());
    }

    @Test
    public void testClassNames_setExplicitCollection_updatesClasses() {
        Element el = new Element(Tag.valueOf("div"), "");
        Set<String> classSet = new HashSet<String>();
        classSet.add("classA");
        classSet.add("classB");
        el.classNames(classSet);

        assertTrue(el.hasClass("classA"));
        assertTrue(el.hasClass("classB"));
        assertTrue(el.className().contains("classA"));
        assertTrue(el.className().contains("classB"));
    }

    @Test
    public void testAppendAndPrependHtml_stringInput_appendsAndPrependsParsedNodes() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>Paragraph</p>");
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());

        div.prepend("<span>First</span>");
        assertEquals(2, div.children().size());
        assertEquals("span", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
    }

    @Test
    public void testBeforeAndAfter_elementAndHtml_insertsSiblings() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element middle = parent.appendElement("p").text("middle");

        middle.before("<h1>Header</h1>");
        middle.after("<footer>Footer</footer>");

        Element extra = new Element(Tag.valueOf("span"), "");
        middle.before(extra);

        assertEquals(4, parent.children().size());
        assertEquals("h1", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());
        assertEquals("p", parent.child(2).tagName());
        assertEquals("footer", parent.child(3).tagName());
    }

    @Test
    public void testWrapAndUnwrap_nestedElements_wrapsAndUnwrapsCorrectly() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span").text("inner");

        Element wrapped = child.wrap("<div class='wrapper'></div>");
        assertEquals(wrapped, child);
        assertEquals("wrapper", child.parent().className());
        assertEquals(1, parent.children().size());
        assertEquals("wrapper", parent.child(0).className());

        Node unwrapped = child.unwrap();
        assertEquals(child, unwrapped);
        assertEquals(parent, child.parent());
        assertEquals("span", parent.child(0).tagName());
    }

    @Test
    public void testAdditionalDOMSearchMethods() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element a1 = div.appendElement("a").attr("href", "http://example.com/test").attr("target", "_blank").text("Link 1");
        Element a2 = div.appendElement("a").attr("href", "ftp://example.org").attr("target", "_self").text("Link 2");

        assertEquals(1, div.getElementsByAttributeValueNot("target", "_blank").size());
        assertEquals(2, div.getElementsByAttributeStarting("hr").size());
        assertEquals(1, div.getElementsByAttributeValueMatching("href", "^http.*").size());
        assertEquals(1, div.getElementsByMatchingText("Link 1").size());
        assertEquals(1, div.getElementsByMatchingOwnText("Link 2").size());
    }

    @Test
    public void testTag_getter_returnsTagInstance() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "");
        assertSame(tag, el.tag());
    }

    @Test
    public void testVal_genericElement_getsAndSetsAttribute() {
        Element span = new Element(Tag.valueOf("span"), "");
        assertEquals("", span.val());
        span.val("test-val");
        assertEquals("test-val", span.attr("value"));
        assertEquals("test-val", span.val());
    }

    @Test
    public void testCssSelector_nestedElements_generatesValidSelector() {
        Element doc = Jsoup.parse("<html><body><div id='content'><p class='first'>Hello</p></div></body></html>");
        Element p = doc.select("p.first").first();
        assertNotNull(p.cssSelector());
        assertEquals(p, doc.select(p.cssSelector()).first());
    }
}