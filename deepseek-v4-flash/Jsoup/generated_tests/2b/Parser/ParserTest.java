package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {
    private static final String BASE = "http://example.com/";

    private Document parse(String html) {
        return Parser.parse(html, BASE);
    }

    private Document parseBody(String html) {
        return Parser.parseBodyFragment(html, BASE);
    }

    // Tests parsing a simple document
    @Test
    public void testParse_simpleHtml_returnsDocumentWithTitleAndBody() {
        Document doc = parse("<html><head><title>Hello</title></head><body><p>World</p></body></html>");
        assertEquals("Hello", doc.title());
        assertEquals("World", doc.body().text());
    }

    // Tests body-only input and implicit document structure
    @Test
    public void testParse_bodyOnly_createsImplicitStructure() {
        Document doc = parse("<p>One</p><p>Two</p>");
        assertEquals("One Two", doc.body().text());
        assertEquals("p", doc.body().children().get(0).tagName());
    }

    // Tests parseBodyFragment entry point
    @Test
    public void testParseBodyFragment_paragraph_returnsDocumentContainingParagraph() {
        Document doc = parseBody("<p>Hello</p>");
        assertEquals("Hello", doc.body().text());
        assertEquals("p", doc.body().children().get(0).tagName());
    }

    // Tests empty input boundary
    @Test
    public void testParse_emptyString_returnsEmptyDocument() {
        Document doc = parse("");
        assertEquals("", doc.text());
    }

    // Tests null html input validation
    @Test(expected = RuntimeException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, BASE);
    }

    // Tests null base URI validation
    @Test(expected = RuntimeException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<html></html>", null);
    }

    // Tests malformed start tag treated as text
    @Test
    public void testParse_malformedStartTag_parsesAsText() {
        Document doc = parse("< p>hello");
        assertTrue(doc.body().text().contains("< p>hello"));
    }

    // Tests quoted and unquoted attribute parsing, and attribute without value
    @Test
    public void testParse_startTagWithAttributes_parsesAttributes() {
        Document doc = parse("<html><body><input disabled><a href=\"http://example.com/page?x=1\" id='a1' class=foo>link</a></body></html>");
        Element input = doc.getElementsByTag("input").first();
        Element link = doc.getElementsByTag("a").first();

        assertNotNull(input);
        assertNotNull(link);
        assertEquals("", input.attr("disabled"));
        assertEquals("http://example.com/page?x=1", link.attr("href"));
        assertEquals("a1", link.attr("id"));
        assertEquals("foo", link.attr("class"));
    }

    // Tests self-closing start tag path
    @Test
    public void testParse_selfClosingTag_createsEmptyElement() {
        Document doc = parse("<html><body><div/><p>text</p></body></html>");
        assertEquals(2, doc.body().children().size());
        assertEquals("div", doc.body().children().get(0).tagName());
        assertEquals("p", doc.body().children().get(1).tagName());
    }

    // Tests invalid end tag being ignored
    @Test
    public void testParse_endTagWithoutMatch_ignoresInvalidClose() {
        Document doc = parse("<html><body><div>one</span>two</div></body></html>");
        assertEquals("onetwo", doc.body().text());
    }

    // Tests textarea data tag content preserved as text
    @Test
    public void testParse_textareaContent_preservesRawText() {
        Document doc = parse("<html><body><textarea><b>bold</b></textarea></body></html>");
        Element textarea = doc.getElementsByTag("textarea").first();
        assertNotNull(textarea);
        assertEquals("<b>bold</b>", textarea.text());
    }

    // Tests script data tag content preserved as data
    @Test
    public void testParse_scriptContent_preservesRawData() {
        Document doc = parse("<html><body><script>if (a < b) { x(); }</script></body></html>");
        assertTrue(doc.toString().contains("if (a < b)"));
    }

    // Tests comment parsing
    @Test
    public void testParse_comment_preservesComment() {
        Document doc = parse("<html><body><!--comment--></body></html>");
        assertTrue(doc.toString().contains("<!--comment-->"));
    }

    // Tests CDATA parsing
    @Test
    public void testParse_cdata_preservesCdataText() {
        Document doc = parseBody("<![CDATA[x < y]]>");
        assertEquals("x < y", doc.body().text());
    }

    // Tests base href updating the base URI for later relative links
    @Test
    public void testParse_baseTag_updatesBaseUriForRelativeLinks() {
        Document doc = parse("<html><head><base href=\"http://example.com/base/\"></head><body><a href=\"page.html\">x</a></body></html>");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertEquals("http://example.com/base/page.html", link.absUrl("href"));
    }

    // Tests XML processing instruction preservation
    @Test
    public void testParse_xmlDeclaration_preservesProcessingInstruction() {
        Document doc = parse("<?xml version=\"1.0\"?><html><body>x</body></html>");
        assertTrue(doc.toString().contains("<?xml"));
    }

    // Tests DOCTYPE declaration preservation
    @Test
    public void testParse_doctypeDeclaration_preservesDoctype() {
        Document doc = parse("<!DOCTYPE html><html><body>x</body></html>");
        assertTrue(doc.toString().contains("<!DOCTYPE"));
    }
}