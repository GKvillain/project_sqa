package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ParserTest {

    @Test
    public void testParse_simpleHtml_returnsDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseBodyFragment_simpleFragment_returnsBodyContent() {
        String bodyHtml = "<p>Fragment</p>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://base.com/");
        assertNotNull(doc);
        assertEquals("Fragment", doc.body().text());
    }

    @Test
    public void testParseBodyFragmentRelaxed_doesNotCreateImplicitParents() {
        String bodyHtml = "<p>Relaxed</p>";
        Document doc = Parser.parseBodyFragmentRelaxed(bodyHtml, "http://base.com/");
        assertNotNull(doc);
        assertEquals("Relaxed", doc.body().text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://base.com/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<html></html>", null);
    }

    @Test
    public void testParse_withComment_ignoresComment() {
        String html = "<html><body><!-- comment --><p>Text</p></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertEquals("Text", doc.body().text());
    }

    @Test
    public void testParse_withCdata_parsesRawText() {
        String html = "<html><body><![CDATA[raw text]]></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertEquals("raw text", doc.body().text());
    }

    @Test
    public void testParse_withXmlDeclaration_doesNotFail() {
        String html = "<?xml version=\"1.0\"?><html><body>Text</body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertNotNull(doc);
    }

    @Test
    public void testParse_withTextarea_parsesContentAsText() {
        String html = "<html><body><textarea>some text</textarea></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        Element ta = doc.body().select("textarea").first();
        assertNotNull(ta);
        assertEquals("some text", ta.text());
    }

    @Test
    public void testParse_withSelfClosingTag_marksEmpty() {
        String html = "<html><body><br/></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        Element br = doc.body().select("br").first();
        assertNotNull(br);
        assertTrue(br.tag().isEmpty());
    }

    @Test
    public void testParse_withBaseTag_updatesBaseUri() {
        String html = "<html><head><base href='http://newbase.com/'></head><body></body></html>";
        Document doc = Parser.parse(html, "http://oldbase.com/");
        assertEquals("http://newbase.com/", doc.baseUri());
    }

    @Test
    public void testParse_withImplicitParent_createsMissingAncestors() {
        String html = "<p>No html/head/body</p>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertNotNull(doc.select("html").first());
        assertNotNull(doc.select("body").first());
        assertEquals("No html/head/body", doc.body().text());
    }

    @Test
    public void testParseBodyFragmentRelaxed_skipsAncestorValidation() {
        String bodyHtml = "<td>cell</td>";
        Document doc = Parser.parseBodyFragmentRelaxed(bodyHtml, "http://base.com/");
        Element td = doc.body().select("td").first();
        assertNotNull(td);
        assertEquals("body", td.parent().tagName());
    }

    @Test
    public void testParse_withEndTagWithoutMatch_ignored() {
        String html = "<html><body><p>Text</b></p></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertEquals("Text", doc.body().text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_withEmptyTagName_throwsException() {
        Parser.parse("< >", "http://base.com/");
    }

    @Test
    public void testParse_withUnquotedAttribute_parsesCorrectly() {
        String html = "<html><body><div class=myClass>Content</div></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        Element div = doc.body().select("div").first();
        assertNotNull(div);
        assertEquals("myClass", div.attr("class"));
        assertEquals("Content", div.text());
    }

    @Test
    public void testParse_withSingleQuotedAttribute_parsesCorrectly() {
        String html = "<html><body><div class='myClass'>Content</div></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        Element div = doc.body().select("div").first();
        assertNotNull(div);
        assertEquals("myClass", div.attr("class"));
        assertEquals("Content", div.text());
    }

    @Test
    public void testParse_textStartingWithLessThan_handlesCorrectly() {
        String html = "<html><body><p>hello < there</p></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertEquals("hello < there", doc.body().text());
    }

    @Test
    public void testParseBodyFragment_emptyString_returnsEmptyBody() {
        Document doc = Parser.parseBodyFragment("", "http://base.com/");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test
    public void testParse_withNestedTags_closesCorrectly() {
        String html = "<html><body><div><span>Inner</span></div></body></html>";
        Document doc = Parser.parse(html, "http://base.com/");
        assertEquals("Inner", doc.body().text());
        Element div = doc.body().select("div").first();
        assertNotNull(div);
        Element span = div.select("span").first();
        assertNotNull(span);
    }
}