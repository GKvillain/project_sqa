package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests tag name modification and retrieval
    @Test
    public void testTagName_changeTag_updatesTagNameAndTagObject() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());

        el.tagName("div");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());
    }

    // Tests exception when tag name is empty
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyString_throwsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    // Tests ID and dataset attribute access
    @Test
    public void testAttrAndDataset_customAttributes_returnsIdAndDatasetMap() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-div");
        assertEquals("main-div", el.id());

        el.attr("data-name", "jsoup");
        el.attr("data-version", "1.0");
        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("1.0", dataset.get("version"));
    }

    // Tests parent and parents hierarchy accumulation
    @Test
    public void testParents_nestedHierarchy_accumulatesAncestorsInOrder() {
        Element grandParent = new Element(Tag.valueOf("div"), "");
        Element parent = new Element(Tag.valueOf("section"), "");
        Element child = new Element(Tag.valueOf("p"), "");

        grandParent.appendChild(parent);
        parent.appendChild(child);

        assertEquals(parent, child.parent());
        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertEquals(parent, parents.get(0));
        assertEquals(grandParent, parents.get(1));
    }

    // Tests child element retrieval and children filtering from mixed child nodes
    @Test
    public void testChildAndChildren_mixedNodes_returnsOnlyElementChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        TextNode text = new TextNode("Text", "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");

        parent.appendChild(text);
        parent.appendChild(child1);
        parent.appendChild(child2);

        assertEquals(3, parent.childNodes().size());
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertEquals(child1, parent.child(0));
        assertEquals(child2, parent.child(1));
    }

    // Tests textNodes and dataNodes filtering
    @Test
    public void testTextNodesAndDataNodes_mixedNodes_returnsFilteredUnmodifiableLists() {
        Element el = new Element(Tag.valueOf("script"), "");
        TextNode text = new TextNode("Some text", "");
        DataNode data = new DataNode("var x = 1;", "");

        el.appendChild(text);
        el.appendChild(data);

        List<TextNode> textNodes = el.textNodes();
        assertEquals(1, textNodes.size());
        assertEquals("Some text", textNodes.get(0).getWholeText());

        List<DataNode> dataNodes = el.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
        assertEquals("var x = 1;", el.data());
    }

    // Tests insertChildren with positive and negative rolled index
    @Test
    public void testInsertChildren_validAndNegativeIndex_shiftsAndInsertsNodes() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element c1 = new Element(Tag.valueOf("p"), "");
        Element c2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(c1);

        List<Node> toInsertStart = Collections.<Node>singletonList(c2);
        parent.insertChildren(0, toInsertStart);
        assertEquals(c2, parent.child(0));
        assertEquals(c1, parent.child(1));

        Element c3 = new Element(Tag.valueOf("b"), "");
        parent.insertChildren(-1, Collections.<Node>singletonList(c3));
        assertEquals(c3, parent.child(2));
    }

    // Tests append and prepend helper methods for child elements and text
    @Test
    public void testAppendAndPrepend_elementsAndText_correctChildOrdering() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element appended = root.appendElement("span");
        Element prepended = root.prependElement("p");
        root.appendText("After");
        root.prependText("Before");

        assertEquals("p", prepended.tagName());
        assertEquals("span", appended.tagName());
        assertEquals(4, root.childNodes().size());
        assertTrue(root.childNodes().get(0) instanceof TextNode);
        assertEquals(prepended, root.childNodes().get(1));
        assertEquals(appended, root.childNodes().get(2));
        assertTrue(root.childNodes().get(3) instanceof TextNode);
    }

    // Tests sibling elements and sibling navigation
    @Test
    public void testSiblingNavigation_multipleSiblings_navigatesCorrectly() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element c1 = parent.appendElement("p");
        Element c2 = parent.appendElement("span");
        Element c3 = parent.appendElement("b");

        assertEquals(0, c1.elementSiblingIndex().intValue());
        assertEquals(1, c2.elementSiblingIndex().intValue());
        assertEquals(2, c3.elementSiblingIndex().intValue());

        assertEquals(c2, c1.nextElementSibling());
        assertNull(c1.previousElementSibling());
        assertEquals(c1, c2.previousElementSibling());
        assertEquals(c3, c2.nextElementSibling());
        assertNull(c3.nextElementSibling());

        assertEquals(c1, c2.firstElementSibling());
        assertEquals(c3, c2.lastElementSibling());

        Elements siblings = c2.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(c1));
        assertTrue(siblings.contains(c3));
        assertFalse(siblings.contains(c2));
    }

    // Tests sibling navigation when element has no parent
    @Test
    public void testSiblingNavigation_noParent_returnsNullAndZeroIndex() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertEquals(0, orphan.elementSiblingIndex().intValue());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertEquals(0, orphan.siblingElements().size());
    }

    // Tests DOM query methods by Tag, Class, Attribute, and Sibling Index
    @Test
    public void testGetElementsBy_variousFilters_matchesExpectedElements() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element p1 = root.appendElement("p").attr("class", "lead highlight").attr("title", "first");
        Element p2 = root.appendElement("p").attr("class", "highlight").attr("title", "second");
        Element span = root.appendElement("span").attr("data-item", "active");

        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(1, root.getElementsByTag("span").size());

        assertEquals(2, root.getElementsByClass("highlight").size());
        assertEquals(1, root.getElementsByClass("lead").size());

        assertEquals(2, root.getElementsByAttribute("title").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("title", "first").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("title", "fir").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("title", "ond").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("title", "ir").size());
        assertEquals(1, root.getElementsByAttributeValueNot("title", "first").size());
        assertEquals(2, root.getElementsByAttributeValueMatching("title", Pattern.compile("first|second")).size());
        assertEquals(2, root.getElementsByAttributeValueMatching("title", "first|second").size());

        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, root.getElementsByIndexEquals(1).size());
        assertEquals(4, root.getAllElements().size()); // root + p1 + p2 + span
    }

    // Tests getElementById returning first matching element or null
    @Test
    public void testGetElementById_existingAndMissingId_returnsElementOrNull() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element p = root.appendElement("p").attr("id", "target");

        assertEquals(p, root.getElementById("target"));
        assertNull(root.getElementById("non-existent"));
    }

    // Tests text matching queries using string and regex
    @Test
    public void testGetElementsMatchingText_stringAndRegex_findsMatchingElements() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element p1 = root.appendElement("p");
        p1.text("Hello World");
        Element p2 = root.appendElement("p");
        p2.text("Foo Bar");

        assertEquals(1, root.getElementsContainingText("world").size());
        assertEquals(1, root.getElementsContainingOwnText("Foo").size());
        assertEquals(1, root.getElementsMatchingText(Pattern.compile("Hello.*")).size());
        assertEquals(1, root.getElementsMatchingText("Hello.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("Foo.*")).size());
        assertEquals(1, root.getElementsMatchingOwnText("Foo.*").size());
    }

    // Tests combined text, ownText, and text setting
    @Test
    public void testTextAndOwnText_nestedElements_returnsProperTextAndModifies() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("Hello ");
        Element b = p.appendElement("b");
        b.text("World");
        p.appendText("!");

        assertEquals("Hello World!", p.text());
        assertEquals("Hello !", p.ownText());
        assertTrue(p.hasText());

        p.text("New Text");
        assertEquals("New Text", p.text());
        assertEquals(1, p.childNodes().size());
    }

    // Tests hasText on empty and whitespace-only elements
    @Test
    public void testHasText_emptyOrWhitespace_returnsFalse() {
        Element empty = new Element(Tag.valueOf("div"), "");
        assertFalse(empty.hasText());

        empty.appendText("   ");
        assertFalse(empty.hasText());

        empty.appendText("content");
        assertTrue(empty.hasText());
    }

    // Tests CSS class manipulations: hasClass, addClass, removeClass, toggleClass
    @Test
    public void testClassNames_classOperations_updatesClassAttributeCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "header main");

        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("MAIN")); // case insensitive
        assertFalse(el.hasClass("footer"));

        el.addClass("footer");
        assertTrue(el.hasClass("footer"));

        el.removeClass("main");
        assertFalse(el.hasClass("main"));

        el.toggleClass("visible");
        assertTrue(el.hasClass("visible"));
        el.toggleClass("visible");
        assertFalse(el.hasClass("visible"));

        Set<String> customClasses = new LinkedHashSet<String>(Arrays.asList("one", "two"));
        el.classNames(customClasses);
        assertEquals("one two", el.className());
        assertEquals(2, el.classNames().size());
    }

    // Tests val() and val(String) on input and textarea elements
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValues() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-input");
        assertEquals("test-input", input.val());
        assertEquals("test-input", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("test-area");
        assertEquals("test-area", textarea.val());
        assertEquals("test-area", textarea.text());
    }

    // Tests html(), html(String), and empty()
    @Test
    public void testHtmlAndEmpty_innerHtml_retrievesAndModifiesCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendElement("p").text("paragraph");

        assertTrue(el.html().contains("<p>paragraph</p>"));

        el.html("<span>replaced</span>");
        assertEquals(1, el.children().size());
        assertEquals("span", el.child(0).tagName());
        assertEquals("replaced", el.child(0).text());

        el.empty();
        assertEquals(0, el.childNodes().size());
        assertEquals("", el.html());
    }

    // Tests select query delegation to Selector
    @Test
    public void testSelect_cssQuery_returnsMatchingElements() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendElement("p").attr("class", "msg").text("Hello");
        root.appendElement("span").text("World");

        Elements selected = root.select("p.msg");
        assertEquals(1, selected.size());
        assertEquals("Hello", selected.get(0).text());
    }

    // Tests clone, equals, and hashCode behavior
    @Test
    public void testCloneAndEquals_clonedElement_isEqualStructureButDifferentInstance() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "my-class");
        el.appendElement("span").text("child");

        Element clone = el.clone();
        assertNotSame(el, clone);
        assertEquals(el.tagName(), clone.tagName());
        assertEquals(el.className(), clone.className());
        assertEquals(1, clone.children().size());

        assertTrue(el.equals(el));
        assertFalse(el.equals(clone));
        assertEquals(el.hashCode(), el.hashCode());
    }
}