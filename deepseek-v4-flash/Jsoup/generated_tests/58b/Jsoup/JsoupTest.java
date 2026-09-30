package org.jsoup;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.safety.Cleaner;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

public class JsoupTest {

    // Normal parse with baseUri, verify resolved link
    @Test
    public void testParse_withBaseUri_resolvesRelativeUrl() {
        String html = "<a href='/page'>link</a>";
        Document doc = Jsoup.parse(html, "http://example.com");
        assertEquals("http://example.com/page", doc.select("a").first().absUrl("href"));
    }

    // Normal parse without baseUri, title extracted
    @Test
    public void testParse_simpleHtml_returnsDocumentWithTitle() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    // Parse with custom parser (XML)
    @Test
    public void testParse_withCustomXmlParser_returnsDocument() {
        String xml = "<root><item>value</item></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.tagName());
    }

    // Parse with null parser - should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParse_nullParser_throwsNullPointerException() {
        Jsoup.parse("<html></html>", "", null);
    }

    // parseBodyFragment normal case
    @Test
    public void testParseBodyFragment_validHtml_returnsDocumentWithBody() {
        String bodyHtml = "<p>Hello</p>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        assertNotNull(doc.body());
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    // parseBodyFragment with null input - should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParseBodyFragment_nullInput_throwsNullPointerException() {
        Jsoup.parseBodyFragment(null);
    }

    // clean removes unsafe tags
    @Test
    public void testClean_removesUnsafeTags() {
        String dirty = "<script>alert('xss')</script><p>safe</p>";
        String clean = Jsoup.clean(dirty, Whitelist.basic());
        assertEquals("<p>safe</p>", clean);
    }

    // clean with baseUri and whitelist (relative URL preserved in cleaned output)
    @Test
    public void testClean_withBaseUriAndWhitelist_containsRelativeUrl() {
        String dirty = "<a href='/page'>link</a>";
        String clean = Jsoup.clean(dirty, "http://example.com", Whitelist.basicWithImages());
        assertTrue(clean.contains("/page"));
    }

    // clean with output settings (no pretty print)
    @Test
    public void testClean_withOutputSettings_noPrettyPrint() {
        String dirty = "<p>Hello</p>";
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        String clean = Jsoup.clean(dirty, "", Whitelist.none(), settings);
        assertEquals("<p>Hello</p>", clean);
    }

    // isValid returns true for allowed content
    @Test
    public void testIsValid_validHtml_returnsTrue() {
        String bodyHtml = "<p>valid</p>";
        assertTrue(Jsoup.isValid(bodyHtml, Whitelist.basic()));
    }

    // isValid returns false for disallowed content
    @Test
    public void testIsValid_invalidHtml_returnsFalse() {
        String bodyHtml = "<script>alert('xss')</script>";
        assertFalse(Jsoup.isValid(bodyHtml, Whitelist.basic()));
    }

    // connect returns non-null connection object
    @Test
    public void testConnect_validUrl_returnsConnection() {
        Connection conn = Jsoup.connect("http://example.com");
        assertNotNull(conn);
    }

    // connect with null url throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConnect_nullUrl_throwsIllegalArgumentException() {
        Jsoup.connect(null);
    }

    // parse(File, charsetName) with existing file
    @Test
    public void testParseFile_withCharset_returnsDocument() throws IOException {
        File temp = File.createTempFile("test", ".html");
        temp.deleteOnExit();
        Files.write(temp.toPath(), "<html><body>Hello</body></html>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(temp, "UTF-8");
        assertEquals("Hello", doc.body().text());
    }

    // parse(File, charsetName) with null charset (fallback)
    @Test
    public void testParseFile_withNullCharset_returnsDocument() throws IOException {
        File temp = File.createTempFile("test", ".html");
        temp.deleteOnExit();
        Files.write(temp.toPath(), "<html><body>Hello</body></html>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(temp, null);
        assertEquals("Hello", doc.body().text());
    }

    // parse(File, charsetName, baseUri) with non-existent file throws IOException
    @Test(expected = IOException.class)
    public void testParseFile_nonExistentFile_throwsIOException() throws IOException {
        File temp = File.createTempFile("temp", ".html");
        temp.delete(); // ensure file does not exist
        Jsoup.parse(temp, "UTF-8", "http://example.com");
    }

    // parse(InputStream, charsetName, baseUri) 
    @Test
    public void testParseInputStream_simpleHtml_returnsDocument() throws IOException {
        String html = "<html><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    // parse(InputStream, null charset, baseUri)
    @Test
    public void testParseInputStream_withNullCharset_returnsDocument() throws IOException {
        String html = "<html><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, null, "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    // parse(InputStream, charsetName, baseUri, parser) with xml parser
    @Test
    public void testParseInputStream_withParser_returnsDocument() throws IOException {
        String xml = "<root><item>value</item></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.tagName());
    }

    // parse(InputStream, ...) with null parser throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParseInputStream_nullParser_throwsNullPointerException() throws IOException {
        String html = "<html></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes());
        Jsoup.parse(in, "UTF-8", "", null);
    }
}