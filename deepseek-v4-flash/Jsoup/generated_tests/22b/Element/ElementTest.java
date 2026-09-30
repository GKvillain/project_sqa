import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

public class ElementTest {

    private Element createDiv() {
        return new Element(Tag.valueOf("div"), "http://example.com");
    }

    // Tests basic tag name getter
    @Test
    public void testTagName_defaultReturnsTagName() {
        Element div = createDiv();
        assertEquals("div", div.tagName());
    }

    // Tests changing tag name
    @Test
    public void testTagName_setterChangesTagName() {
        Element div = createDiv();
        div.tagName("span");
        assertEquals("span", div.tagName());
    }

    // Tests block detection
    @Test
    public void testIsBlock_divIsBlock() {
        Element div = createDiv();
        assertTrue(div.isBlock());
    }

    // Tests inline detection
    @Test
    public void testIsBlock_spanIsNotBlock() {
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        assertFalse(span.isBlock());
    }

    // Tests id attribute getter when present
    @Test
    public void testId_returnsAttribute() {
        Element div = createDiv();
        div.attr("id", "myId");
        assertEquals("myId", div.id());
    }

    // Tests id default to empty
    @Test
    public void testId_defaultEmpty() {
        Element div = createDiv();
        assertEquals("", div.id());
    }

    // Tests parent relationship after appendChild
    @Test
    public void testParent_returnsParent() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(child);
        assertEquals(parent, child.parent());
    }

    // Tests ancestors via parents()
    @Test
    public void testParents_returnsAncestors() {
        Element grandparent = createDiv();
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        grandparent.appendChild(parent);
        parent.appendChild(child);
        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertEquals(parent, parents.get(0));
        assertEquals(grandparent, parents.get(1));
    }

    // Tests children() returns only Element children
    @Test
    public void testChildren_returnsChildElements() {
        Element div = createDiv();
        Element p = div.appendElement("p");
        Element span = div.appendElement("span");
        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals(p, children.get(0));
        assertEquals(span, children.get(1));
    }

    // Tests child() by index
    @Test
    public void testChild_returnsElementAtIndex() {
        Element div = createDiv();
        Element p = div.appendElement("p");
        Element span = div.appendElement("span");
        assertEquals(p, div.child(0));
        assertEquals(span, div.child(1));
    }

    // Tests text() concatenates all text
    @Test
    public void testText_returnsCombinedText() {
        Element div = createDiv();
        div.appendChild(new TextNode("Hello ", "http://example.com"));
        div.appendElement("b").text("there");
        div.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello there now!", div.text());
    }

    // Tests ownText() returns direct text only
    @Test
    public void testOwnText_returnsDirectText() {
        Element div = createDiv();
        div.appendChild(new TextNode("Hello ", "http://example.com"));
        div.appendElement("b").text("there");
        div.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello  now!", div.ownText());
    }

    // Tests html() returns inner HTML
    @Test
    public void testHtml_returnsInnerHtml() {
        Element div = createDiv();
        div.appendChild(new TextNode("Hello", "http://example.com"));
        div.appendElement("b").text("World");
        String html = div.html();
        assertTrue(html.contains("Hello"));
        assertTrue(html.contains("<b>World</b>"));
    }

    // Tests html(String) sets inner HTML
    @Test
    public void testHtml_methodSetsInnerHtml() {
        Element div = createDiv();
        div.html("<p>New</p>");
        assertEquals("<p>New</p>", div.html());
    }

    // Tests appendChild adds node at end
    @Test
    public void testAppendChild_addsToEnd() {
        Element div = createDiv();
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        div.appendChild(p);
        assertEquals(1, div.children().size());
        assertEquals(p, div.child(0));
    }

    // Tests prependChild adds node at start
    @Test
    public void testPrependChild_addsToStart() {
        Element div = createDiv();
        Element p1 = div.appendElement("p");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        div.prependChild(span);
        assertEquals(span, div.child(0));
        assertEquals(p1, div.child(1));
    }

    // Tests before() creates sibling and does not affect target's inner HTML (potential defect)
    @Test
    public void testBefore_createsSiblingAndPreservesHtml() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        child.text("Hello");
        parent.appendChild(child);
        child.before("<span>Before</span>");
        assertEquals("Hello", child.text());
        assertEquals(2, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
    }

    // Tests after() creates sibling and preserves inner HTML (potential defect)
    @Test
    public void testAfter_createsSiblingAndPreservesHtml() {
        Element parent = createDiv();
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        child.text("Hello");
        parent.appendChild(child);
        child.after("<span>After</span>");
        assertEquals("Hello", child.text());
        assertEquals(2, parent.children().size());
        assertEquals("span", parent.child(1).tagName());
    }

    // Tests siblingElements returns all other element siblings
    @Test
    public void testSiblingElements_returnsSiblings() {
        Element parent = createDiv();
        Element p1 = parent.appendElement("p");
        Element p2 = parent.appendElement("p");
        Elements siblings = p1.siblingElements();
        assertEquals(1, siblings.size());
        assertEquals(p2, siblings.get(0));
    }

    // Tests nextElementSibling returns the next element
    @Test
    public void testNextElementSibling_returnsNext() {
        Element parent = createDiv();
        Element p1 = parent.appendElement("p");
        Element span = parent.appendElement("span");
        assertEquals(span, p1.nextElementSibling());
    }

    // Tests previousElementSibling returns the previous element
    @Test
    public void testPreviousElementSibling_returnsPrevious() {
        Element parent = createDiv();
        Element p1 = parent.appendElement("p");
        Element span = parent.appendElement("span");
        assertEquals(p1, span.previousElementSibling());
    }

    // Tests hasClass case insensitive matching
    @Test
    public void testHasClass_caseInsensitive() {
        Element div = createDiv();
        div.addClass("Foo");
        assertTrue(div.hasClass("foo"));
        assertTrue(div.hasClass("Foo"));
    }

    // Tests addClass adds new class
    @Test
    public void testAddClass_addsNewClass() {
        Element div = createDiv();
        div.addClass("test");
        assertTrue(div.hasClass("test"));
    }

    // Tests removeClass removes existing class
    @Test
    public void testRemoveClass_removesExistingClass() {
        Element div = createDiv();
        div.addClass("test");
        div.removeClass("test");
        assertFalse(div.hasClass("test"));
    }

    // Tests empty() removes all children
    @Test
    public void testEmpty_removesChildren() {
        Element div = createDiv();
        div.appendElement("p");
        div.empty();
        assertEquals(0, div.children().size());
    }

    // Tests exception on empty tag name
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyThrows() {
        Element div = createDiv();
        div.tagName("");
    }

    // Tests exception on null append
    @Test(expected = IllegalArgumentException.class)
    public void testAppend_nullThrows() {
        Element div = createDiv();
        div.append(null);
    }

    // Tests getElementById returns found element
    @Test
    public void testGetElementById_returnsFound() {
        Element div = createDiv();
        Element p = div.appendElement("p");
        p.attr("id", "testId");
        assertEquals(p, div.getElementById("testId"));
    }

    // Tests getElementById returns null when not found
    @Test
    public void testGetElementById_notFoundReturnsNull() {
        Element div = createDiv();
        assertNull(div.getElementById("nonexistent"));
    }
}