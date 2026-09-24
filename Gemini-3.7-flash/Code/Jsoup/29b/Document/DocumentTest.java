package org.jsoup.nodes;

import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.QuirksMode;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DocumentTest {

    // Tests createShell with valid baseUri and ensures standard HTML structure is created
    @Test
    public void testCreateShell_validBaseUri_createsShellStructure() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("http://example.com/", doc.baseUri());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", doc.head().tagName());
        assertEquals("body", doc.body().tagName());
        assertEquals("html", doc.head().parent().tagName());
    }

    // Tests createShell throws exception on null baseUri
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullBaseUri_throwsException() {
        Document.createShell(null);
    }

    // Tests title retrieval when no title element exists
    @Test
    public void testTitle_noTitleElement_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());
    }

    // Tests setting and getting title when title element is not initially present
    @Test
    public void testTitle_setNewTitle_appendsToHeadAndReturnsTitle() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Test Document Title");
        assertEquals("Test Document Title", doc.title());
        assertNotNull(doc.head().getElementsByTag("title").first());
        assertEquals("Test Document Title", doc.head().getElementsByTag("title").first().text());
    }

    // Tests updating an existing title element
    @Test
    public void testTitle_updateExistingTitle_replacesOldTitle() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Initial Title");
        assertEquals("Initial Title", doc.title());

        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());
    }

    // Tests title whitespace trimming and formatting
    @Test
    public void testTitle_withWhitespaceAndNewlines_returnsTrimmed() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendElement("title").text("\n\n   Document Title with Whitespace   \n ");
        assertEquals("Document Title with Whitespace", doc.title());
    }

    // Tests setting null title throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_nullTitle_throwsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    // Tests createElement creates detached element with doc's base URI
    @Test
    public void testCreateElement_validTagName_returnsElementWithDocBaseUri() {
        Document doc = new Document("http://example.com/base/");
        Element div = doc.createElement("div");
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/base/", div.baseUri());
        assertNull(div.parent());
    }

    // Tests text(String) updates body text without removing head or body elements
    @Test
    public void testText_settingBodyText_preservesStructure() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Doc Title");
        doc.text("New Body Content");

        assertEquals("New Body Content", doc.body().text());
        assertEquals("Doc Title", doc.title());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Tests nodeName returns #document
    @Test
    public void testNodeName_returnsCorrectNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
    }

    // Tests outerHtml outputs HTML content without outer #document tag
    @Test
    public void testOuterHtml_emptyDoc_returnsHtmlContent() {
        Document doc = Document.createShell("http://example.com/");
        String outerHtml = doc.outerHtml();
        assertTrue(outerHtml.contains("<html>"));
        assertTrue(outerHtml.contains("<head>"));
        assertTrue(outerHtml.contains("<body>"));
        assertFalse(outerHtml.contains("#document"));
    }

    // Tests normalise method on malformed tree with missing html/head/body and misplaced text nodes
    @Test
    public void testNormalise_malformedDocument_restructuresCorrectly() {
        Document doc = new Document("http://example.com/");
        doc.appendText("Text in root");
        Element p = doc.appendElement("p").text("Paragraph in root");

        doc.normalise();

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertTrue(doc.body().text().contains("Text in root"));
        assertTrue(doc.body().text().contains("Paragraph in root"));
    }

    // Tests normalise method with duplicate head and body elements merging into single master elements
    @Test
    public void testNormalise_duplicateHeadAndBody_mergesDuplicates() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head1 = html.appendElement("head");
        head1.appendElement("title").text("Title 1");
        Element body1 = html.appendElement("body");
        body1.appendElement("p").text("Body 1 Content");

        Element head2 = html.appendElement("head");
        head2.appendElement("meta").attr("charset", "utf-8");
        Element body2 = html.appendElement("body");
        body2.appendElement("span").text("Body 2 Content");

        doc.normalise();

        assertEquals(1, doc.getElementsByTag("head").size());
        assertEquals(1, doc.getElementsByTag("body").size());
        assertEquals(1, doc.getElementsByTag("title").size());
        assertEquals(1, doc.getElementsByTag("meta").size());
        assertEquals(1, doc.getElementsByTag("p").size());
        assertEquals(1, doc.getElementsByTag("span").size());
    }

    // Tests cloning of Document including OutputSettings deep clone
    @Test
    public void testClone_clonedDocument_hasIndependentOutputSettings() {
        Document doc = Document.createShell("http://example.com/");
        doc.outputSettings().indentAmount(4);
        doc.outputSettings().prettyPrint(false);

        Document clone = doc.clone();
        assertNotSame(doc, clone);
        assertNotSame(doc.outputSettings(), clone.outputSettings());
        assertEquals(4, clone.outputSettings().indentAmount());
        assertFalse(clone.outputSettings().prettyPrint());

        clone.outputSettings().indentAmount(2);
        assertEquals(4, doc.outputSettings().indentAmount());
        assertEquals(2, clone.outputSettings().indentAmount());
    }

    // Tests OutputSettings getters, setters and method chaining
    @Test
    public void testOutputSettings_gettersAndSetters_updatesSettingsCorrectly() {
        OutputSettings settings = new OutputSettings();
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals(Charset.forName("UTF-8"), settings.charset());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
        assertNotNull(settings.encoder());

        settings.escapeMode(Entities.EscapeMode.extended)
                .charset("ISO-8859-1")
                .prettyPrint(false)
                .indentAmount(8);

        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
        assertEquals(Charset.forName("ISO-8859-1"), settings.charset());
        assertFalse(settings.prettyPrint());
        assertEquals(8, settings.indentAmount());
    }

    // Tests OutputSettings indentAmount validation for negative value
    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettings_negativeIndentAmount_throwsException() {
        OutputSettings settings = new OutputSettings();
        settings.indentAmount(-1);
    }

    // Tests setting null OutputSettings on Document throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testDocument_nullOutputSettings_throwsException() {
        Document doc = new Document("http://example.com/");
        doc.outputSettings(null);
    }

    // Tests QuirksMode getter and setter
    @Test
    public void testQuirksMode_getAndSet_updatesMode() {
        Document doc = new Document("http://example.com/");
        assertEquals(QuirksMode.noQuirks, doc.quirksMode());

        doc.quirksMode(QuirksMode.quirks);
        assertEquals(QuirksMode.quirks, doc.quirksMode());

        doc.quirksMode(QuirksMode.limitedQuirks);
        assertEquals(QuirksMode.limitedQuirks, doc.quirksMode());
    }
}