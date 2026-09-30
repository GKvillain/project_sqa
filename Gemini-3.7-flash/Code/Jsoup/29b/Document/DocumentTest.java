package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DocumentTest {

    // Tests createShell creates html, head, and body elements
    @Test
    public void testCreateShell_validBaseUri_createsValidStructure() {
        Document doc = Document.createShell("http://example.com/");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("html", doc.child(0).tagName());
        assertEquals("head", doc.head().tagName());
        assertEquals("body", doc.body().tagName());
    }

    // Tests createShell with null baseUri throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullBaseUri_throwsException() {
        Document.createShell(null);
    }

    // Tests title getter on document without title element returns empty string
    @Test
    public void testTitle_noTitleElement_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());
    }

    // Tests title setter creates title in head when not present
    @Test
    public void testTitle_setTitleWhenNoneExists_createsTitleInHead() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());
    }

    // Tests title setter updates existing title element
    @Test
    public void testTitle_updateExistingTitle_updatesCorrectly() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Old Title");
        doc.title("New Title");
        assertEquals("New Title", doc.title());
        assertEquals(1, doc.getElementsByTag("title").size());
    }

    // Tests title with whitespace trims properly
    @Test
    public void testTitle_titleWithWhitespace_returnsTrimmed() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendElement("title").text("   Whitespace Title   \n");
        assertEquals("Whitespace Title", doc.title());
    }

    // Tests title setter with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_nullInput_throwsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    // Tests createElement creates element with doc baseUri without attaching
    @Test
    public void testCreateElement_validTag_returnsDetachedElement() {
        Document doc = new Document("http://example.com/");
        Element div = doc.createElement("div");
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/", div.baseUri());
        assertNull(div.parent());
    }

    // Tests normalise with missing html, head, body tags creates them
    @Test
    public void testNormalise_emptyDocument_createsStructure() {
        Document doc = new Document("http://example.com/");
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.child(0).tagName());
    }

    // Tests normalise moves text nodes outside body into body
    @Test
    public void testNormalise_textNodesOutsideBody_movesToBody() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");
        head.appendText("Text in head");

        doc.normalise();
        assertTrue(doc.body().text().contains("Text in head"));
    }

    // Tests normalise merges multiple head and body elements
    @Test
    public void testNormalise_duplicateHeadAndBody_mergesDuplicates() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head1 = html.appendElement("head");
        head1.appendElement("meta");
        Element head2 = html.appendElement("head");
        head2.appendElement("style");
        Element body1 = html.appendElement("body");
        body1.appendElement("p");
        Element body2 = html.appendElement("body");
        body2.appendElement("div");

        doc.normalise();
        assertEquals(1, doc.getElementsByTag("head").size());
        assertEquals(1, doc.getElementsByTag("body").size());
        assertNotNull(doc.head().getElementsByTag("style").first());
        assertNotNull(doc.body().getElementsByTag("div").first());
    }

    // Tests outerHtml does not render a #document root tag
    @Test
    public void testOuterHtml_standardDoc_rendersInnerHtml() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Hello");
        assertEquals("<html>\n <head></head>\n <body>\n  <p>Hello</p>\n </body>\n</html>", doc.outerHtml());
    }

    // Tests text(String) replaces body content without removing structure
    @Test
    public void testText_setTextOnDoc_updatesBodyContent() {
        Document doc = Document.createShell("http://example.com/");
        doc.text("New text content");
        assertEquals("New text content", doc.body().text());
        assertNotNull(doc.head());
    }

    // Tests nodeName returns #document
    @Test
    public void testNodeName_returnsDocumentNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
    }

    // Tests Document clone creates deep copy including output settings
    @Test
    public void testClone_documentClone_createsIndependentCopy() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Original");
        doc.outputSettings().indentAmount(4);

        Document clone = doc.clone();
        clone.title("Cloned");
        clone.outputSettings().indentAmount(2);

        assertEquals("Original", doc.title());
        assertEquals("Cloned", clone.title());
        assertEquals(4, doc.outputSettings().indentAmount());
        assertEquals(2, clone.outputSettings().indentAmount());
    }

    // Tests OutputSettings getters and setters
    @Test
    public void testOutputSettings_gettersAndSetters_updatesValues() {
        Document.OutputSettings settings = new Document.OutputSettings();

        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());

        settings.charset(Charset.forName("US-ASCII"));
        assertEquals(Charset.forName("US-ASCII"), settings.charset());
        assertNotNull(settings.encoder());

        settings.charset("ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), settings.charset());

        settings.prettyPrint(false);
        assertFalse(settings.prettyPrint());

        settings.indentAmount(3);
        assertEquals(3, settings.indentAmount());
    }

    // Tests OutputSettings indentAmount with negative value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettings_negativeIndentAmount_throwsException() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(-1);
    }

    // Tests OutputSettings clone creates independent copy
    @Test
    public void testOutputSettings_clone_createsIndependentCopy() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(5);
        settings.escapeMode(Entities.EscapeMode.extended);

        Document.OutputSettings clone = settings.clone();
        clone.indentAmount(2);
        clone.escapeMode(Entities.EscapeMode.base);

        assertEquals(5, settings.indentAmount());
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
        assertEquals(2, clone.indentAmount());
        assertEquals(Entities.EscapeMode.base, clone.escapeMode());
    }

    // Tests outputSettings null check on document
    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettings_nullSettings_throwsException() {
        Document doc = new Document("http://example.com/");
        doc.outputSettings(null);
    }

    // Tests quirksMode getter and setter
    @Test
    public void testQuirksMode_getAndSet_updatesQuirksMode() {
        Document doc = new Document("http://example.com/");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }
}