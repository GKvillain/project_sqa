package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import java.util.List;

public class NodeTest {

    // --- Helper methods ---
    private Document createDoc(String baseUri) {
        return new Document(baseUri);
    }

    private Element appendChildToDoc(Document doc, String tag) {
        Element el = doc.createElement(tag);
        doc.appendChild(el);
        return el;
    }

    // --- Tests for attr(String) ---
    @Test
    // Normal case: existing attribute returns its value
    public void testAttr_existingKey_returnsValue() {
        Element el = new Element(Tag.valueOf("a"), "").attr("href", "http://example.com");
        assertEquals("http://example.com", el.attr("href"));
    }

    @Test
    // Normal case: non-existing attribute returns empty string
    public void testAttr_nonExistingKey_returnsEmpty() {
        Element el = new Element(Tag.valueOf("a"), "");
        assertEquals("", el.attr("href"));
    }

    @Test
    // Edge case: attribute key with "abs:" prefix delegates to absUrl
    public void testAttr_absPrefix_callsAbsUrl() {
        Document doc = createDoc("http://base.com");
        Element el = doc.createElement("a");
        el.attr("href", "page.html");
        doc.appendChild(el);
        assertEquals("http://base.com/page.html", el.attr("abs:href"));
    }

    // --- Tests for absUrl(String) ---
    @Test
    // Normal case: absolute URL returns as is
    public void testAbsUrl_absoluteUrl_returnsItself() {
        Document doc = createDoc("http://base.com");
        Element el = doc.createElement("a");
        el.attr("href", "http://other.com/path");
        doc.appendChild(el);
        assertEquals("http://other.com/path", el.absUrl("href"));
    }

    @Test
    // Normal case: relative URL with valid base returns absolute
    public void testAbsUrl_relativeUrlWithBase_returnsAbsolute() {
        Document doc = createDoc("http://base.com/");
        Element el = doc.createElement("a");
        el.attr("href", "page.html");
        doc.appendChild(el);
        assertEquals("http://base.com/page.html", el.absUrl("href"));
    }

    @Test
    // Branch: malformed base URL but attribute is absolute -> return absolute
    public void testAbsUrl_malformedBase_relUrlAbsolute_returnsAbsolute() {
        Document doc = createDoc("not-a-url");
        Element el = doc.createElement("p");
        el.attr("src", "http://valid.com/img.png");
        doc.appendChild(el);
        assertEquals("http://valid.com/img.png", el.absUrl("src"));
    }

    @Test
    // Branch: malformed base URL and relative attribute -> return empty
    public void testAbsUrl_malformedBase_relUrlRelative_returnsEmpty() {
        Document doc = createDoc("not-a-url");
        Element el = doc.createElement("p");
        el.attr("src", "img.png");
        doc.appendChild(el);
        assertEquals("", el.absUrl("src"));
    }

    @Test
    // Branch: query string workaround (? at start)
    public void testAbsUrl_queryStringWorkaround() {
        Document doc = createDoc("http://base.com/path/file.html");
        Element el = doc.createElement("a");
        el.attr("href", "?foo=bar");
        doc.appendChild(el);
        assertEquals("http://base.com/path/file.html?foo=bar", el.absUrl("href"));
    }

    @Test
    // Edge case: missing attribute -> return empty
    public void testAbsUrl_missingAttribute_returnsEmpty() {
        Document doc = createDoc("http://base.com");
        Element el = doc.createElement("a");
        doc.appendChild(el);
        assertEquals("", el.absUrl("href"));
    }

    // --- Tests for remove() ---
    @Test
    // Normal case: remove node from parent
    public void testRemove_normal_removesNode() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element child = doc.createElement("span");
        parent.appendChild(child);
        doc.appendChild(parent);
        child.remove();
        assertFalse(parent.childNodes().contains(child));
        assertNull(child.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    // Edge case: remove node with no parent throws
    public void testRemove_noParent_throwsException() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        orphan.remove();
    }

    // --- Tests for before(String) ---
    @Test
    // Normal case: insert HTML before node
    public void testBeforeString_validHtml_insertsBefore() {
        Document doc = createDoc("");
        Element parent = doc.createElement("ul");
        Element item = new Element(Tag.valueOf("li"), "").attr("class", "second");
        parent.appendChild(item);
        doc.appendChild(parent);
        item.before("<li class='first'>First</li>");
        List<Node> children = parent.childNodes();
        assertEquals(2, children.size());
        assertEquals("li", children.get(0).nodeName());
        assertEquals("first", ((Element)children.get(0)).attr("class"));
    }

    // --- Tests for before(Node) ---
    @Test
    // Normal case: insert node before another node
    public void testBeforeNode_validNode_insertsBefore() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element existing = new Element(Tag.valueOf("p"), "");
        Element newbie = new Element(Tag.valueOf("h1"), "");
        parent.appendChild(existing);
        doc.appendChild(parent);
        existing.before(newbie);
        List<Node> siblings = parent.childNodes();
        assertEquals(2, siblings.size());
        assertSame(newbie, siblings.get(0));
        assertSame(existing, siblings.get(1));
    }

    // --- Tests for after(String) ---
    @Test
    // Normal case: insert HTML after node
    public void testAfterString_insertsAfter() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element first = new Element(Tag.valueOf("p"), "");
        parent.appendChild(first);
        doc.appendChild(parent);
        first.after("<p>second</p>");
        List<Node> children = parent.childNodes();
        assertEquals(2, children.size());
        assertEquals("p", children.get(1).nodeName());
    }

    // --- Tests for siblingNodes(), nextSibling(), previousSibling() ---
    @Test
    // Normal case: siblingNodes returns all siblings including self
    public void testSiblingNodes_returnsAllSiblings() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        Element c = new Element(Tag.valueOf("c"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        doc.appendChild(parent);
        List<Node> siblings = b.siblingNodes();
        assertEquals(3, siblings.size());
        assertTrue(siblings.contains(a));
        assertTrue(siblings.contains(b));
        assertTrue(siblings.contains(c));
    }

    @Test
    // Normal case: nextSibling for middle node returns next
    public void testNextSibling_middle_returnsNext() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        Element c = new Element(Tag.valueOf("c"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        doc.appendChild(parent);
        assertSame(b, a.nextSibling());
        assertSame(c, b.nextSibling());
    }

    @Test
    // Boundary case: last sibling returns null
    public void testNextSibling_last_returnsNull() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element a = new Element(Tag.valueOf("a"), "");
        parent.appendChild(a);
        doc.appendChild(parent);
        assertNull(a.nextSibling());
    }

    @Test
    // Normal case: previousSibling for middle node returns previous
    public void testPreviousSibling_middle_returnsPrevious() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        Element c = new Element(Tag.valueOf("c"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        doc.appendChild(parent);
        assertSame(a, b.previousSibling());
        assertSame(b, c.previousSibling());
    }

    @Test
    // Boundary case: first sibling returns null
    public void testPreviousSibling_first_returnsNull() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element a = new Element(Tag.valueOf("a"), "");
        parent.appendChild(a);
        doc.appendChild(parent);
        assertNull(a.previousSibling());
    }

    // --- Tests for wrap(String) ---
    @Test
    // Normal case: wrap with valid HTML
    public void testWrap_validHtml_wrapsElement() {
        Document doc = createDoc("");
        Element target = new Element(Tag.valueOf("span"), "");
        doc.appendChild(target);
        target.wrap("<div></div>");
        Element parent = (Element) target.parent();
        assertEquals("div", parent.nodeName());
        assertSame(target, parent.childNodes().get(0));
    }

    @Test
    // Edge case: wrap with HTML that has no element as first child -> returns null
    public void testWrap_invalidHtml_returnsNull() {
        Document doc = createDoc("");
        Element target = new Element(Tag.valueOf("span"), "");
        doc.appendChild(target);
        Node result = target.wrap("some text");
        assertNull(result);
    }

    // --- Tests for clone() ---
    @Test
    // Normal case: clone creates deep copy with no parent
    public void testClone_deepCopy_parentNull() {
        Document doc = createDoc("http://base.com");
        Element original = new Element(Tag.valueOf("div"), "");
        original.attr("id", "1");
        Element child = new Element(Tag.valueOf("span"), "");
        original.appendChild(child);
        doc.appendChild(original);
        Node cloned = original.clone();
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertNull(cloned.parent());
        assertTrue(cloned instanceof Element);
        Element clonedEl = (Element) cloned;
        assertEquals("div", clonedEl.nodeName());
        assertEquals("1", clonedEl.attr("id"));
        assertEquals(1, clonedEl.childNodes().size());
        assertNotSame(child, clonedEl.childNodes().get(0));
    }

    // --- Tests for ownerDocument() ---
    @Test
    // Normal case: node with document returns that document
    public void testOwnerDocument_nodeWithParent_returnsDocument() {
        Document doc = createDoc("");
        Element el = doc.createElement("div");
        doc.appendChild(el);
        assertSame(doc, el.ownerDocument());
    }

    @Test
    // Edge case: orphan node returns null
    public void testOwnerDocument_orphanNode_returnsNull() {
        Element orphan = new Element(Tag.valueOf("p"), "");
        assertNull(orphan.ownerDocument());
    }

    // --- Tests for parent() ---
    @Test
    // Normal case: parent returns correct parent
    public void testParent_returnsParent() {
        Document doc = createDoc("");
        Element parent = doc.createElement("div");
        Element child = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child);
        doc.appendChild(parent);
        assertSame(parent, child.parent());
    }

    // --- Tests for childNode(int) ---
    @Test(expected = IndexOutOfBoundsException.class)
    // Edge case: childNode with invalid index throws
    public void testChildNode_invalidIndex_throwsIndexOutOfBounds() {
        Document doc = createDoc("");
        Element parent = new Element(Tag.valueOf("div"), "");
        doc.appendChild(parent);
        parent.childNode(5); // no children
    }
}