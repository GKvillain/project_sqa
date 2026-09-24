package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTest {

    // Tests Document constructor with base URI
    @Test
    public void testDocumentConstructor_validBaseUri_initializesCorrectly() {
        Document doc = new Document("http://example.com/");
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("#document", doc.nodeName());
    }

    // Tests createShell with valid base URI creating html, head, and body
    @Test
    public void testCreateShell_validBaseUri_createsValidDocumentStructure() {
        Document doc = Document.createShell("http://example.com/");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.childNode(0).nodeName());
        assertEquals("head", doc.head().nodeName());
        assertEquals("body", doc.body().nodeName());
    }

    // Tests createShell with null base URI throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullBaseUri_throwsException() {
        Document.createShell(null);
    }

    // Tests head accessor when head is present
    @Test
    public void testHead_shellDocument_returnsHeadElement() {
        Document doc = Document.createShell("http://example.com/");
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    // Tests body accessor when body is present
    @Test
    public void testBody_shellDocument_returnsBodyElement() {
        Document doc = Document.createShell("http://example.com/");
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    // Tests title when no title element exists
    @Test
    public void testTitle_noTitleElement_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());
    }

    // Tests title when title element exists with whitespace
    @Test
    public void testTitle_withExistingTitle_returnsTrimmedTitle() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendElement("title").text("  Test Page Title  ");
        assertEquals("Test Page Title", doc.title());
    }

    // Tests setting title when title element does not exist
    @Test
    public void testTitle_setTitleWhenNoTitlePresent_addsTitleToHead() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("New Title");
        assertEquals("New Title", doc.title());
        assertEquals("New Title", doc.head().getElementsByTag("title").first().text());
    }

    // Tests updating title when title element already exists
    @Test
    public void testTitle_updateExistingTitle_updatesTitleText() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Initial Title");
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());
    }

    // Tests setting title with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_nullTitle_throwsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    // Tests createElement creates detached element with doc base URI
    @Test
    public void testCreateElement_validTagName_returnsElementWithBaseUri() {
        Document doc = new Document("http://example.com/");
        Element el = doc.createElement("div");
        assertNotNull(el);
        assertEquals("div", el.tagName());
        assertEquals("http://example.com/", el.baseUri());
        assertNull(el.parent());
    }

    // Tests normalise on completely empty document
    @Test
    public void testNormalise_emptyDocument_createsHtmlHeadBody() {
        Document doc = new Document("http://example.com/");
        doc.normalise();
        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Tests normalise moves text nodes outside body into body
    @Test
    public void testNormalise_textNodesOutsideBody_movesTextIntoBody() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");

        doc.appendChild(new TextNode("TextInRoot", ""));
        html.appendChild(new TextNode("TextInHtml", ""));
        head.appendChild(new TextNode("TextInHead", ""));
        body.text("BodyContent");

        doc.normalise();

        assertTrue(doc.body().text().contains("TextInRoot"));
        assertTrue(doc.body().text().contains("TextInHtml"));
        assertTrue(doc.body().text().contains("TextInHead"));
    }

    // Tests outerHtml returns inner html without outer wrapper tag
    @Test
    public void testOuterHtml_shellDocument_returnsHtmlContent() {
        Document doc = Document.createShell("http://example.com/");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertFalse(html.contains("#root"));
        assertFalse(html.contains("#document"));
    }

    // Tests text method sets body text without destroying document structure
    @Test
    public void testText_settingBodyText_preservesStructureAndSetsText() {
        Document doc = Document.createShell("http://example.com/");
        doc.text("Hello World");
        assertEquals("Hello World", doc.body().text());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Tests nodeName returns #document
    @Test
    public void testNodeName_default_returnsDocumentNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
    }
}