package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import static org.junit.Assert.*;

public class DocumentTest {

    // Tests constructor and nodeName
    @Test
    public void testDocumentConstructor_validUri_setsCorrectNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
        assertEquals("http://example.com/", doc.baseUri());
    }

    // Tests createShell with valid baseUri
    @Test
    public void testCreateShell_validUri_createsStructure() {
        Document doc = Document.createShell("http://example.com/");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", doc.head().tagName());
        assertEquals("body", doc.body().tagName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", doc.outerHtml());
    }

    // Tests createShell with null baseUri throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullUri_throwsException() {
        Document.createShell(null);
    }

    // Tests title retrieval when no title tag exists
    @Test
    public void testTitle_noTitleTag_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());
    }

    // Tests setting title when no title tag exists
    @Test
    public void testTitle_setTitleWhenNoneExists_createsTitleInHead() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());
    }

    // Tests updating an existing title tag
    @Test
    public void testTitle_updateExistingTitle_updatesText() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Initial");
        doc.title("Updated");
        assertEquals("Updated", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());
    }

    // Tests title retrieval with whitespace trimming
    @Test
    public void testTitle_withWhitespace_returnsTrimmed() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendElement("title").text("   Trimmed Title   ");
        assertEquals("Trimmed Title", doc.title());
    }

    // Tests title with null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_nullInput_throwsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    // Tests createElement creates element with correct tag and baseUri
    @Test
    public void testCreateElement_validTag_createsElement() {
        Document doc = new Document("http://example.com/");
        Element el = doc.createElement("div");
        assertEquals("div", el.tagName());
        assertEquals("http://example.com/", el.baseUri());
    }

    // Tests normalise on empty document creates html, head, and body
    @Test
    public void testNormalise_emptyDocument_createsStructure() {
        Document doc = new Document("http://example.com/");
        doc.normalise();
        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Tests normalise moves non-blank text nodes into body in correct order
    @Test
    public void testNormalise_textNodesInHeadAndRoot_movesToBodyInOrder() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendChild(new TextNode("head text", ""));
        doc.appendChild(new TextNode("root text", ""));
        doc.body().appendElement("p").text("body text");

        doc.normalise();

        assertEquals(0, doc.head().textNodes().size());
        assertTrue(doc.body().text().contains("head text"));
        assertTrue(doc.body().text().contains("root text"));
    }

    // Tests normalise ignores blank text nodes
    @Test
    public void testNormalise_blankTextNodes_ignored() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendChild(new TextNode("   ", ""));
        doc.normalise();
        assertEquals("", doc.body().text());
    }

    // Tests text(String) replaces body content without destroying document structure
    @Test
    public void testText_setsBodyText_preservesStructure() {
        Document doc = Document.createShell("http://example.com/");
        doc.text("Hello World");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Hello World", doc.body().text());
    }

    // Tests outerHtml returns html without root tag wrapper
    @Test
    public void testOuterHtml_returnsInnerHtmlContent() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Paragraph");
        assertEquals("<html>\n <head></head>\n <body>\n  <p>Paragraph</p>\n </body>\n</html>", doc.outerHtml());
    }

    // Tests Document clone method creates deep copy
    @Test
    public void testClone_createsDeepCopy() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("div").text("Original");
        Document clone = doc.clone();

        assertEquals(doc.outerHtml(), clone.outerHtml());
        clone.body().select("div").first().text("Modified");
        assertNotEquals(doc.outerHtml(), clone.outerHtml());
        assertEquals("Original", doc.body().select("div").first().text());
        assertEquals("Modified", clone.body().select("div").first().text());
    }

    // Tests Document OutputSettings methods
    @Test
    public void testOutputSettings_getAndSet() {
        Document doc = Document.createShell("http://example.com/");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        settings.indentAmount(4);
        settings.charset(Charset.forName("US-ASCII"));

        doc.outputSettings(settings);
        assertSame(settings, doc.outputSettings());
        assertFalse(doc.outputSettings().prettyPrint());
        assertEquals(4, doc.outputSettings().indentAmount());
        assertEquals(Charset.forName("US-ASCII"), doc.outputSettings().charset());
    }

    // Tests OutputSettings clone method
    @Test
    public void testOutputSettings_clone() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(8);
        settings.prettyPrint(false);

        Document.OutputSettings cloned = settings.clone();
        assertEquals(8, cloned.indentAmount());
        assertFalse(cloned.prettyPrint());
        assertEquals(settings.charset(), cloned.charset());
        assertEquals(settings.escapeMode(), cloned.escapeMode());
    }

    // Tests OutputSettings prettyPrint formatting effect
    @Test
    public void testOutputSettings_prettyPrintFalse_rendersCompact() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Paragraph");
        doc.outputSettings().prettyPrint(false);
        assertEquals("<html><head></head><body><p>Paragraph</p></body></html>", doc.outerHtml());
    }

    // Tests Document charset get and set
    @Test
    public void testCharset_getAndSet() {
        Document doc = Document.createShell("http://example.com/");
        doc.charset(Charset.forName("ISO-8859-1"));
        assertEquals(Charset.forName("ISO-8859-1"), doc.charset());
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    // Tests Document quirksMode get and set
    @Test
    public void testQuirksMode_getAndSet() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }

    // Tests Document normalise with out-of-order elements
    @Test
    public void testNormalise_elementsMovedToHtml() {
        Document doc = new Document("http://example.com/");
        Element div = new Element("div");
        doc.appendChild(div);

        doc.normalise();

        assertNotNull(doc.body());
        assertTrue(doc.body().children().contains(div));
    }
}