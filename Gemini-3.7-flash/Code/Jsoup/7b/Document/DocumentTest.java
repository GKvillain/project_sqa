package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import static org.junit.Assert.*;

public class DocumentTest {

    // Tests createShell with valid base URI
    @Test
    public void testCreateShell_validBaseUri_createsShellStructure() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("http://example.com", doc.baseUri());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", doc.head().nodeName());
        assertEquals("body", doc.body().nodeName());
    }

    // Tests createShell with null base URI throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateShell_nullBaseUri_throwsException() {
        Document.createShell(null);
    }

    // Tests nodeName returning #document
    @Test
    public void testNodeName_default_returnsDocument() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
    }

    // Tests title retrieval when no title tag exists
    @Test
    public void testTitle_noTitleElement_returnsEmptyString() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("", doc.title());
    }

    // Tests title setting when title tag does not exist
    @Test
    public void testTitle_setTitleWhenNoneExists_createsTitleInHead() {
        Document doc = Document.createShell("http://example.com");
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());
    }

    // Tests title updating when title tag already exists
    @Test
    public void testTitle_updateExistingTitle_updatesText() {
        Document doc = Document.createShell("http://example.com");
        doc.title("Initial Title");
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());
    }

    // Tests title setting with null value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTitle_nullTitle_throwsException() {
        Document doc = Document.createShell("http://example.com");
        doc.title(null);
    }

    // Tests createElement creates standalone element with document base URI
    @Test
    public void testCreateElement_validTagName_returnsElementWithBaseUri() {
        Document doc = new Document("http://example.com");
        Element el = doc.createElement("div");
        assertEquals("div", el.nodeName());
        assertEquals("http://example.com", el.baseUri());
        assertNull(el.parent());
    }

    // Tests text setting updates body text without destroying html/body structure
    @Test
    public void testText_setBodyText_updatesBodyOnly() {
        Document doc = Document.createShell("http://example.com");
        doc.text("Hello World");
        assertEquals("Hello World", doc.body().text());
        assertNotNull(doc.head());
    }

    // Tests normalise moves non-blank text nodes from root/head/html into body
    @Test
    public void testNormalise_textNodesOutsideBody_movesTextToBody() {
        Document doc = new Document("http://example.com");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");

        doc.appendChild(new TextNode("Root Text", ""));
        head.appendChild(new TextNode("Head Text", ""));

        doc.normalise();

        assertTrue(doc.body().text().contains("Root Text"));
        assertTrue(doc.body().text().contains("Head Text"));
        assertEquals(0, head.getElementsByTag("title").size());
    }

    // Tests normalise creates missing html, head, and body elements
    @Test
    public void testNormalise_missingHtmlHeadBody_createsMissingStructure() {
        Document doc = new Document("http://example.com");
        doc.normalise();

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.childNode(0).nodeName());
    }

    // Tests outerHtml returning children html without wrapper tag
    @Test
    public void testOuterHtml_standardDoc_returnsRenderedHtml() {
        Document doc = Document.createShell("http://example.com");
        doc.body().appendElement("p").text("Paragraph");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<p>Paragraph</p>"));
    }

    // Tests OutputSettings escapeMode getter and setter chaining
    @Test
    public void testOutputSettings_escapeMode_setsAndGets() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());

        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
    }

    // Tests OutputSettings charset by Charset instance and by String name
    @Test
    public void testOutputSettings_charset_setsAndGets() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        assertEquals(Charset.forName("UTF-8"), settings.charset());
        assertNotNull(settings.encoder());

        settings.charset(Charset.forName("US-ASCII"));
        assertEquals(Charset.forName("US-ASCII"), settings.charset());

        settings.charset("ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), settings.charset());
    }

    // Tests OutputSettings prettyPrint getter and setter
    @Test
    public void testOutputSettings_prettyPrint_setsAndGets() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        assertTrue(settings.prettyPrint());

        settings.prettyPrint(false);
        assertFalse(settings.prettyPrint());
    }

    // Tests OutputSettings indentAmount with valid boundary value
    @Test
    public void testOutputSettings_indentAmountValid_setsAndGets() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        assertEquals(1, settings.indentAmount());

        settings.indentAmount(0);
        assertEquals(0, settings.indentAmount());

        settings.indentAmount(4);
        assertEquals(4, settings.indentAmount());
    }

    // Tests OutputSettings indentAmount with negative value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettings_indentAmountNegative_throwsException() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(-1);
    }
}