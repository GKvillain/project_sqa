package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for org.jsoup.nodes.Document.
 * Designed to detect Defects4J bug 1b and achieve reasonable coverage.
 */
public class DocumentTest {

    // Test constructor sets base URI correctly
    @Test
    public void testConstructor_validBaseUri_setsBaseUri() {
        Document doc = new Document("http://example.com");
        assertEquals("http://example.com", doc.baseUri());
    }

    // Test createShell creates valid document with html, head, body
    @Test
    public void testCreateShell_validBaseUri_createsHtmlHeadBody() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.getElementsByTag("html").first().tagName());
        assertEquals("head", doc.head().tagName());
        assertEquals("body", doc.body().tagName());
    }

    // Test head() returns null on document without head element
    @Test
    public void testHead_documentWithoutHead_returnsNull() {
        Document doc = new Document("http://example.com");
        assertNull(doc.head());
    }

    // Test body() returns null on document without body element
    @Test
    public void testBody_documentWithoutBody_returnsNull() {
        Document doc = new Document("http://example.com");
        assertNull(doc.body());
    }

    // Test title() returns empty string when no title element
    @Test
    public void testTitle_getTitleWithoutTitleElement_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("", doc.title());
    }

    // Test title(String) when head already has a title element (update)
    @Test
    public void testTitle_setTitleWhenTitleExists_updatesTitle() {
        Document doc = Document.createShell("http://example.com");
        doc.title("Old");
        doc.title("New");
        assertEquals("New", doc.title());
    }

    // Test title(String) when head exists but no title element (add)
    @Test
    public void testTitle_setTitleWhenHeadExistsAndNoTitle_addsTitle() {
        Document doc = Document.createShell("http://example.com");
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertNotNull(doc.getElementsByTag("title").first());
    }

    // Test title(String) with leading/trailing whitespace (trims output)
    @Test
    public void testTitle_setTitleWithWhitespace_returnsTrimmed() {
        Document doc = Document.createShell("http://example.com");
        doc.title("  Hello World  ");
        assertEquals("Hello World", doc.title());
    }

    // Test title(String) on document without head – this exposes Defects4J bug 1b
    @Test
    public void testTitle_setTitleOnDocumentWithoutHead_worksCorrectly() {
        Document doc = new Document("http://example.com");
        doc.title("Bug Test");
        assertEquals("Bug Test", doc.title());
    }

    // Test title(String) with null title throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_setNullTitle_throwsIllegalArgumentException() {
        Document doc = Document.createShell("http://example.com");
        doc.title(null);
    }

    // Test createShell with null baseUri throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullBaseUri_throwsIllegalArgumentException() {
        Document.createShell(null);
    }

    // Test createElement creates element with correct tag name and base URI
    @Test
    public void testCreateElement_validTagName_createsElementWithSameBaseUri() {
        Document doc = new Document("http://example.com");
        Element el = doc.createElement("div");
        assertEquals("div", el.tagName());
        assertEquals("http://example.com", el.baseUri());
    }

    // Test normalise when document has no html/head/body
    @Test
    public void testNormalise_documentWithoutHtml_createsHtmlHeadBody() {
        Document doc = new Document("http://example.com");
        assertNull(doc.head());
        assertNull(doc.body());
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertNotNull(doc.getElementsByTag("html").first());
    }

    // Test normalise moves non‑blank text nodes from root to body
    @Test
    public void testNormalise_textNodeInRoot_movesTextToBody() {
        Document doc = new Document("http://example.com");
        TextNode tn = new TextNode("hello", doc.baseUri());
        doc.appendChild(tn);
        doc.normalise();
        // body should contain the text (with a leading space added by normalise)
        assertTrue(doc.body().text().contains("hello"));
        // the text node should no longer be a direct child of document
        boolean found = false;
        for (Node n : doc.childNodes()) {
            if (n instanceof TextNode && ((TextNode)n).text().equals("hello")) {
                found = true;
                break;
            }
        }
        assertFalse(found);
    }

    // Test normalise leaves blank text nodes in place
    @Test
    public void testNormalise_blankTextNodeInHead_notMoved() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        head.appendChild(new TextNode("   ", doc.baseUri()));
        doc.normalise();
        // body should have no children because blank nodes are not moved
        assertEquals(0, doc.body().childNodes().size());
        // the blank text node should still be in head
        boolean foundBlank = false;
        for (Node n : head.childNodes()) {
            if (n instanceof TextNode && ((TextNode)n).isBlank()) {
                foundBlank = true;
                break;
            }
        }
        assertTrue(foundBlank);
    }

    // Test normalise moves non‑blank text nodes from head to body
    @Test
    public void testNormalise_textNodeInHead_movesTextToBody() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        head.appendChild(new TextNode("head text", doc.baseUri()));
        doc.normalise();
        // body should contain the moved text
        assertTrue(doc.body().text().contains("head text"));
        // head should no longer contain that text node
        boolean found = false;
        for (Node n : head.childNodes()) {
            if (n instanceof TextNode && ((TextNode)n).text().equals("head text")) {
                found = true;
                break;
            }
        }
        assertFalse(found);
    }

    // Test normalise on a document that already has html, head, body does not add extra elements
    @Test
    public void testNormalise_documentWithHtmlHeadBody_doesNotAddExtraElements() {
        Document doc = Document.createShell("http://example.com");
        int childCountBefore = doc.childNodes().size();
        doc.normalise();
        assertEquals(childCountBefore, doc.childNodes().size());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Test text(String) setter clears and sets body content, preserving document structure
    @Test
    public void testText_setText_clearsAndSetsBodyText() {
        Document doc = Document.createShell("http://example.com");
        doc.text("New Body Text");
        assertEquals("New Body Text", doc.body().text());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Test outerHtml returns inner HTML of document (without outer wrapper)
    @Test
    public void testOuterHtml_documentWithHtmlHeadBody_returnsCorrectHtml() {
        Document doc = Document.createShell("http://example.com");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
    }

    // Test nodeName returns "#document"
    @Test
    public void testNodeName_returnsDocument() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
    }
}