package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests cloning an element to ensure classNames set is independently cloned
    @Test
    public void testClone_afterAccessingClassNames_createsIndependentCopy() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("class1");
        Set<String> originalClasses = el.classNames();
        assertTrue(originalClasses.contains("class1"));

        Element clone = el.clone();
        Set<String> cloneClasses = clone.classNames();

        assertNotSame(originalClasses, cloneClasses);
        clone.addClass("class2");
        assertFalse(el.hasClass("class2"));
        assertTrue(clone.hasClass("class2"));
    }

    // Tests updating tag name and checking block-level status
    @Test
    public void testTagName_validNewTag_updatesTagNameAndTagObject() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.tagName());
        assertFalse(el.isBlock());

        el.tagName("div");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());
    }

    // Tests empty tag name validation
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyTag_throwsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    // Tests ID retrieval when present and when missing
    @Test
    public void testId_presentAndMissing_returnsCorrectString() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-header");
        assertEquals("main-header", el.id());
    }

    // Tests dataset retrieval for HTML5 data- attributes
    @Test
    public void testDataset_customDataAttributes_returnsFilteredMap() {
        Attributes attrs = new Attributes();
        attrs.put("data-category", "books");
        attrs.put("id", "item1");
        Element el = new Element(Tag.valueOf("div"), "", attrs);

        Map<String, String> dataset = el.dataset();
        assertEquals(1, dataset.size());
        assertEquals("books", dataset.get("category"));
    }

    // Tests hierarchy methods like parent, parents, children, and child
    @Test
    public void testParentsAndChildren_nestedElements_navigatesTree() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = root.appendElement("p");
        Element child2 = root.appendElement("span");
        Element grandChild = child1.appendElement("b");

        assertEquals(root, child1.parent());
        assertEquals(2, root.children().size());
        assertEquals(child1, root.child(0));
        assertEquals(child2, root.child(1));

        Elements parents = grandChild.parents();
        assertEquals(2, parents.size());
        assertEquals(child1, parents.get(0));
        assertEquals(root, parents.get(1));
    }

    // Tests child text nodes and data nodes filtering
    @Test
    public void testTextNodesAndDataNodes_mixedChildren_filtersCorrectly() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendText("Text 1");
        el.appendElement("span").text("Child Span");
        el.appendChild(new DataNode("var x = 1;", ""));
        el.appendText("Text 2");

        List<TextNode> textNodes = el.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Text 1", textNodes.get(0).getWholeText());
        assertEquals("Text 2", textNodes.get(1).getWholeText());

        List<DataNode> dataNodes = el.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
    }

    // Tests inserting child nodes at start, end, and negative index
    @Test
    public void testInsertChildren_validAndNegativeIndex_insertsAtCorrectPosition() {
        Element el = new Element(Tag.valueOf("div"), "");
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        Element p3 = new Element(Tag.valueOf("p"), "");

        el.appendChild(p1);
        el.appendChild(p3);

        el.insertChildren(1, Collections.singletonList(p2));
        assertEquals(3, el.children().size());
        assertEquals(p2, el.child(1));

        Element p0 = new Element(Tag.valueOf("p"), "");
        el.insertChildren(0, Collections.singletonList(p0));
        assertEquals(p0, el.child(0));

        Element pLast = new Element(Tag.valueOf("p"), "");
        el.insertChildren(-1, Collections.singletonList(pLast));
        assertEquals(pLast, el.child(4));
    }

    // Tests inserting child nodes with invalid index throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_outOfBoundsIndex_throwsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(5, Collections.singletonList(new Element(Tag.valueOf("p"), "")));
    }

    // Tests sibling navigation methods
    @Test
    public void testSiblingElements_multipleSiblings_navigatesCorrectly() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element first = parent.appendElement("span");
        Element middle = parent.appendElement("p");
        Element last = parent.appendElement("b");

        assertEquals(2, middle.siblingElements().size());
        assertNull(first.previousElementSibling());
        assertEquals(middle, first.nextElementSibling());
        assertEquals(last, middle.nextElementSibling());
        assertEquals(middle, last.previousElementSibling());
        assertNull(last.nextElementSibling());

        assertEquals(first, middle.firstElementSibling());
        assertEquals(last, middle.lastElementSibling());
        assertEquals(Integer.valueOf(1), middle.elementSiblingIndex());
    }

    // Tests sibling navigation when element has no parent
    @Test
    public void testSiblingElements_orphanElement_returnsNullOrEmpty() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertEquals(0, orphan.siblingElements().size());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());
    }

    // Tests DOM selection and search by tag, class, and id
    @Test
    public void testGetElements_byTagClassAndId_findsMatchingNodes() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = root.appendElement("p").attr("id", "p1").addClass("intro");
        Element child2 = root.appendElement("p").addClass("body");
        Element grandChild = child2.appendElement("span").addClass("intro");

        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(child1, root.getElementById("p1"));
        assertNull(root.getElementById("nonexistent"));

        Elements introElements = root.getElementsByClass("intro");
        assertEquals(2, introElements.size());
        assertTrue(introElements.contains(child1));
        assertTrue(introElements.contains(grandChild));

        Elements all = root.getAllElements();
        assertEquals(4, all.size());
    }

    // Tests DOM search by attribute variations
    @Test
    public void testGetElementsByAttribute_variousConditions_returnsMatches() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element link1 = root.appendElement("a").attr("href", "http://example.com/one").attr("data-test", "val1");
        Element link2 = root.appendElement("a").attr("href", "https://jsoup.org/two").attr("data-test", "val2");

        assertEquals(2, root.getElementsByAttribute("href").size());
        assertEquals(2, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("href", "http://example.com/one").size());
        assertEquals(link1, root.getElementsByAttributeValue("href", "http://example.com/one").get(0));
        assertEquals(1, root.getElementsByAttributeValueStarting("href", "https://").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("href", "two").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("href", "o").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("href", Pattern.compile("jsoup\\.org")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("href", "example\\.com").size());
    }

    // Tests sibling index queries (less than, greater than, equals)
    @Test
    public void testGetElementsByIndex_indexComparisons_returnsCorrectElements() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("p");
        parent.appendElement("p");

        assertEquals(1, parent.getElementsByIndexLessThan(1).size());
        assertEquals(1, parent.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, parent.getElementsByIndexEquals(1).size());
    }

    // Tests text matching queries by string and regular expression
    @Test
    public void testGetElementsMatchingText_patterns_returnsMatches() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element p1 = root.appendElement("p").text("Hello World");
        Element p2 = root.appendElement("p").text("Goodbye World");

        assertEquals(2, root.getElementsContainingText("World").size());
        assertEquals(1, root.getElementsContainingOwnText("Hello").size());
        assertEquals(2, root.getElementsMatchingText("(?i)world").size());
        assertEquals(1, root.getElementsMatchingOwnText("Hello.*").size());
    }

    // Tests regex syntax exception handling
    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingText_invalidRegex_throwsException() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.getElementsMatchingText("[invalid regex");
    }

    // Tests text and ownText retrieval and whitespace normalization
    @Test
    public void testTextAndOwnText_nestedElements_normalizesAndExtracts() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendText("Hello ");
        div.appendElement("b").text("beautiful");
        div.appendText(" world!");

        assertEquals("Hello beautiful world!", div.text());
        assertEquals("Hello world!", div.ownText());
        assertTrue(div.hasText());

        Element empty = new Element(Tag.valueOf("p"), "");
        assertFalse(empty.hasText());
        assertEquals("", empty.text());
        assertEquals("", empty.ownText());
    }

    // Tests setting text and emptying element
    @Test
    public void testTextSetterAndEmpty_resetsChildren() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendElement("span").text("old content");
        assertEquals(1, el.children().size());

        el.text("new content");
        assertEquals(0, el.children().size());
        assertEquals("new content", el.text());

        el.empty();
        assertEquals(0, el.childNodes().size());
        assertEquals("", el.text());
    }

    // Tests class manipulation methods: addClass, removeClass, toggleClass, hasClass
    @Test
    public void testClassManipulation_addRemoveToggle_updatesAttributes() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("highlight");
        assertTrue(el.hasClass("highlight"));
        assertTrue(el.hasClass("HIGHLIGHT")); // Case insensitive
        assertEquals("highlight", el.className());

        el.addClass("secondary");
        assertTrue(el.hasClass("secondary"));
        assertEquals("highlight secondary", el.className());

        el.removeClass("highlight");
        assertFalse(el.hasClass("highlight"));
        assertTrue(el.hasClass("secondary"));

        el.toggleClass("secondary");
        assertFalse(el.hasClass("secondary"));
        el.toggleClass("secondary");
        assertTrue(el.hasClass("secondary"));
    }

    // Tests val() and val(String) for input and textarea tags
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValues() {
        Element input = new Element(Tag.valueOf("input"), "");
        assertEquals("", input.val());
        input.val("user123");
        assertEquals("user123", input.val());
        assertEquals("user123", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        assertEquals("", textarea.val());
        textarea.val("Sample text in textarea");
        assertEquals("Sample text in textarea", textarea.val());
        assertEquals("Sample text in textarea", textarea.text());
    }

    // Tests HTML and outer HTML rendering including self-closing tags
    @Test
    public void testOuterHtml_regularAndSelfClosingTags_rendersCorrectly() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "container");
        Element p = div.appendElement("p");
        p.text("Paragraph");

        assertEquals("<p>Paragraph</p>", div.html());
        assertEquals("<div id=\"container\">\n <p>Paragraph</p>\n</div>", div.outerHtml());

        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\" />", img.outerHtml());
    }
}