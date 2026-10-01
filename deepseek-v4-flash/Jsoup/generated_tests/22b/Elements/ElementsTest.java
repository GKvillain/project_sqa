package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElementsTest {

    // Helper: parse HTML and select with query
    private Elements select(String html, String query) {
        Document doc = Jsoup.parse(html);
        return doc.select(query);
    }

    // Tests attr(String) when attribute exists on first element
    @Test
    public void testAttr_exists_returnsAttributeValue() {
        Elements els = select("<p id='x'>One</p><p id='y'>Two</p>", "p");
        assertEquals("x", els.attr("id"));
    }

    // Tests attr(String) when no element has the attribute
    @Test
    public void testAttr_notExists_returnsEmptyString() {
        Elements els = select("<p>No attr</p>", "p");
        assertEquals("", els.attr("id"));
    }

    // Tests hasAttr returns true when attribute is present
    @Test
    public void testHasAttr_found_returnsTrue() {
        Elements els = select("<p id='a'>Text</p>", "p");
        assertTrue(els.hasAttr("id"));
    }

    // Tests hasAttr returns false when attribute is absent
    @Test
    public void testHasAttr_notFound_returnsFalse() {
        Elements els = select("<p>No attr</p>", "p");
        assertFalse(els.hasAttr("id"));
    }

    // Tests val() returns value of first input element
    @Test
    public void testVal_onNotEmptyList_returnsFirstElementValue() {
        Elements els = select("<input type='text' value='hello'>", "input");
        assertEquals("hello", els.val());
    }

    // Tests val() returns empty string for empty list
    @Test
    public void testVal_onEmptyList_returnsEmptyString() {
        Elements els = new Elements();
        assertEquals("", els.val());
    }

    // Tests text() joins texts with space (covers both branches of sb.length condition)
    @Test
    public void testText_multipleElements_concatenatedWithSpace() {
        Elements els = select("<p>First</p><p>Second</p>", "p");
        assertEquals("First Second", els.text());
    }

    // Tests text() on empty list
    @Test
    public void testText_emptyList_returnsEmptyString() {
        Elements els = new Elements();
        assertEquals("", els.text());
    }

    // Tests outerHtml() joins outer HTML with newline
    @Test
    public void testOuterHtml_multipleElements_concatenatedWithNewline() {
        Elements els = select("<p>One</p><p>Two</p>", "p");
        assertEquals("<p>One</p>\n<p>Two</p>", els.outerHtml());
    }

    // Tests eq(int) with valid index returns single-element list
    @Test
    public void testEq_indexWithinBounds_returnsElement() {
        Elements els = select("<p>A</p><p>B</p><p>C</p>", "p");
        Elements sub = els.eq(1);
        assertEquals(1, sub.size());
        assertEquals("B", sub.text());
    }

    // Tests eq(int) with out-of-bounds index returns empty list (includes negative)
    @Test
    public void testEq_indexOutOfBounds_returnsEmpty() {
        Elements els = select("<p>A</p><p>B</p>", "p");
        assertTrue(els.eq(2).isEmpty());
        assertTrue(els.eq(-1).isEmpty());
    }

    // Tests first() returns null for empty list
    @Test
    public void testFirst_empty_returnsNull() {
        Elements els = new Elements();
        assertNull(els.first());
    }

    // Tests last() returns null for empty list
    @Test
    public void testLast_empty_returnsNull() {
        Elements els = new Elements();
        assertNull(els.last());
    }

    // Tests is(String) returns true when at least one element matches
    @Test
    public void testIs_matchingQuery_returnsTrue() {
        Elements els = select("<p class='a'>One</p><p>Two</p>", "p");
        assertTrue(els.is(".a"));
    }

    // Tests is(String) returns false when no element matches
    @Test
    public void testIs_nonMatchingQuery_returnsFalse() {
        Elements els = select("<p>One</p>", "p");
        assertFalse(els.is("span"));
    }

    // Tests clone() produces deep copy independent from original
    @Test
    public void testClone_deepCopy_independent() {
        Document doc = Jsoup.parse("<p id='x'>Original</p>");
        Elements original = doc.select("p");
        Elements cloned = original.clone();
        cloned.attr("id", "y");
        cloned.html("Modified"); // fixed: text(String) does not exist, use html(String)
        assertEquals("x", original.attr("id"));
        assertEquals("Original", original.text());
    }

    // Tests wrap with empty HTML throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWrap_emptyHtml_throwsException() {
        Elements els = select("<p>Text</p>", "p");
        els.wrap("");
    }

    // Tests wrap with null HTML throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWrap_nullHtml_throwsException() {
        Elements els = select("<p>Text</p>", "p");
        els.wrap(null);
    }

    // Tests traverse with null NodeVisitor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTraverse_nullNodeVisitor_throwsException() {
        Elements els = select("<p>Text</p>", "p");
        els.traverse(null);
    }

    // Tests addClass adds class to all elements and covers hasClass true/false branches
    @Test
    public void testAddClass_classAddedToAll() {
        Elements els = select("<p>One</p><p>Two</p>", "p");
        // false branch of hasClass before adding
        assertFalse(els.hasClass("highlight"));
        els.addClass("highlight");
        for (Element e : els) {
            assertTrue(e.hasClass("highlight")); // true branch after adding
        }
    }
}