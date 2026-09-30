package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DocumentTest {

    // Tests createShell creates html, head, and body
    @Test
    public void testCreateShell_validUri_createsStructure() {
        Document doc = Document.createShell("http://example.com/");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("#document", doc.nodeName());
    }

    // Tests createShell with null baseUri throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullUri_throwsException() {
        Document.createShell(null);
    }

    // Tests getting title when no title element exists
    @Test
    public void testTitle_noTitleElement_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());
    }

    // Tests setting and getting title when title element does not exist initially
    @Test
    public void testTitle_setNewTitle_createsTitleInHead() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());
    }

    // Tests updating an existing title element
    @Test
    public void testTitle_updateExistingTitle_updatesText() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Initial Title");
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.getElementsByTag("title").size());
    }

    // Tests setting null title throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_nullTitle_throwsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    // Tests createElement creates element with doc's base URI but not attached to doc
    @Test
    public void testCreateElement_validTag_createsDetachedElementWithBaseUri() {
        Document doc = new Document("http://example.com/");
        Element div = doc.createElement("div");
        assertNotNull(div);
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/", div.baseUri());
        assertNull(div.parent());
    }

    // Tests normalise method when html, head, and body are missing
    @Test
    public void testNormalise_emptyDocument_createsHtmlHeadBody() {
        Document doc = new Document("http://example.com/");
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.childNode(0).nodeName());
    }

    // Tests normalise moves non-blank text nodes into body
    @Test
    public void testNormalise_textNodesInRootAndHead_movedToBody() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendText("Head text");
        doc.appendText("Root text");
        doc.normalise();

        assertTrue(doc.body().text().contains("Head text"));
        assertTrue(doc.body().text().contains("Root text"));
    }

    // Tests setting text on document sets body text without destroying structure
    @Test
    public void testText_setText_setsBodyTextPreservingStructure() {
        Document doc = Document.createShell("http://example.com/");
        doc.text("Hello World");
        assertEquals("Hello World", doc.body().text());
        assertNotNull(doc.head());
    }

    // Tests outerHtml outputs child html without wrapping #document tag
    @Test
    public void testOuterHtml_standardDoc_rendersHtmlWithoutDocumentTag() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Sample text");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<p>Sample text</p>"));
        assertFalse(html.contains("#document"));
    }

    // Tests outputSettings defaults and chaining setters
    @Test
    public void testOutputSettings_defaultsAndModifications() {
        Document doc = new Document("http://example.com/");
        Document.OutputSettings settings = doc.outputSettings();

        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals(Charset.forName("UTF-8"), settings.charset());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
        assertNotNull(settings.encoder());

        settings.escapeMode(Entities.EscapeMode.extended)
                .charset("US-ASCII")
                .prettyPrint(false)
                .indentAmount(4);

        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
        assertEquals(Charset.forName("US-ASCII"), settings.charset());
        assertFalse(settings.prettyPrint());
        assertEquals(4, settings.indentAmount());
    }

    // Tests setting negative indentAmount throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettings_negativeIndentAmount_throwsException() {
        Document doc = new Document("http://example.com/");
        doc.outputSettings().indentAmount(-1);
    }

    // Tests charset update using Charset instance
    @Test
    public void testOutputSettings_charsetInstance_updatesCharsetAndEncoder() {
        Document doc = new Document("http://example.com/");
        Charset iso88591 = Charset.forName("ISO-8859-1");
        doc.outputSettings().charset(iso88591);
        assertEquals(iso88591, doc.outputSettings().charset());
        assertEquals(iso88591, doc.outputSettings().encoder().charset());
    }
}