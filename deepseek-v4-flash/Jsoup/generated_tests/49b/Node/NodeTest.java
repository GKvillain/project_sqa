package org.jsoup.nodes;

import org.jsoup.parser.Parser;
import org.jsoup.helper.Validate;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for Node (Defects4J bug 49b).
 * Covers key methods, branches, and edge cases.
 */
public class NodeTest {

    // Helper: parse a HTML fragment to a Document (root Element)
    private Element parse(String html) {
        return Parser.parse(html);
    }

    // ---- attr(String) ----

    @Test
    // Normal case: existing attribute returns its value
    public void testAttr_existingKey_returnsValue() {
        Element el = parse("<a href='http://example.com'>link</a>");
        assertEquals("http://example.com", el.attr("href"));
    }

    @Test
    // Edge: missing attribute returns empty string
    public void testAttr_missingKey_returnsEmpty() {
        Element el = parse("<a>link</a>");
        assertEquals("", el.attr("nonexistent"));
    }

    @Test
    // Normal: abs: prefix delegates to absUrl
    public void testAttr_absPrefix_usesAbsUrl() {
        Element el = parse("<a href='/path'>link</a>");
        el.setBaseUri("http://base.com");
        assertEquals("http://base.com/path", el.attr("abs:href"));
    }

    @Test(expected = NullPointerException.class)
    // Invalid: null attribute key throws NullPointerException
    public void testAttr_nullKey_throwsException() {
        Element el = parse("<a>link</a>");
        el.attr(null);
    }

    // ---- hasAttr(String) ----

    @Test
    // Normal: existing attribute returns true
    public void testHasAttr_existingKey_returnsTrue() {
        Element el = parse("<a href='x'>link</a>");
        assertTrue(el.hasAttr("href"));
    }

    @Test
    // Branch: abs: prefix with existing attribute and resolvable absolute URL returns true
    public void testHasAttr_absPrefixResolvable_returnsTrue() {
        Element el = parse("<a href='/p'>link</a>");
        el.setBaseUri("http://b.com");
        assertTrue(el.hasAttr("abs:href"));
    }

    @Test
    // Branch: abs: prefix with missing attribute returns false
    public void testHasAttr_absPrefixMissingKey_returnsFalse() {
        Element el = parse("<a>link</a>");
        assertFalse(el.hasAttr("abs:missing"));
    }

    @Test(expected = NullPointerException.class)
    // Edge: null key throws NullPointerException
    public void testHasAttr_nullKey_throwsException() {
        parse("<a>link</a>").hasAttr(null);
    }

    // ---- absUrl(String) ----

    @Test
    // Normal: relative attribute with base URI resolves to absolute
    public void testAbsUrl_relativeAttr_returnsAbsolute() {
        Element el = parse("<a href='/rel'>link</a>");
        el.setBaseUri("http://example.com");
        assertEquals("http://example.com/rel", el.absUrl("href"));
    }

    @Test
    // Edge: missing attribute returns empty string
    public void testAbsUrl_missingAttr_returnsEmpty() {
        Element el = parse("<a>link</a>");
        assertEquals("", el.absUrl("href"));
    }

    // ---- setBaseUri(String) ----

    @Test
    // Normal: updates base URI of node and descendants
    public void testSetBaseUri_updatesDescendants() {
        Element div = parse("<div><a href='/a'>A</a></div>");
        div.setBaseUri("http://root.com");
        Element a = div.child(0);
        assertEquals("http://root.com/a", a.absUrl("href"));
    }

    // ---- childNode(int) ----

    @Test
    // Normal: positive index returns correct child
    public void testChildNode_validIndex_returnsNode() {
        Element div = parse("<div><span>1</span><b>2</b></div>");
        assertEquals("span", div.childNode(0).nodeName());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    // Boundary: negative index throws exception
    public void testChildNode_negativeIndex_throwsException() {
        Element div = parse("<div><span></span></div>");
        div.childNode(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    // Boundary: index equals size throws exception
    public void testChildNode_indexEqualsSize_throwsException() {
        Element div = parse("<div><span></span></div>");
        div.childNode(1);
    }

    // ---- childNodesCopy() ----

    @Test
    // Normal: deep copy is independent of original children
    public void testChildNodesCopy_deepCopyIndependent() {
        Element div = parse("<div><span>text</span></div>");
        List<Node> copy = div.childNodesCopy();
        assertEquals(1, copy.size());
        // Modify child in copy
        ((Element)copy.get(0)).attr("data-x", "modified");
        // Original should not have the attribute
        assertFalse(div.childNode(0).hasAttr("data-x"));
    }

    // ---- remove() ----

    @Test
    // Normal: removes node from parent
    public void testRemove_removesFromParent() {
        Element div = parse("<div><span>child</span></div>");
        Node child = div.childNode(0);
        child.remove();
        assertEquals(0, div.childNodeSize());
    }

    @Test(expected = NullPointerException.class)
    // Edge: remove on orphan node (no parent) throws NullPointerException
    public void testRemove_orphanNode_throwsException() {
        Element orphan = parse("<p></p>");
        orphan.remove(); // parent is Document, not null – need true orphan
        // Create a node without parent by cloning? Let's use TextNode
        TextNode tn = new TextNode("text", "");
        tn.remove(); // parent should be null
    }

    // ---- before(String) ----

    @Test
    // Normal: inserts HTML before current node
    public void testBefore_html_insertsPrecedingSibling() {
        Element div = parse("<div><span>mid</span></div>");
        Node span = div.childNode(0);
        span.before("<b>before</b>");
        assertEquals("b", div.childNode(0).nodeName());
        assertEquals("span", div.childNode(1).nodeName());
    }

    // ---- wrap(String) ----

    @Test
    // Normal: wraps node with provided HTML
    public void testWrap_validHtml_wrapsNode() {
        Element div = parse("<div><span>inner</span></div>");
        Node span = div.childNode(0);
        span.wrap("<b></b>");
        assertEquals("b", div.childNode(0).nodeName());
        assertEquals("span", div.childNode(0).childNode(0).nodeName());
    }

    // ---- unwrap() ----

    @Test
    // Normal: unwrap promotes children
    public void testUnwrap_removesNodeKeepsChildren() {
        Element div = parse("<div><p><span>text</span></p></div>");
        Node p = div.childNode(0);
        p.unwrap();
        assertEquals(1, div.childNodeSize());
        assertEquals("span", div.childNode(0).nodeName());
    }

    // ---- parent() / parentNode() ----

    @Test
    // Normal: parent returns correct node
    public void testParent_returnsParentNode() {
        Element div = parse("<div><span>child</span></div>");
        Node child = div.childNode(0);
        assertSame(div, child.parent());
    }

    // ---- siblingNodes() ----

    @Test
    // Normal: returns all siblings except self
    public void testSiblingNodes_excludesSelf() {
        Element div = parse("<div><a>1</a><b>2</b><c>3</c></div>");
        Node b = div.childNode(1);
        List<Node> siblings = b.siblingNodes();
        assertEquals(2, siblings.size());
        assertEquals("a", siblings.get(0).nodeName());
        assertEquals("c", siblings.get(1).nodeName());
    }

    // ---- clone() ----

    @Test
    // Normal: cloned node has no parent and deep copy is independent
    public void testClone_deepCopyOrphan() {
        Element div = parse("<div><span>text</span></div>");
        Element clone = div.clone();
        assertNull(clone.parent());
        assertEquals("div", clone.nodeName());
        // Modify clone child, original unchanged
        clone.childNode(0).attr("data", "cloned");
        assertFalse(div.childNode(0).hasAttr("data"));
    }

    // ---- equals() & hashCode() ----

    @Test
    // Normal: two nodes with same content are equal
    public void testEquals_sameContent_returnsTrue() {
        Element e1 = parse("<a href='x'>link</a>");
        Element e2 = parse("<a href='x'>link</a>");
        assertTrue(e1.equals(e2));
    }

    @Test
    // Normal: different attributes not equal
    public void testEquals_differentAttributes_returnsFalse() {
        Element e1 = parse("<a href='x'>link</a>");
        Element e2 = parse("<a href='y'>link</a>");
        assertFalse(e1.equals(e2));
    }

    @Test
    // Normal: hash codes agree with equals
    public void testHashCode_equalNodes_sameHash() {
        Element e1 = parse("<a href='z'>text</a>");
        Element e2 = parse("<a href='z'>text</a>");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    // ---- outerHtml() ----

    @Test
    // Normal: produces correct HTML string
    public void testOuterHtml_simpleElement_returnsHtml() {
        Element div = parse("<div class='test'>Hello</div>");
        assertEquals("<div class=\"test\">\n Hello\n</div>", div.outerHtml());
    }
}