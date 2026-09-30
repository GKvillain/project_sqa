package org.jsoup.nodes;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

public class ElementTest {

    private Element createDiv(String baseUri) {
        return new Element(Tag.valueOf("div"), baseUri);
    }

    @Test
    public void testConstructorAndNodeName() {
        Element div = createDiv("http://example.com");
        assertEquals("div", div.nodeName());
        assertEquals("div", div.tagName());
    }

    @Test
    public void testText_WithMixedChildren_ReturnsNormalizedText() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("Hello ", ""));
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("there", ""));
        p.appendChild(b);
        p.appendChild(new TextNode(" now! ", ""));
        assertEquals("Hello there now!", p.text());
    }

    @Test
    public void testOwnText_WithMixedChildren_ReturnsDirectText() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("Hello ", ""));
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("there", ""));
        p.appendChild(b);
        p.appendChild(new TextNode(" now!", ""));
        assertEquals("Hello now!", p.ownText());
    }

    @Test
    public void testHasText_WithContent_ReturnsTrue() {
        Element div = createDiv("");
        div.appendChild(new TextNode("Hello", ""));
        assertTrue(div.hasText());
    }

    @Test
    public void testHasText_WithBlankText_ReturnsFalse() {
        Element div = createDiv("");
        div.appendChild(new TextNode("   ", ""));
        assertFalse(div.hasText());
    }

    @Test
    public void testClassNames_WithMultipleClasses_ReturnsSet() {
        Element div = createDiv("");
        div.attr("class", "header gray");
        Set<String> expected = new LinkedHashSet<>(Arrays.asList("header", "gray"));
        assertEquals(expected, div.classNames());
    }

    @Test
    public void testHasClass_CaseInsensitive_ReturnsTrue() {
        Element div = createDiv("");
        div.attr("class", "Header");
        assertTrue(div.hasClass("header"));
        assertTrue(div.hasClass("Header"));
        assertTrue(div.hasClass("HEADER"));
    }

    @Test
    public void testAddClass_ThenRemoveClass_ChangesClassAttribute() {
        Element div = createDiv("");
        div.addClass("foo");
        assertEquals("foo", div.className());
        div.addClass("bar");
        assertTrue(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));
        div.removeClass("foo");
        assertFalse(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));
    }

    @Test
    public void testToggleClass_WhenPresent_RemovesIt() {
        Element div = createDiv("");
        div.attr("class", "active");
        div.toggleClass("active");
        assertEquals("", div.className().trim());
    }

    @Test
    public void testVal_TextArea_ReturnsTextContent() {
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("my value");
        assertEquals("my value", textarea.val());
    }

    @Test
    public void testSiblingElements_WithSiblings_ReturnsAllButSelf() {
        Element parent = createDiv("");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        Element child3 = parent.appendElement("div");
        Elements siblings = child2.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(child1));
        assertTrue(siblings.contains(child3));
    }

    @Test
    public void testNextAndPreviousElementSibling_WithSiblings_ReturnsCorrectElement() {
        Element parent = createDiv("");
        Element first = parent.appendElement("p");
        Element second = parent.appendElement("span");
        Element third = parent.appendElement("div");
        assertNull(first.previousElementSibling());
        assertEquals(second, first.nextElementSibling());
        assertEquals(first, second.previousElementSibling());
        assertEquals(third, second.nextElementSibling());
        assertNull(third.nextElementSibling());
    }

    @Test
    public void testCssSelector_StandaloneElement_ReturnsTag() {
        Element div = createDiv("");
        assertEquals("div", div.cssSelector());
    }

    @Test
    public void testCssSelector_WithParentNoId_ReturnsParentChild() {
        Element parent = createDiv("");
        Element child = parent.appendElement("p");
        assertEquals("div > p", child.cssSelector());
    }

    @Test
    public void testCssSelector_WithMultipleSiblings_IncludesNthChild() {
        Element parent = createDiv("");
        parent.appendElement("p");
        parent.appendElement("p");
        Element secondP = parent.children().get(1);
        String selector = secondP.cssSelector();
        assertTrue(selector.contains(":nth-child(2)"));
    }

    @Test
    public void testHtml_WithChildElements_ReturnsInnerHtml() {
        Element div = createDiv("");
        div.appendElement("p").text("Hello");
        assertEquals("<p>Hello</p>", div.html());
    }

    @Test
    public void testEmpty_RemovesAllChildren() {
        Element div = createDiv("");
        div.appendElement("p");
        div.appendElement("span");
        div.empty();
        assertEquals(0, div.childNodeSize());
    }

    @Test
    public void testInsertChildren_AtEnd_InsertsCorrectly() {
        Element div = createDiv("");
        Element existing = div.appendElement("p");
        Element newChild = new Element(Tag.valueOf("span"), "");
        div.insertChildren(-1, Collections.singletonList(newChild));
        assertEquals(2, div.children().size());
        assertEquals(existing, div.child(0));
        assertEquals(newChild, div.child(1));
    }

    @Test
    public void testGetElementById_WithId_ReturnsElement() {
        Element div = createDiv("");
        Element target = div.appendElement("span");
        target.attr("id", "myId");
        assertEquals(target, div.getElementById("myId"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatching_InvalidRegex_ThrowsIllegalArgumentException() {
        Element div = createDiv("");
        div.getElementsByAttributeValueMatching("href", "[invalid");
    }

    // ==================== Test cases added for uncovered coverage ====================

    @Test
    public void testHasAttribute_Existing_ReturnsTrue() {
        Element div = createDiv("");
        div.attr("id", "test");
        assertTrue(div.hasAttr("id"));
    }

    @Test
    public void testHasAttribute_NonExisting_ReturnsFalse() {
        Element div = createDiv("");
        assertFalse(div.hasAttr("class"));
    }

    @Test
    public void testGetAttribute_WhenSet_ReturnsValue() {
        Element div = createDiv("");
        div.attr("data-value", "hello");
        assertEquals("hello", div.attr("data-value"));
    }

    @Test
    public void testRemoveAttribute_Removes() {
        Element div = createDiv("");
        div.attr("style", "color:red");
        div.removeAttr("style");
        assertFalse(div.hasAttr("style"));
    }

    @Test
    public void testClassNames_WithExtraSpaces_Normalizes() {
        Element div = createDiv("");
        div.attr("class", "  foo   bar  ");
        Set<String> expected = new LinkedHashSet<>(Arrays.asList("foo", "bar"));
        assertEquals(expected, div.classNames());
    }

    @Test
    public void testAddClass_Duplicate_DoesNotDuplicate() {
        Element div = createDiv("");
        div.addClass("foo");
        div.addClass("foo");
        assertEquals("foo", div.className());
    }

    @Test
    public void testRemoveClass_NonExistent_NoChange() {
        Element div = createDiv("");
        div.attr("class", "foo");
        div.removeClass("bar");
        assertEquals("foo", div.className());
    }

    @Test
    public void testToggleClass_NotPresent_Adds() {
        Element div = createDiv("");
        div.toggleClass("active");
        assertTrue(div.hasClass("active"));
    }

    @Test
    public void testGetElementsByTag_ReturnsCorrect() {
        Element div = createDiv("");
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("p");
        Element span = div.appendElement("span");
        Elements ps = div.getElementsByTag("p");
        assertEquals(2, ps.size());
        assertTrue(ps.contains(p1));
        assertTrue(ps.contains(p2));
    }

    @Test
    public void testGetElementsByClass_ReturnsCorrect() {
        Element div = createDiv("");
        Element a = div.appendElement("p").addClass("foo");
        Element b = div.appendElement("span").addClass("foo");
        div.appendElement("div").addClass("bar");
        Elements foos = div.getElementsByClass("foo");
        assertEquals(2, foos.size());
        assertTrue(foos.contains(a));
        assertTrue(foos.contains(b));
    }

    @Test
    public void testGetElementsByAttribute_ReturnsCorrect() {
        Element div = createDiv("");
        Element a = div.appendElement("p").attr("data-x", "1");
        Element b = div.appendElement("span").attr("data-y", "2");
        div.appendElement("div");
        Elements dataAttrs = div.getElementsByAttribute("data-x");
        assertEquals(1, dataAttrs.size());
        assertTrue(dataAttrs.contains(a));
    }

    @Test
    public void testGetElementsByAttributeValue_ReturnsCorrect() {
        Element div = createDiv("");
        Element a = div.appendElement("p").attr("hidden", "true");
        Element b = div.appendElement("span").attr("hidden", "false");
        Elements hiddenTrue = div.getElementsByAttributeValue("hidden", "true");
        assertEquals(1, hiddenTrue.size());
        assertTrue(hiddenTrue.contains(a));
    }

    @Test
    public void testGetElementById_NotFound_ReturnsNull() {
        Element div = createDiv("");
        assertNull(div.getElementById("nonexistent"));
    }

    @Test
    public void testClone_EqualsOriginal() {
        Element div = createDiv("http://example.com");
        div.attr("class", "foo bar");
        div.appendElement("p").text("Hello");
        Element cloned = div.clone();
        assertEquals(div.html(), cloned.html());
        assertEquals(div.outerHtml(), cloned.outerHtml());
        assertTrue(cloned.hasClass("foo"));
        assertEquals(div.tagName(), cloned.tagName());
        assertEquals(div.children().size(), cloned.children().size());
    }

    @Test
    public void testDataSet_ReturnsModifiableMap() {
        Element div = createDiv("");
        div.attr("data-name", "jsoup");
        Map<String, String> data = div.dataset();
        assertEquals("jsoup", data.get("name"));
        data.put("lang", "java");
        assertTrue(div.hasAttr("data-lang"));
    }

    @Test
    public void testVal_Input_ReturnsValueAttribute() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "myValue");
        assertEquals("myValue", input.val());
    }

    @Test
    public void testAppendChild_AddsToEnd() {
        Element div = createDiv("");
        Element first = div.appendElement("p");
        Element second = new Element(Tag.valueOf("span"), "");
        div.appendChild(second);
        assertEquals(2, div.children().size());
        assertEquals(first, div.child(0));
        assertEquals(second, div.child(1));
    }

    @Test
    public void testPrependChild_AddsToStart() {
        Element div = createDiv("");
        Element first = div.appendElement("p");
        Element newChild = new Element(Tag.valueOf("span"), "");
        div.prependChild(newChild);
        assertEquals(2, div.children().size());
        assertEquals(newChild, div.child(0));
        assertEquals(first, div.child(1));
    }

    @Test
    public void testWholeText_ReturnsConcatenatedTextNodes() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("Hello", ""));
        p.appendChild(new TextNode(" World", ""));
        assertEquals("Hello World", p.wholeText());
    }

    @Test
    public void testOuterHtml_IncludesTagAndAttributes() {
        Element div = createDiv("");
        div.attr("class", "test");
        div.text("Content");
        String outer = div.outerHtml();
        assertTrue(outer.startsWith("<div"));
        assertTrue(outer.contains("class=\"test\""));
        assertTrue(outer.contains("Content"));
        assertTrue(outer.endsWith("</div>"));
    }

    @Test
    public void testCssSelector_WithId_IncludesId() {
        Element div = createDiv("");
        Element target = div.appendElement("p").attr("id", "myId");
        String selector = target.cssSelector();
        assertEquals("#myId", selector);
    }

    @Test
    public void testCssSelector_WithClass_IncludesClass() {
        Element div = createDiv("");
        Element target = div.appendElement("p").addClass("highlight");
        String selector = target.cssSelector();
        assertTrue(selector.endsWith("p.highlight"));
    }
}