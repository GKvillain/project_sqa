package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;
import org.jsoup.parser.Tag;

/**
 * JUnit 4 test class for org.jsoup.nodes.Element, covering key behaviors
 * and specifically the clone bug (Defects4J 66b).
 */
public class ElementTest {

    // ---------- Constructor tests ----------

    @Test
    public void testConstructor_StringTag_setsCorrectTagName() {
        Element e = new Element("div");
        assertEquals("div", e.tagName());
    }

    @Test
    public void testConstructor_TagBaseUri_setsBaseUriAndTag() {
        Element e = new Element(Tag.valueOf("p"), "http://example.com");
        assertEquals("p", e.tagName());
        assertEquals("http://example.com", e.baseUri());
    }

    // ---------- Child manipulation tests ----------

    @Test
    public void testAppendChild_addsChildAndUpdatesSiblingIndex() {
        Element parent = new Element("ul");
        Element child = new Element("li");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());
        assertEquals(0, child.siblingIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendChild_nullInput_throwsException() {
        new Element("div").appendChild(null);
    }

    @Test
    public void testPrependChild_addsChildAtStart() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        Element first = new Element("b");
        parent.prependChild(first);
        assertEquals(first, parent.child(0));
    }

    @Test
    public void testEmpty_clearsChildren() {
        Element e = new Element("div");
        e.appendChild(new Element("span"));
        e.text("hello");
        e.empty();
        assertEquals(0, e.childNodeSize());
    }

    // ---------- Text extraction tests ----------

    @Test
    public void testText_returnsCombinedTextWithNormalization() {
        Element p = new Element("p");
        p.appendChild(new TextNode("Hello "));
        Element b = new Element("b");
        b.appendChild(new TextNode("there"));
        p.appendChild(b);
        p.appendChild(new TextNode(" now!"));
        assertEquals("Hello there now!", p.text());
    }

    @Test
    public void testOwnText_returnsDirectTextOnly() {
        Element p = new Element("p");
        p.appendChild(new TextNode("Hello "));
        p.appendChild(new Element("b").appendText("there"));
        p.appendChild(new TextNode(" now!"));
        assertEquals("Hello now!", p.ownText());
    }

    // ---------- hasClass tests (performance sensitive) ----------

    @Test
    public void testHasClass_exactMatch_returnsTrue() {
        Element e = new Element("div").attr("class", "header");
        assertTrue(e.hasClass("header"));
    }

    @Test
    public void testHasClass_partialMatch_returnsTrue() {
        Element e = new Element("div").attr("class", "header main");
        assertTrue(e.hasClass("main"));
    }

    @Test
    public void testHasClass_noMatch_returnsFalse() {
        Element e = new Element("div").attr("class", "header");
        assertFalse(e.hasClass("footer"));
    }

    @Test
    public void testHasClass_emptyAttribute_returnsFalse() {
        Element e = new Element("div");
        assertFalse(e.hasClass("anything"));
    }

    // ---------- Clone tests (targeting Defects4J 66b) ----------

    @Test
    public void testClone_deepCopyChildNodes_shouldBeIndependent() {
        Element parent = new Element("div");
        parent.appendChild(new TextNode("original"));
        Element clone = parent.clone();

        // modify original by adding a child
        parent.appendChild(new Element("span"));

        // clone should still have only the original child
        assertEquals(1, clone.childNodeSize());
        assertEquals("original", clone.text());
    }

    @Test
    public void testClone_attributesCopiedIndependently() {
        Element e = new Element("a").attr("href", "http://example.com");
        Element clone = e.clone();
        e.attr("href", "http://other.com");
        assertEquals("http://example.com", clone.attr("href"));
    }

    @Test
    public void testClone_baseUriPreserved() {
        Element e = new Element(Tag.valueOf("div"), "http://original.com");
        Element clone = e.clone();
        assertEquals("http://original.com", clone.baseUri());
    }

    // ---------- HTML output tests ----------

    @Test
    public void testHtml_returnsInnerHtml() {
        Element e = new Element("div");
        e.appendChild(new Element("p").appendText("text"));
        assertEquals("<p>text</p>", e.html());
    }

    @Test
    public void testOuterHtml_selfClosingTag_doesNotIncludeEndTag() {
        Element br = new Element("br");
        // In html syntax, self-closing tags like <br> should not have end tag
        String outer = br.outerHtml();
        assertFalse(outer.contains("</br>"));
        assertTrue(outer.contains("<br>"));
    }

    // ---------- Insert children ----------

    @Test
    public void testInsertChildren_atEnd_works() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        Element b = new Element("b");
        parent.insertChildren(1, b);
        assertEquals(b, parent.child(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_nullCollection_throwsException() {
        new Element("div").insertChildren(0, (List<Element>) null);
    }

    // ---------- Select / find tests ----------

    @Test
    public void testSelectFirst_returnsFirstMatch() {
        Element root = new Element("html");
        Element div = new Element("div").attr("id", "mydiv");
        root.appendChild(div);
        Element result = root.selectFirst("#mydiv");
        assertNotNull(result);
        assertEquals(div, result);
    }

    @Test
    public void testIs_matchingSelector_returnsTrue() {
        Element e = new Element("div").attr("class", "foo");
        assertTrue(e.is(".foo"));
    }

    @Test
    public void testIs_nonMatchingSelector_returnsFalse() {
        Element e = new Element("div").attr("class", "foo");
        assertFalse(e.is(".bar"));
    }

    // ---------- hasText ----------

    @Test
    public void testHasText_withText_returnsTrue() {
        Element e = new Element("p").appendText("a");
        assertTrue(e.hasText());
    }

    @Test
    public void testHasText_blankText_returnsFalse() {
        Element e = new Element("p").appendText("   ");
        assertFalse(e.hasText());
    }

    @Test
    public void testHasText_noChildren_returnsFalse() {
        Element e = new Element("p");
        assertFalse(e.hasText());
    }

    // ---------- Miscellaneous ----------

    @Test
    public void testTagName_changesTag() {
        Element e = new Element("span");
        e.tagName("div");
        assertEquals("div", e.tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagName_empty_throwsException() {
        new Element("span").tagName("");
    }
}