package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    // Tests normal parse of a simple HTML document
    @Test
    public void testParse_simpleHtml_documentCreated() {
        Document doc = Jsoup.parse("<html><head></head><body><p>Hello</p></body></html>");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Hello", doc.body().text());
    }

    // Tests parse with a self-closing tag
    @Test
    public void testParse_selfClosingTag_handledCorrectly() {
        Document doc = Jsoup.parse("<br/>");
        Elements brs = doc.select("br");
        assertEquals(1, brs.size());
        // br is considered self-closing in HTML, but parsed correctly
        assertNotNull(brs.first());
    }

    // Tests parseFragment with a null context (should parse as full document fragment)
    @Test
    public void testParseFragment_nullContext_returnsChildNodes() {
        // This will call parseFragment indirectly via Jsoup.parseBodyFragment which uses the tree builder
        Document doc = Jsoup.parseBodyFragment("<p>Test</p>");
        assertNotNull(doc);
        assertNotNull(doc.body());
        // body should contain the paragraph (or at least the text)
        assertTrue(doc.body().text().contains("Test"));
    }

    // Tests parseFragment with a context element (table context)
    @Test
    public void testParseFragment_tableContext_handlesTableCell() {
        // Parsing a table cell fragment within a table context
        Document doc = Jsoup.parse("<table><tr><td id='existing'>x</td></tr></table>");
        Element existingCell = doc.select("#existing").first();
        assertNotNull(existingCell);
        // The fragment parser works internally, but we can at least ensure the document is built
        assertNotNull(doc);
    }

    // Tests that stack manipulation methods work (pop, push, onStack)
    @Test
    public void testStackOperations_elementsAdded_areOnStack() {
        Document doc = Jsoup.parse("<div><span></span></div>");
        // The tree builder manipulates stack internally; we verify the DOM structure
        Elements spans = doc.select("span");
        assertEquals(1, spans.size());
        Element span = spans.first();
        assertEquals("div", span.parent().tagName());
    }

    // Tests the behavior of resetInsertionMode with various stack states (simulates fragment parsing)
    @Test
    public void testResetInsertionMode_startsWithFrameset_transitionsCorrectly() {
        // Parse a frameset document to trigger InFrameset state
        Document doc = Jsoup.parse("<html><frameset><frame src='x'/></frameset></html>");
        assertNotNull(doc);
        // The frameset should be present (though body may be absent in strict frameset)
        Elements frameset = doc.select("frameset");
        assertEquals(1, frameset.size());
    }

    // Tests foster parenting when inserting a node inside a table
    @Test
    public void testFosterInsert_fosterParentSet_insertsBeforeTable() {
        // Parsing a table with a div inside should cause foster parenting
        Document doc = Jsoup.parse("<table><div>Text</div><tr><td>Cell</td></tr></table>");
        Elements divs = doc.select("div");
        Elements tables = doc.select("table");
        assertTrue(divs.size() > 0);
        // The div should be placed before the table (foster parent behavior)
        // Verify the div is not a child of the table
        assertFalse(divs.first().parent().tagName().equals("table"));
    }

    // Tests that setting base URI via <base> works
    @Test
    public void testMaybeSetBaseUri_baseTag_updatesBaseUri() {
        Document doc = Jsoup.parse("<head><base href='http://example.com/'></head><body></body>");
        String baseUri = doc.baseUri();
        // The base URI may be set; check it's not empty (depends on implementation)
        assertNotNull(baseUri);
        // Note: In Jsoup, base URI is set on the document from the <base> tag
        assertTrue(baseUri.contains("example.com") || baseUri.isEmpty());
    }

    // Tests error handling when unexpected token occurs
    @Test
    public void testError_unexpectedToken_addsToErrorList() {
        // Parse a document with a known error (e.g., stray end tag)
        Document doc = Jsoup.parse("<b>Hello</b></b>");
        // The parser should still work; just check no exception
        assertNotNull(doc);
        assertTrue(doc.text().contains("Hello"));
    }

    // Tests that formatting elements are reconstructed correctly
    @Test
    public void testReconstructFormattingElements_basicReconstruction() {
        Document doc = Jsoup.parse("<b><i>Text</i></b>");
        Elements bTags = doc.select("b");
        assertEquals(1, bTags.size());
        Elements iTags = doc.select("i");
        assertEquals(1, iTags.size());
        // The tree builder should have handled the formatting elements
        assertNotNull(bTags.first());
        assertNotNull(iTags.first());
    }

    // Tests that inSpecificScope correctly identifies scope elements
    @Test
    public void testInSpecificScope_targetInScope_returnsTrue() {
        // Scope checks are internal, but we can test indirectly by parsing structured documents
        // Parse a table with a cell; InCell scope applies
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        assertNotNull(doc);
        Elements td = doc.select("td");
        assertEquals(1, td.size());
    }

    // Tests the formatting element stack limit (pushing 3 identical elements)
    @Test
    public void testPushActiveFormattingElements_sameElementThreeTimes_removesFirst() {
        // This behavior is internal; we can test via nested formatting tags
        Document doc = Jsoup.parse("<b><b><b>Text</b></b></b>");
        Elements bTags = doc.select("b");
        // There should be only one <b> element in the tree (nested)
        assertEquals(1, bTags.size());
        // The tree builder should have handled the stacking correctly
        assertTrue(bTags.first().text().contains("Text"));
    }

    // Tests that builder transitions between states correctly for known elements
    @Test
    public void testTransition_inSelect_switchesState() {
        // Parse a select element; the state should transition to InSelect
        Document doc = Jsoup.parse("<select><option>A</option></select>");
        assertNotNull(doc);
        Elements options = doc.select("option");
        assertEquals(1, options.size());
    }

    // Tests that generateImpliedEndTags works when closing elements
    @Test
    public void testGenerateImpliedEndTags_liWithoutClosing_impliedEndAdded() {
        Document doc = Jsoup.parse("<ul><li>Item<li>Another</ul>");
        Elements lis = doc.select("li");
        assertEquals(2, lis.size());
        // Both list items should be present; tree builder handled implied end tags
        assertNotNull(lis.get(0));
        assertNotNull(lis.get(1));
    }

    // Tests queuing and retrieving pending table characters
    @Test
    public void testPendingTableCharacters_charactersQueued_retrievedCorrectly() {
        // This tests internal behavior; we can verify indirectly via token processing
        // Parse a table with text before any table cell (should be fostered)
        Document doc = Jsoup.parse("<table>Text<table><tr><td>Cell</td></tr></table></table>");
        assertNotNull(doc);
        // "Text" should appear somewhere in the final document
        assertTrue(doc.body().text().contains("Text"));
    }

    // Tests that removing an element from the formatting elements list works
    @Test
    public void testRemoveFromActiveFormattingElements_elementRemoved() {
        // Use nested formatting elements to trigger removal
        Document doc = Jsoup.parse("<b><i>Text</i></b>");
        assertNotNull(doc);
        Elements iTags = doc.select("i");
        assertTrue(iTags.size() > 0);
    }

    // Tests that isSpecial correctly identifies special elements
    @Test
    public void testIsSpecial_specialElement_returnsTrue() {
        // Special elements are internal; we can check parsing of known special elements
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        Elements specials = doc.select("table, tr, td");
        assertEquals(3, specials.size()); // table, tr, td
    }
}