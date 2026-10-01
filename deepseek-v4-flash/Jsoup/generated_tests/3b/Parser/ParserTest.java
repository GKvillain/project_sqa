package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserTest {

    // Normal case: parse simple HTML with html, head, body, title
    @Test
    public void testParse_simpleHtml_returnsDocumentWithCorrectStructure() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    // Boundary: null html
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://example.com");
    }

    // Boundary: null baseUri
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<html></html>", null);
    }

    // Edge: empty html
    @Test
    public void testParse_emptyHtml_returnsEmptyDocument() {
        Document doc = Parser.parse("", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.html().isEmpty());
    }

    // Edge: HTML with comment
    @Test
    public void testParse_htmlWithComment_createsCommentNode() {
        String html = "<html><body><!-- comment --><p>text</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // comment should be inside body, but text still present
        assertEquals("text", doc.body().text());
    }

    // Edge: HTML with CDATA
    @Test
    public void testParse_htmlWithCdata_createsTextNodeWithRawText() {
        String html = "<html><body><![CDATA[<raw>]]></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // CDATA content should appear as text (not parsed as tag)
        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("<raw>"));
    }

    // Edge: HTML with self-closing tag (img)
    @Test
    public void testParse_htmlWithSelfClosingTag_doesNotAddToStack() {
        String html = "<html><body><img src='a' /></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        Elements imgs = doc.select("img");
        assertEquals(1, imgs.size());
        assertTrue(imgs.first().hasAttr("src"));
    }

    // Edge: HTML with data tag (textarea) - lower case
    @Test
    public void testParse_htmlWithDataTagTextarea_parsesTextNode() {
        String html = "<html><body><textarea>some text</textarea></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // textarea content should be a text node, not HTML inside
        assertEquals("some text", doc.select("textarea").first().text());
    }

    // Edge: HTML with uppercase data tag (textarea) - potential bug case
    @Test
    public void testParse_htmlWithUppercaseDataTag_parsesCorrectly() {
        // This test targets case-insensitive matching of closing tag (defect 3b)
        String html = "<html><body><TEXTAREA>content</TEXTAREA></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("content", doc.select("textarea").first().text());
    }

    // Edge: HTML with data tag (script) - should create DataNode, not escaped
    @Test
    public void testParse_htmlWithScriptTag_createsDataNode() {
        String html = "<html><body><script>var x = \"<test>\";</script></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // script content should be raw, not HTML-escaped
        String scriptContent = doc.select("script").first().html();
        assertTrue(scriptContent.contains("\"<test>\""));
    }

    // Edge: HTML with base tag updates base URI
    @Test
    public void testParse_htmlWithBaseTag_updatesBaseUri() {
        String html = "<html><head><base href='http://newbase.com/'></head><body><a href='/page'>link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // The base href should update the document's base URI
        assertEquals("http://newbase.com/", doc.baseUri());
    }

    // Edge: HTML with attributes (single and double quotes)
    @Test
    public void testParse_htmlWithAttributes_parsesValues() {
        String html = "<html><body><div id='main' class=\"container\"></div></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.select("div").first();
        assertEquals("main", div.attr("id"));
        assertEquals("container", div.attr("class"));
    }

    // Edge: Unclosed tag (no closing tag) - should still produce document
    @Test
    public void testParse_htmlWithUnclosedTag_handlesGracefully() {
        String html = "<html><body><p>unclosed";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("unclosed", doc.body().text());
    }

    // Edge: Body fragment parsing
    @Test
    public void testParseBodyFragment_simpleHtml_returnsDocumentWithBodyOnly() {
        String bodyHtml = "<p>fragment</p>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://example.com");
        assertNotNull(doc);
        // The document should have an empty head and parsed body
        assertTrue(doc.head().children().isEmpty());
        assertEquals("fragment", doc.body().text());
    }

    // Edge: DOCTYPE handling (though it's parsed as XML declaration)
    @Test
    public void testParse_htmlWithDoctype_parsesAsXmlDeclaration() {
        String html = "<!DOCTYPE html><html><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // The DOCTYPE ends up as an XmlDeclaration child of document (or html?)
        // Just ensure no exception and document exists
        assertNotNull(doc);
        // The document should have the <html> element
        assertNotNull(doc.select("html").first());
    }

    // Edge: End tag with missing name (e.g., "</>") should be ignored
    @Test
    public void testParse_htmlWithEmptyEndTag_ignores() {
        String html = "<html><body><p>text</></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // The invalid end tag should be ignored; <p> remains unclosed
        assertEquals("text", doc.body().text());
    }

    // Edge: Attribute with empty key (e.g., <div '='value'>) - should skip
    @Test
    public void testParse_htmlWithEmptyAttributeKey_skipsAttribute() {
        String html = "<html><body><div ='value'></div></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        // The attribute with empty key should be ignored
        Element div = doc.select("div").first();
        assertEquals(0, div.attributes().size());
    }

    // ===== New tests for uncovered areas =====

    @Test
    public void testParse_htmlWithCharacterEntities_resolvesCorrectly() {
        String html = "<html><body><p>&amp; &lt; &gt; &quot; &#65; &#x41;</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        String text = doc.body().text();
        assertEquals("& < > \" A A", text);
    }

    @Test
    public void testParse_htmlWithVoidElements_brHrInput() {
        String html = "<html><body>line1<br>line2<hr><input type='text' value='x'></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        Elements brs = doc.select("br");
        assertEquals(1, brs.size());
        Elements hrs = doc.select("hr");
        assertEquals(1, hrs.size());
        Elements inputs = doc.select("input");
        assertEquals(1, inputs.size());
        assertEquals("x", inputs.first().attr("value"));
    }

    @Test
    public void testParse_htmlWithStyleElement_createsDataNode() {
        String html = "<html><head><style>body { color: red; }</style></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        Element style = doc.select("style").first();
        assertNotNull(style);
        assertTrue(style.html().contains("color: red;"));
        assertEquals("body { color: red; }", style.data().trim());
    }

    @Test
    public void testParse_htmlWithMismatchedTags_autoCloses() {
        String html = "<html><body><b><i>bold italic</b></i></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        Element b = doc.select("b").first();
        assertNotNull(b);
        Elements i = b.select("i");
        assertEquals(1, i.size());
        assertEquals("bold italic", i.first().text());
    }

    @Test
    public void testParse_htmlWithBooleanAttributes_parsedAsEmptyString() {
        String html = "<html><body><input disabled checked readonly></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        Element input = doc.select("input").first();
        assertNotNull(input);
        assertEquals("", input.attr("disabled"));
        assertEquals("", input.attr("checked"));
        assertEquals("", input.attr("readonly"));
    }

    @Test
    public void testParse_htmlWithMultipleRootNodes_mergedIntoBody() {
        String html = "<p>first</p><p>second</p>";
        Document doc = Parser.parse(html, "http://example.com");
        Elements ps = doc.select("p");
        assertEquals(2, ps.size());
        assertEquals("first", ps.get(0).text());
        assertEquals("second", ps.get(1).text());
        assertEquals(2, doc.body().children().size());
    }

    @Test
    public void testParse_htmlWithTableAndParagraph_fosterParenting() {
        String html = "<table><tr><td>cell</td><p>paragraph</p></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");
        Elements ps = doc.select("p");
        assertEquals(1, ps.size());
        assertNotNull(ps.first());
        assertFalse(ps.first().parents().stream().anyMatch(e -> e.tagName().equals("table")));
    }

    @Test
    public void testParse_htmlWithNestedLists_parsedCorrectly() {
        String html = "<ul><li>item1<ul><li>nested</li></ul></li></ul>";
        Document doc = Parser.parse(html, "http://example.com");
        Elements items = doc.select("li");
        assertEquals(2, items.size());
        assertEquals("item1", items.get(0).text());
        assertEquals("nested", items.get(1).text());
        Elements nestedUl = items.get(0).select("ul");
        assertEquals(1, nestedUl.size());
    }
}