package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.charset.Charset;

public class DocumentTest {
    private static final String BASE_URI = "http://example.com";

    @Test
    public void testCreateShell_baseUriNotNull_createsDocumentWithHtmlHeadBody() {
        Document doc = Document.createShell(BASE_URI);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.head().parent().tagName());
    }

    @Test
    public void testHead_emptyDocument_returnsNull() {
        Document doc = new Document(BASE_URI);
        assertNull(doc.head());
    }

    @Test
    public void testBody_emptyDocument_returnsNull() {
        Document doc = new Document(BASE_URI);
        assertNull(doc.body());
    }

    @Test
    public void testTitle_emptyDocument_returnsEmptyString() {
        Document doc = new Document(BASE_URI);
        assertEquals("", doc.title());
    }

    @Test
    public void testTitle_setAndGet_returnsTrimmedText() {
        Document doc = Document.createShell(BASE_URI);
        doc.title("  My Title  ");
        assertEquals("My Title", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitle_setNullTitle_throwsException() {
        Document doc = Document.createShell(BASE_URI);
        doc.title(null);
    }

    @Test
    public void testTitle_setWhenNoTitle_addsTitleToHead() {
        Document doc = Document.createShell(BASE_URI);
        doc.title("New Title");
        assertNotNull(doc.getElementsByTag("title").first());
        assertEquals("New Title", doc.title());
    }

    @Test
    public void testCreateElement_returnsNewElementWithBaseUri() {
        Document doc = new Document(BASE_URI);
        Element elem = doc.createElement("div");
        assertEquals("div", elem.tagName());
        assertEquals(BASE_URI, elem.baseUri());
    }

    @Test
    public void testNormalise_emptyDocument_addsHtmlHeadBody() {
        Document doc = new Document(BASE_URI);
        assertNull(doc.head());
        assertNull(doc.body());
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(1, doc.children().size());
    }

    @Test
    public void testNormalise_textInHead_movedToBody() {
        Document doc = Document.createShell(BASE_URI);
        Element head = doc.head();
        head.appendChild(new TextNode("Hello", BASE_URI));
        doc.normalise();
        assertTrue(head.childNodes().isEmpty());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testNormalise_multipleTextNodes_movedToBodyWithoutExtraSpaces() {
        Document doc = Document.createShell(BASE_URI);
        Element head = doc.head();
        head.appendChild(new TextNode("First", BASE_URI));
        head.appendChild(new TextNode("Second", BASE_URI));
        doc.normalise();
        assertEquals("FirstSecond", doc.body().text());
    }

    @Test
    public void testNormalise_blankTextNodesNotMoved() {
        Document doc = Document.createShell(BASE_URI);
        Element head = doc.head();
        head.appendChild(new TextNode("   ", BASE_URI));
        head.appendChild(new TextNode("Real", BASE_URI));
        doc.normalise();
        assertEquals("Real", doc.body().text());
    }

    @Test
    public void testNormalise_textInRoot_movedToBody() {
        Document doc = new Document(BASE_URI);
        doc.appendChild(new TextNode("RootText", BASE_URI));
        doc.normalise();
        assertEquals("RootText", doc.body().text());
    }

    @Test
    public void testNormalise_textInHtmlElement_movedToBody() {
        Document doc = Document.createShell(BASE_URI);
        Element html = doc.child(0);
        html.appendChild(new TextNode("HtmlText", BASE_URI));
        doc.normalise();
        assertEquals("HtmlText", doc.body().text());
    }

    @Test
    public void testOuterHtml_returnsSameAsHtml() {
        Document doc = Document.createShell(BASE_URI);
        assertEquals(doc.html(), doc.outerHtml());
    }

    @Test
    public void testText_overridesBodyText() {
        Document doc = Document.createShell(BASE_URI);
        doc.text("My body text");
        assertEquals("My body text", doc.body().text());
    }

    @Test
    public void testOutputSettings_escapeMode_defaultAndSet() {
        Document doc = new Document(BASE_URI);
        assertEquals(Entities.EscapeMode.base, doc.outputSettings().escapeMode());
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, doc.outputSettings().escapeMode());
    }

    @Test
    public void testOutputSettings_charset_defaultAndSet() {
        Document doc = new Document(BASE_URI);
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
        doc.outputSettings().charset("ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testOutputSettings_prettyPrint_defaultAndSet() {
        Document doc = new Document(BASE_URI);
        assertTrue(doc.outputSettings().prettyPrint());
        doc.outputSettings().prettyPrint(false);
        assertFalse(doc.outputSettings().prettyPrint());
    }

    @Test
    public void testOutputSettings_indentAmount_defaultAndSet() {
        Document doc = new Document(BASE_URI);
        assertEquals(1, doc.outputSettings().indentAmount());
        doc.outputSettings().indentAmount(4);
        assertEquals(4, doc.outputSettings().indentAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettings_indentAmount_negative_throwsException() {
        Document doc = new Document(BASE_URI);
        doc.outputSettings().indentAmount(-1);
    }

    @Test
    public void testNodeName_returnsHashDocument() {
        Document doc = new Document(BASE_URI);
        assertEquals("#document", doc.nodeName());
    }
}