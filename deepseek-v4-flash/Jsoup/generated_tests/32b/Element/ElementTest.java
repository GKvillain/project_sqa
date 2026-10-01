package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * JUnit 4 test class for org.jsoup.nodes.Element, targeting common usage and potential defects.
 */
public class ElementTest {

    // Helper: create a simple div element
    private Element createDiv() {
        return new Element(Tag.valueOf("div"), "http://example.com");
    }

    // ==================== Constructor & Basic Properties ====================

    @Test
    // Tests normal constructor: tag name and base URI are set correctly
    public void testConstructor_validInput_createsElement() {
        Element div = createDiv();
        assertEquals("div", div.tagName());
        assertEquals("div", div.nodeName());
        assertTrue(div.isBlock());
    }

    // ==================== Child Manipulation ====================

    @Test
    // Tests appendChild adds child and children() returns it
    public void testAppendChild_addsChild_increasesChildren() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test(expected = IllegalArgumentException.class)
    // Tests appendChild with null child throws exception
    public void testAppendChild_null_throwsException() {
        Element parent = createDiv();
        parent.appendChild(null);
    }

    @Test
    // Tests prependChild inserts at the beginning
    public void testPrependChild_addsAtStart_firstChildIsPrepended() {
        Element parent = createDiv();
        Element first = new Element(Tag.valueOf("a"), "http://example.com");
        Element second = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(first);
        parent.prependChild(second);
        assertEquals(2, parent.children().size());
        assertSame(second, parent.child(0));
        assertSame(first, parent.child(1));
    }

    @Test
    // Tests children() returns empty list for element without children
    public void testChildren_empty_noChildren() {
        Element div = createDiv();
        assertTrue(div.children().isEmpty());
    }

    // ==================== Text Methods ====================

    @Test
    // Tests text() combines all descendant text
    public void testText_combinedText_returnsNormalized() {
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello ", "http://example.com"));
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        b.appendChild(new TextNode("there", "http://example.com"));
        p.appendChild(b);
        p.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello there now!", p.text());
    }

    @Test
    // Tests ownText() returns only direct child text nodes
    public void testOwnText_directTextOnly_doesNotIncludeDescendant() {
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello ", "http://example.com"));
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        b.appendChild(new TextNode("there", "http://example.com"));
        p.appendChild(b);
        p.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello  now!", p.ownText());
    }

    @Test
    // Tests hasText() returns true when element has non‑blank text
    public void testHasText_nonBlankText_returnsTrue() {
        Element div = createDiv();
        assertFalse(div.hasText());
        div.appendChild(new TextNode("  ", "http://example.com"));
        assertFalse(div.hasText()); // whitespace only
        div.appendChild(new TextNode("abc", "http://example.com"));
        assertTrue(div.hasText());
    }

    @Test
    // Tests setting text clears children and adds a text node
    public void testText_setText_clearsAndSetsContent() {
        Element div = createDiv();
        div.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        div.text("new content");
        assertEquals("new content", div.text());
        assertEquals(1, div.childNodes().size());
        assertTrue(div.childNodes().get(0) instanceof TextNode);
    }

    // ==================== HTML Methods ====================

    @Test
    // Tests html() returns inner HTML
    public void testHtml_innerHtml_returnsMarkup() {
        Element div = createDiv();
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        span.attr("class", "foo");
        div.appendChild(span);
        assertEquals("<span class=\"foo\"></span>", div.html());
    }

    @Test
    // Tests empty() removes all children
    public void testEmpty_removesAllChildren() {
        Element div = createDiv();
        div.appendChild(new TextNode("text", "http://example.com"));
        div.empty();
        assertTrue(div.children().isEmpty());
        assertTrue(div.childNodes().isEmpty());
    }

    // ==================== Class Manipulation ====================

    @Test
    // Tests classNames() returns empty set when no class attribute
    public void testClassNames_default_emptySet() {
        Element div = createDiv();
        Set<String> classes = div.classNames();
        assertTrue(classes.isEmpty());
    }

    @Test
    // Tests addClass adds a class and updates attribute
    public void testAddClass_newClass_addedToSetAndAttribute() {
        Element div = createDiv();
        div.addClass("foo");
        assertTrue(div.hasClass("foo"));
        assertEquals("foo", div.className());
    }

    @Test
    // Tests removeClass removes existing class
    public void testRemoveClass_existingClass_removed() {
        Element div = createDiv();
        div.addClass("foo");
        div.addClass("bar");
        div.removeClass("foo");
        assertFalse(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));
        assertEquals("bar", div.className());
    }

    @Test
    // Tests toggleClass toggles presence of a class
    public void testToggleClass_toggle_classAddedOrRemoved() {
        Element div = createDiv();
        div.toggleClass("foo");
        assertTrue(div.hasClass("foo"));
        div.toggleClass("foo");
        assertFalse(div.hasClass("foo"));
    }

    @Test
    // Tests hasClass is case‑insensitive
    public void testHasClass_caseInsensitive_returnsTrue() {
        Element div = createDiv();
        div.addClass("FOO");
        assertTrue(div.hasClass("foo"));
        assertTrue(div.hasClass("FOO"));
    }

    // ==================== Identity & Cloning ====================

    @Test
    // Tests equals returns true only for same object
    public void testEquals_sameObject_returnsTrue() {
        Element div = createDiv();
        assertTrue(div.equals(div));
        assertFalse(div.equals(createDiv()));
    }

    @Test
    // Tests clone creates an independent copy with the same structure
    public void testClone_independentCopy_sameContent() {
        Element original = createDiv();
        original.addClass("test");
        Element clone = original.clone();
        assertNotSame(original, clone);
        assertTrue(clone.hasClass("test"));
        // modifications to clone must not affect original
        clone.addClass("extra");
        assertFalse(original.hasClass("extra"));
    }

    // ==================== Sibling Methods ====================

    @Test
    // Tests siblingElements returns all other element siblings
    public void testSiblingElements_withSiblings_returnsOthers() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        Elements siblings = a.siblingElements();
        assertEquals(1, siblings.size());
        assertSame(b, siblings.get(0));
    }

    @Test
    // Tests nextElementSibling returns next element sibling
    public void testNextElementSibling_normal_returnsNext() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(b, a.nextElementSibling());
        assertNull(b.nextElementSibling());
    }

    @Test
    // Tests previousElementSibling returns previous element sibling
    public void testPreviousElementSibling_normal_returnsPrevious() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        assertNull(a.previousElementSibling());
        assertSame(a, b.previousElementSibling());
    }

    // ==================== Query Methods ====================

    @Test
    // Tests getElementById finds a child with matching id attribute
    public void testGetElementById_childWithId_returnsElement() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("id", "target");
        parent.appendChild(child);
        Element found = parent.getElementById("target");
        assertSame(child, found);
    }

    @Test
    // Tests getElementsByTag finds descendant elements with given tag
    public void testGetElementsByTag_descendant_returnsMatching() {
        Element parent = createDiv();
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(span);
        Elements list = parent.getElementsByTag("span");
        assertEquals(1, list.size());
        assertSame(span, list.get(0));
    }

    // ==================== InsertChildren ====================

    @Test(expected = IllegalArgumentException.class)
    // Tests insertChildren with out‑of‑bounds index throws
    public void testInsertChildren_outOfBounds_throwsException() {
        Element parent = createDiv();
        parent.appendChild(new Element(Tag.valueOf("a"), "http://example.com"));
        List<Element> toInsert = java.util.Collections.singletonList(new Element(Tag.valueOf("b"), "http://example.com"));
        // index 2 > currentSize (1) → should throw
        parent.insertChildren(2, toInsert);
    }

    @Test
    // Tests insertChildren inserts at specified index
    public void testInsertChildren_validIndex_insertsCorrectly() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.insertChildren(0, java.util.Collections.singletonList(b));
        assertEquals(2, parent.children().size());
        assertSame(b, parent.child(0));
        assertSame(a, parent.child(1));
    }

    // ==================== Val ====================

    @Test
    // Tests val() returns text for textarea, else value attribute
    public void testVal_textarea_returnsText() {
        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        textarea.text("my text");
        assertEquals("my text", textarea.val());
    }

    // ==================== Additional Tests for Uncovered Coverage ====================

    @Test
    // Tests isBlock() false for inline elements
    public void testIsBlock_span_returnsFalse() {
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        assertFalse(span.isBlock());
    }

    @Test
    // Tests updating tag name via tagName(String)
    public void testTagName_updateTagName_reflectsChange() {
        Element div = createDiv();
        div.tagName("span");
        assertEquals("span", div.tagName());
        assertEquals("span", div.nodeName());
    }

    @Test
    // Tests tag() returns the underlying Tag
    public void testTag_returnsTagObject() {
        Element div = createDiv();
        assertEquals("div", div.tag().getName());
    }

    @Test
    // Tests id() returns the id attribute
    public void testId_withId_returnsId() {
        Element div = createDiv();
        div.attr("id", "main");
        assertEquals("main", div.id());
    }

    @Test
    // Tests attr round-trip and hasAttr true/false paths
    public void testAttr_roundTrip_returnsValue() {
        Element div = createDiv();
        assertFalse(div.hasAttr("data-x"));
        div.attr("data-x", "value");
        assertTrue(div.hasAttr("data-x"));
        assertEquals("value", div.attr("data-x"));
    }

    @Test
    // Tests removeAttr removes an existing attribute
    public void testRemoveAttr_removesAttribute() {
        Element div = createDiv();
        div.attr("id", "x");
        div.removeAttr("id");
        assertFalse(div.hasAttr("id"));
    }

    @Test
    // Tests baseUri() returns configured base URI
    public void testBaseUri_returnsConfiguredUri() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("http://example.com", div.baseUri());
    }

    @Test
    // Tests absUrl resolves relative URLs against base URI
    public void testAbsUrl_resolvesRelativeUrl() {
        Element a = new Element(Tag.valueOf("a"), "http://example.com/base/");
        a.attr("href", "page.html");
        assertEquals("http://example.com/base/page.html", a.absUrl("href"));
    }

    @Test
    // Tests dataset() maps data-* attributes
    public void testDataset_mapsDataAttributes() {
        Element div = createDiv();
        div.attr("data-name", "value");
        Map<String, String> dataset = div.dataset();
        assertEquals("value", dataset.get("name"));
    }

    @Test
    // Tests parent() returns the parent element
    public void testParent_returnsParentElement() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        assertSame(parent, child.parent());
    }

    @Test
    // Tests parents() returns all ancestors
    public void testParents_returnsAncestors() {
        Element root = createDiv();
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        root.appendChild(parent);
        parent.appendChild(child);
        Elements parents = child.parents();
        assertTrue(parents.contains(parent));
        assertTrue(parents.contains(root));
    }

    @Test
    // Tests child(int) returns the child at the given index
    public void testChild_validIndex_returnsCorrectChild() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        assertSame(child, parent.child(0));
    }

    @Test
    // Tests childNodeSize counts all node types, not just elements
    public void testChildNodeSize_countIncludesNonElements() {
        Element div = createDiv();
        assertEquals(0, div.childNodeSize());
        div.appendChild(new TextNode("text", "http://example.com"));
        assertEquals(1, div.childNodeSize());
    }

    @Test
    // Tests childNodes returns all child nodes
    public void testChildNodes_containsAllNodeTypes() {
        Element div = createDiv();
        TextNode text = new TextNode("text", "http://example.com");
        div.appendChild(text);
        assertEquals(1, div.childNodes().size());
        assertSame(text, div.childNodes().get(0));
    }

    @Test
    // Tests appendChildren adds a collection of children
    public void testAppendChildren_addsAllChildren() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChildren(Arrays.asList(a, b));
        assertEquals(2, parent.children().size());
        assertSame(a, parent.child(0));
        assertSame(b, parent.child(1));
    }

    @Test
    // Tests prependChildren inserts all children at the beginning
    public void testPrependChildren_addsAllAtStart() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.prependChildren(Arrays.asList(b));
        assertSame(b, parent.child(0));
        assertSame(a, parent.child(1));
    }

    @Test
    // Tests firstElementSibling and lastElementSibling
    public void testFirstAndLastElementSibling_withSiblings() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        Element c = new Element(Tag.valueOf("c"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        assertSame(a, b.firstElementSibling());
        assertSame(c, b.lastElementSibling());
    }

    @Test
    // Tests elementSiblingIndex returns the correct index
    public void testElementSiblingIndex_returnsIndex() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        assertEquals(0, a.elementSiblingIndex());
        assertEquals(1, b.elementSiblingIndex());
    }

    @Test
    // Tests getElementById returns null when no element matches
    public void testGetElementById_missing_returnsNull() {
        Element parent = createDiv();
        parent.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        assertNull(parent.getElementById("missing"));
    }

    @Test
    // Tests getElementsByTag returns empty when no tag matches
    public void testGetElementsByTag_noMatch_returnsEmpty() {
        Element parent = createDiv();
        parent.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        assertTrue(parent.getElementsByTag("p").isEmpty());
    }

    @Test
    // Tests getElementsByClass matches a class attribute
    public void testGetElementsByClass_matchesClass() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("class", "foo bar");
        parent.appendChild(child);
        Elements found = parent.getElementsByClass("foo");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByAttribute matches attribute presence
    public void testGetElementsByAttribute_matchesAttributePresence() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("data-custom", "x");
        parent.appendChild(child);
        Elements found = parent.getElementsByAttribute("data-custom");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByAttributeStarting matches attribute key prefix
    public void testGetElementsByAttributeStarting_matchesPrefix() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("data-x", "1");
        parent.appendChild(child);
        Elements found = parent.getElementsByAttributeStarting("data-");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByAttributeValue matches exact attribute value
    public void testGetElementsByAttributeValue_matchesExactValue() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("type", "text");
        parent.appendChild(child);
        Elements found = parent.getElementsByAttributeValue("type", "text");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByAttributeValueStarting matches value prefix
    public void testGetElementsByAttributeValueStarting_matchesPrefixValue() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("class", "btn-primary");
        parent.appendChild(child);
        Elements found = parent.getElementsByAttributeValueStarting("class", "btn-");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByAttributeValueEnding matches value suffix
    public void testGetElementsByAttributeValueEnding_matchesSuffixValue() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("class", "btn-primary");
        parent.appendChild(child);
        Elements found = parent.getElementsByAttributeValueEnding("class", "primary");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByAttributeValueContaining matches substring in value
    public void testGetElementsByAttributeValueContaining_matchesContainingValue() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.attr("class", "btn-primary");
        parent.appendChild(child);
        Elements found = parent.getElementsByAttributeValueContaining("class", "prim");
        assertTrue(found.contains(child));
    }

    @Test
    // Tests getElementsByIndexLessThan filters by sibling index
    public void testGetElementsByIndexLessThan_returnsFirstN() {
        Element parent = createDiv();
        parent.appendChild(new Element(Tag.valueOf("a"), "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("b"), "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("c"), "http://example.com"));
        Elements found = parent.getElementsByIndexLessThan(2);
        assertTrue(found.contains(parent.child(0)));
        assertFalse(found.contains(parent.child(2)));
    }

    @Test
    // Tests getElementsByIndexGreaterThan filters by sibling index
    public void testGetElementsByIndexGreaterThan_returnsAfterIndex() {
        Element parent = createDiv();
        parent.appendChild(new Element(Tag.valueOf("a"), "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("b"), "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("c"), "http://example.com"));
        Elements found = parent.getElementsByIndexGreaterThan(0);
        assertTrue(found.contains(parent.child(1)));
        assertTrue(found.contains(parent.child(2)));
        assertFalse(found.contains(parent.child(0)));
    }

    @Test
    // Tests getElementsByIndexEquals filters by exact sibling index
    public void testGetElementsByIndexEquals_returnsMatchingIndex() {
        Element parent = createDiv();
        parent.appendChild(new Element(Tag.valueOf("a"), "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("b"), "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("c"), "http://example.com"));
        Elements found = parent.getElementsByIndexEquals(1);
        assertTrue(found.contains(parent.child(1)));
        assertFalse(found.contains(parent.child(0)));
    }

    @Test
    // Tests getElementsContainingText finds elements whose text contains a string
    public void testGetElementsContainingText_returnsMatchingElements() {
        Element parent = createDiv();
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello there", "http://example.com"));
        parent.appendChild(p);
        Elements found = parent.getElementsContainingText("Hello");
        assertTrue(found.contains(p));
    }

    @Test
    // Tests getElementsContainingOwnText finds elements whose own text contains a string
    public void testGetElementsContainingOwnText_returnsMatchingElements() {
        Element parent = createDiv();
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello there", "http://example.com"));
        parent.appendChild(p);
        Elements found = parent.getElementsContainingOwnText("Hello");
        assertTrue(found.contains(p));
    }

    @Test
    // Tests getElementsMatchingText matches descendant text with regex
    public void testGetElementsMatchingText_returnsMatchingElements() {
        Element parent = createDiv();
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello 123", "http://example.com"));
        parent.appendChild(p);
        Elements found = parent.getElementsMatchingText(Pattern.compile("\\d+"));
        assertTrue(found.contains(p));
    }

    @Test
    // Tests getElementsMatchingOwnText matches own text with regex
    public void testGetElementsMatchingOwnText_returnsMatchingElements() {
        Element parent = createDiv();
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello there", "http://example.com"));
        parent.appendChild(p);
        Elements found = parent.getElementsMatchingOwnText(Pattern.compile("Hello"));
        assertTrue(found.contains(p));
    }

    @Test
    // Tests getAllElements includes the element itself and descendants
    public void testGetAllElements_includesSelfAndDescendants() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        Elements all = parent.getAllElements();
        assertTrue(all.contains(parent));
        assertTrue(all.contains(child));
    }

    @Test
    // Tests html() returns empty string for an element with no children
    public void testHtml_empty_returnsEmptyString() {
        assertEquals("", createDiv().html());
    }

    @Test
    // Tests outerHtml includes attributes
    public void testOuterHtml_includesAttributes() {
        Element div = createDiv();
        div.attr("id", "x").addClass("y");
        String html = div.outerHtml();
        assertTrue(html.contains("id=\"x\""));
        assertTrue(html.contains("class=\"y\""));
    }

    @Test
    // Tests html(String) replaces existing children
    public void testHtml_setHtml_replacesChildren() {
        Element div = createDiv();
        div.append("<span>one</span>");
        div.html("<p>two</p>");
        assertEquals("<p>two</p>", div.html());
        assertEquals("p", div.child(0).tagName());
    }

    @Test
    // Tests append(String) parses HTML and adds children
    public void testAppend_parsesHtmlAndAddsChildren() {
        Element div = createDiv();
        div.append("<span>one</span>");
        assertEquals(1, div.children().size());
        assertEquals("span", div.child(0).tagName());
    }

    @Test
    // Tests prepend(String) parses HTML and adds at the beginning
    public void testPrepend_parsesHtmlAndAddsAtStart() {
        Element div = createDiv();
        div.append("<span>one</span>");
        div.prepend("<p>zero</p>");
        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
    }

    @Test
    // Tests before(String) inserts a sibling before the element
    public void testBefore_insertsElementBefore() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        parent.appendChild(a);
        a.before("<span>before</span>");
        assertEquals(2, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
        assertEquals("a", parent.child(1).tagName());
    }

    @Test
    // Tests after(String) inserts a sibling after the element
    public void testAfter_insertsElementAfter() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        parent.appendChild(a);
        a.after("<em>after</em>");
        assertEquals("a", parent.child(0).tagName());
        assertEquals("em", parent.child(1).tagName());
    }

    @Test
    // Tests wrap wraps the element in the provided HTML
    public void testWrap_wrapsElementInNewParent() {
        Element parent = createDiv();
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        parent.appendChild(a);
        a.wrap("<div class=\"wrapper\"></div>");
        Element wrapper = parent.child(0);
        assertEquals("div", wrapper.tagName());
        assertTrue(wrapper.hasClass("wrapper"));
        assertSame(a, wrapper.child(0));
    }

    @Test
    // Tests unwrap moves children into the parent and removes the element
    public void testUnwrap_movesChildrenIntoParent() {
        Element parent = createDiv();
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        b.appendChild(new TextNode("text", "http://example.com"));
        parent.appendChild(b);
        b.unwrap();
        assertTrue(parent.children().isEmpty());
        assertEquals(1, parent.childNodeSize());
        assertEquals("text", parent.text());
    }

    @Test
    // Tests cssSelector uses an id selector when id is present
    public void testCssSelector_withId_returnsIdSelector() {
        Element div = createDiv();
        div.attr("id", "content");
        assertEquals("#content", div.cssSelector());
    }

    @Test
    // Tests select runs a CSS query against the element
    public void testSelect_returnsMatchingElements() {
        Element div = createDiv();
        div.append("<p class=\"x\">one</p>");
        Elements p = div.select("p.x");
        assertEquals(1, p.size());
    }

    @Test
    // Tests appendElement creates and appends a child element
    public void testAppendElement_addsChildElement() {
        Element div = createDiv();
        div.appendElement("span");
        assertEquals("span", div.child(0).tagName());
    }

    @Test
    // Tests prependElement creates and prepends a child element
    public void testPrependElement_addsChildAtStart() {
        Element div = createDiv();
        div.appendElement("span");
        div.prependElement("em");
        assertEquals("em", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
    }

    @Test
    // Tests appendText adds a direct text node
    public void testAppendText_addsTextNode() {
        Element div = createDiv();
        div.appendText("hello");
        assertTrue(div.hasText());
        assertEquals("hello", div.ownText());
    }

    @Test
    // Tests prependText adds text at the beginning
    public void testPrependText_addsTextAtStart() {
        Element div = createDiv();
        div.appendText(" world");
        div.prependText("hello");
        assertEquals("hello world", div.ownText());
    }

    @Test
    // Tests appendTo moves the element to another parent
    public void testAppendTo_movesElementToNewParent() {
        Element originalParent = createDiv();
        Element newParent = createDiv();
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        originalParent.appendChild(child);
        child.appendTo(newParent);
        assertTrue(originalParent.children().isEmpty());
        assertSame(child, newParent.child(0));
    }

    @Test
    // Tests className() default value
    public void testClassName_default_returnsEmptyString() {
        assertEquals("", createDiv().className());
    }

    @Test
    // Tests className() joins multiple classes
    public void testClassName_multipleClasses_joinedBySpace() {
        Element div = createDiv();
        div.addClass("a");
        div.addClass("b");
        assertEquals("a b", div.className());
    }

    @Test
    // Tests classNames(Set) replaces the current class set
    public void testClassNames_setClassNames_replacesClasses() {
        Element div = createDiv();
        div.classNames(new LinkedHashSet<String>(Arrays.asList("a", "b")));
        assertTrue(div.hasClass("a"));
        assertTrue(div.hasClass("b"));
    }

    @Test
    // Tests val() returns value attribute for input elements
    public void testVal_input_returnsValueAttribute() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.attr("value", "foo");
        assertEquals("foo", input.val());
    }

    @Test
    // Tests val(String) updates value attribute for input elements
    public void testVal_setValue_updatesInputValue() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.val("bar");
        assertEquals("bar", input.attr("value"));
    }

    @Test
    // Tests val(String) updates text for textarea elements
    public void testVal_setTextareaValue_updatesText() {
        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        textarea.val("hello");
        assertEquals("hello", textarea.text());
    }

    @Test
    // Tests data() concatenates DataNode contents
    public void testData_concatenatesDataNodes() {
        Element script = new Element(Tag.valueOf("script"), "http://example.com");
        script.appendChild(new DataNode("var x=1;", "http://example.com"));
        assertEquals("var x=1;", script.data());
    }

    @Test
    // Tests siblingElements returns empty for an element without siblings
    public void testSiblingElements_noSiblings_returnsEmpty() {
        assertTrue(createDiv().siblingElements().isEmpty());
    }

    @Test
    // Tests parent() returns null for an orphan element
    public void testParent_noParent_returnsNull() {
        assertNull(createDiv().parent());
    }

    @Test
    // Tests parents() returns empty for an orphan element
    public void testParents_noParent_returnsEmpty() {
        assertTrue(createDiv().parents().isEmpty());
    }
}