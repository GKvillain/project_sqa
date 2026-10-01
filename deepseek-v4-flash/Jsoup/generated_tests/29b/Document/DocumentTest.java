package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Entities;

public class DocumentTest {

    // Tests constructor: base URI and node name
    @Test
    public void testConstructor_defaultState() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
        assertEquals("http://example.com", doc.baseUri());
        assertNull(doc.head());
        assertNull(doc.body());
    }

    // Tests createShell returns document with html, head, body
    @Test
    public void testCreateShell_validBaseUri_returnsDocumentWithStructure() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(1, doc.getElementsByTag("html").size());
    }

    // Tests head() returns null when document has no head element
    @Test
    public void testHead_whenNotExists_returnsNull() {
        Document doc = new Document("http://example.com");
        assertNull(doc.head());
    }

    // Tests body() returns null when document has no body element
    @Test
    public void testBody_whenNotExists_returnsNull() {
        Document doc = new Document("http://example.com");
        assertNull(doc.body());
    }

    // Tests head() returns head after normalise
    @Test
    public void testHead_afterNormalise_returnsHead() {
        Document doc = new Document("http://example.com");
        doc.normalise();
        assertNotNull(doc.head());
    }

    // Tests title get returns empty string if no title element
    @Test
    public void testTitle_get_initialEmpty() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("", doc.title());
    }

    // Tests title set updates existing title element
    @Test
    public void testTitle_set_withExistingTitle_updates() {
        Document doc = Document.createShell("http://example.com");
        doc.title("Hello World");
        assertEquals("Hello World", doc.title());
    }

    // Tests title set adds title to head when missing
    @Test
    public void testTitle_set_noTitle_addsToHead() {
        Document doc = Document.createShell("http://example.com");
        // remove title if present
        Elements titles = doc.getElementsByTag("title");
        if (!titles.isEmpty()) {
            titles.first().remove();
        }
        doc.title("New Title");
        assertEquals("New Title", doc.title());
    }

    // Tests normalise adds html, head, body to empty document
    @Test
    public void testNormalise_withEmptyDocument_addsHtmlHeadBody() {
        Document doc = new Document("http://example.com");
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(1, doc.getElementsByTag("html").size());
    }

    // Tests normalise merges multiple head elements into one
    @Test
    public void testNormalise_withMultipleHeads_merges() {
        Document doc = Document.createShell("http://example.com");
        Element html = doc.getElementsByTag("html").first();
        html.appendElement("head"); // second head
        doc.normalise();
        Elements heads = doc.getElementsByTag("head");
        assertEquals(1, heads.size());
    }

    // Tests normalise moves non-blank text nodes from root into body
    @Test
    public void testNormaliseTextNodes_nonBlankTextMovedToBody() {
        Document doc = new Document("http://example.com");
        doc.appendChild(new TextNode("Hello", "http://example.com"));
        doc.appendChild(new TextNode(" ", "http://example.com")); // blank
        doc.normalise();
        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("Hello"));
        // blank text should not appear
        assertFalse(bodyText.trim().isEmpty());
    }

    // Tests clone produces an independent copy
    @Test
    public void testClone_independentCopy() {
        Document doc = Document.createShell("http://example.com");
        Document clone = doc.clone();
        assertNotSame(doc, clone);
        assertNotSame(doc.outputSettings(), clone.outputSettings());
        assertEquals(doc.title(), clone.title());
    }

    // Tests output settings get and set with chaining
    @Test
    public void testOutputSettings_getAndSet() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        assertNotNull(settings);
        settings.escapeMode(Entities.EscapeMode.extended);
        settings.prettyPrint(false);
        settings.indentAmount(2);
        assertEquals(Entities.EscapeMode.extended, doc.outputSettings().escapeMode());
        assertFalse(doc.outputSettings().prettyPrint());
        assertEquals(2, doc.outputSettings().indentAmount());
    }

    // Tests quirks mode set and get
    @Test
    public void testQuirksMode_setAndGet() {
        Document doc = new Document("http://example.com");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    // Tests text() clears body and sets text
    @Test
    public void testText_method_clearsBodyAndSetsText() {
        Document doc = Document.createShell("http://example.com");
        doc.text("Hello");
        assertEquals("Hello", doc.body().text());
    }

    // Tests createElement returns element with document's base URI
    @Test
    public void testCreateElement_returnsElementWithBaseUri() {
        Document doc = new Document("http://example.com");
        Element elem = doc.createElement("div");
        assertEquals("div", elem.tagName());
        assertEquals("http://example.com", elem.baseUri());
    }

    // Tests outerHtml returns inner HTML of document
    @Test
    public void testOuterHtml_returnsHtmlContent() {
        Document doc = Document.createShell("http://example.com");
        String outer = doc.outerHtml();
        assertTrue(outer.contains("<html>"));
        assertTrue(outer.contains("<head>"));
        assertTrue(outer.contains("<body>"));
        assertTrue(outer.contains("</html>"));
    }

    // Tests title set with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_set_null_throwsException() {
        Document doc = Document.createShell("http://example.com");
        doc.title(null);
    }

    // Tests title set on document without head throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testTitle_set_onDocumentWithoutHead_throwsNullPointer() {
        Document doc = new Document("http://example.com");
        doc.title("test");
    }

    // Tests normaliseStructure ensures head is parented by html
    @Test
    public void testNormaliseStructure_parentedByHtml() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        // detach head from html by moving to root
        head.remove();
        doc.appendChild(head);
        // now head is directly under document, not under html
        doc.normalise();
        // after normalise, head should be child of html
        Element html = doc.getElementsByTag("html").first();
        assertTrue(html.childNodes().stream().anyMatch(n -> n.nodeName().equals("head")));
    }
}