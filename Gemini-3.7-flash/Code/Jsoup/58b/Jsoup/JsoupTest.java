package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JsoupTest {

    // Tests parsing HTML string without base URI
    @Test
    public void testParse_htmlString_returnsDocument() {
        Document doc = Jsoup.parse("<p>Hello World</p>");
        assertEquals("Hello World", doc.select("p").text());
    }

    // Tests parsing HTML string with base URI to resolve relative links
    @Test
    public void testParse_htmlStringWithBaseUri_resolvesAbsoluteUrl() {
        String html = "<a href='/path/page.html'>Link</a>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        assertEquals("http://example.com/path/page.html", doc.select("a").first().absUrl("href"));
    }

    // Tests parsing XML string using custom XML parser
    @Test
    public void testParse_xmlStringWithXmlParser_preservesXmlStructure() {
        String xml = "<root><child id='1'/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(1, doc.select("child").size());
        assertEquals("1", doc.select("child").attr("id"));
    }

    // Tests parseBodyFragment creates body structure properly
    @Test
    public void testParseBodyFragment_simpleFragment_createsBody() {
        Document doc = Jsoup.parseBodyFragment("<div><span>Test</span></div>");
        assertEquals("Test", doc.body().select("span").text());
    }

    // Tests parseBodyFragment with base URI resolves absolute link inside fragment
    @Test
    public void testParseBodyFragment_withBaseUri_resolvesAbsoluteUrl() {
        Document doc = Jsoup.parseBodyFragment("<a href='sub/index.html'>Click</a>", "http://example.com/dir/");
        assertEquals("http://example.com/dir/sub/index.html", doc.body().select("a").first().absUrl("href"));
    }

    // Tests connect returns valid Connection object
    @Test
    public void testConnect_validUrl_returnsConnection() {
        Connection con = Jsoup.connect("http://example.com");
        assertNotNull(con);
    }

    // Tests parsing from InputStream
    @Test
    public void testParse_inputStream_returnsParsedDocument() throws IOException {
        String html = "<html><body><h1>Title</h1></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "http://example.com");
        assertEquals("Title", doc.select("h1").text());
    }

    // Tests parsing from InputStream with XML Parser
    @Test
    public void testParse_inputStreamWithParser_parsesCustomXml() throws IOException {
        String xml = "<data><item>Value</item></data>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("Value", doc.select("item").text());
    }

    // Tests parsing from File with default baseUri
    @Test
    public void testParse_file_returnsParsedDocument() throws IOException {
        File tempFile = File.createTempFile("jsoup_test", ".html");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        try {
            out.write("<p>File Content</p>".getBytes("UTF-8"));
        } finally {
            out.close();
        }

        Document doc = Jsoup.parse(tempFile, "UTF-8");
        assertEquals("File Content", doc.select("p").text());
    }

    // Tests parsing from File with custom baseUri
    @Test
    public void testParse_fileWithBaseUri_returnsParsedDocument() throws IOException {
        File tempFile = File.createTempFile("jsoup_test_base", ".html");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        try {
            out.write("<a href='relative.html'>Link</a>".getBytes("UTF-8"));
        } finally {
            out.close();
        }

        Document doc = Jsoup.parse(tempFile, "UTF-8", "http://example.com/");
        assertEquals("http://example.com/relative.html", doc.select("a").first().absUrl("href"));
    }

    // Tests cleaning unsafe HTML with basic Whitelist
    @Test
    public void testClean_unsafeHtml_removesDisallowedTags() {
        String unsafe = "<p><script>alert('xss');</script>Valid Text</p>";
        String safe = Jsoup.clean(unsafe, Whitelist.basic());
        assertEquals("<p>Valid Text</p>", safe);
    }

    // Tests cleaning HTML with base URI
    @Test
    public void testClean_withBaseUri_resolvesRelativeLinks() {
        String unsafe = "<a href='/wiki/Main_Page'>Link</a>";
        String safe = Jsoup.clean(unsafe, "http://example.com", Whitelist.basic());
        assertEquals("<a href=\"http://example.com/wiki/Main_Page\" rel=\"nofollow\">Link</a>", safe);
    }

    // Tests cleaning HTML with OutputSettings
    @Test
    public void testClean_withOutputSettings_appliesSettings() {
        String html = "<p>Line1\nLine2</p>";
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        String safe = Jsoup.clean(html, "", Whitelist.relaxed(), settings);
        assertEquals("<p>Line1\nLine2</p>", safe);
    }

    // Tests isValid with completely valid HTML fragment
    @Test
    public void testIsValid_validHtml_returnsTrue() {
        String validHtml = "<p><a href=\"http://example.com/\">Link</a></p>";
        assertTrue(Jsoup.isValid(validHtml, Whitelist.basic()));
    }

    // Tests isValid with unsafe HTML containing script tag
    @Test
    public void testIsValid_invalidHtmlWithScript_returnsFalse() {
        String invalidHtml = "<p>Text <script>alert(1);</script></p>";
        assertFalse(Jsoup.isValid(invalidHtml, Whitelist.basic()));
    }

    // Tests isValid with disallowed attributes on valid tags
    @Test
    public void testIsValid_disallowedAttribute_returnsFalse() {
        String invalidHtml = "<p onclick=\"alert('click')\">Text</p>";
        assertFalse(Jsoup.isValid(invalidHtml, Whitelist.basic()));
    }
}