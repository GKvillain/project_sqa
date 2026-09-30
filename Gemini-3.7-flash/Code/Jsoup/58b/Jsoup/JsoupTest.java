package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class JsoupTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // Tests parsing simple HTML string
    @Test
    public void testParse_simpleHtml_returnsParsedDocument() {
        String html = "<html><head><title>Test Title</title></head><body><p>Hello World</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("Hello World", doc.select("p").text());
    }

    // Tests parsing HTML with base URI
    @Test
    public void testParse_htmlWithBaseUri_resolvesAbsoluteUrl() {
        String html = "<a href='/test.html'>Link</a>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        assertEquals("http://example.com/test.html", doc.select("a").first().absUrl("href"));
    }

    // Tests parsing HTML using custom XML parser
    @Test
    public void testParse_withXmlParser_parsesXmlStructure() {
        String xml = "<root><item id='1'>Value</item></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("Value", doc.select("item").text());
        assertEquals("1", doc.select("item").attr("id"));
    }

    // Tests parsing body fragment without base URI
    @Test
    public void testParseBodyFragment_validFragment_placesInBody() {
        String fragment = "<div><p>Fragment Paragraph</p></div>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        assertEquals("Fragment Paragraph", doc.body().select("p").text());
    }

    // Tests parsing body fragment with base URI
    @Test
    public void testParseBodyFragment_withBaseUri_resolvesRelativeLinks() {
        String fragment = "<a href='sub/page.html'>Link</a>";
        Document doc = Jsoup.parseBodyFragment(fragment, "http://example.com/dir/");
        assertEquals("http://example.com/dir/sub/page.html", doc.body().select("a").first().absUrl("href"));
    }

    // Tests cleaning unsafe HTML with basic whitelist
    @Test
    public void testClean_unsafeHtml_stripsUnsafeTags() {
        String unsafeHtml = "<p><a href='http://example.com/' onclick='steal()'>Link</a><script>alert(1);</script></p>";
        String cleanHtml = Jsoup.clean(unsafeHtml, Whitelist.basic());
        assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", cleanHtml);
    }

    // Tests cleaning HTML with custom output settings
    @Test
    public void testClean_withOutputSettings_preservesSettings() {
        String html = "<p>&amp; &lt; &gt;</p>";
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        String cleanHtml = Jsoup.clean(html, "", Whitelist.basic(), settings);
        assertEquals("<p>&amp; &lt; &gt;</p>", cleanHtml);
    }

    // Tests cleaning HTML with base URI to resolve relative links
    @Test
    public void testClean_relativeUrlWithBaseUri_resolvesAndCleans() {
        String html = "<a href='/foo'>Link</a>";
        String cleanHtml = Jsoup.clean(html, "http://example.com", Whitelist.basic());
        assertEquals("<a href=\"http://example.com/foo\" rel=\"nofollow\">Link</a>", cleanHtml);
    }

    // Tests validating safe HTML returns true
    @Test
    public void testIsValid_safeHtml_returnsTrue() {
        String safeHtml = "<p>This is <b>safe</b> text.</p>";
        assertTrue(Jsoup.isValid(safeHtml, Whitelist.basic()));
    }

    // Tests validating unsafe HTML returns false
    @Test
    public void testIsValid_unsafeHtml_returnsFalse() {
        String unsafeHtml = "<p>Text <script>alert(1);</script></p>";
        assertFalse(Jsoup.isValid(unsafeHtml, Whitelist.basic()));
    }

    // Tests creating connection instance
    @Test
    public void testConnect_validUrl_returnsConnection() {
        Connection con = Jsoup.connect("http://example.com");
        assertNotNull(con);
    }

    // Tests parsing from InputStream with charset and base URI
    @Test
    public void testParse_inputStream_parsesDocumentCorrectly() throws IOException {
        String html = "<html><head><title>Stream Test</title></head><body><p>Content</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(in, "UTF-8", "http://example.com/");
        assertEquals("Stream Test", doc.title());
        assertEquals("Content", doc.select("p").text());
    }

    // Tests parsing from InputStream with custom Parser
    @Test
    public void testParse_inputStreamWithParser_parsesXmlCorrectly() throws IOException {
        String xml = "<xml><data>Stream Data</data></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(in, "UTF-8", "http://example.com/", Parser.xmlParser());
        assertEquals("Stream Data", doc.select("data").text());
    }

    // Tests parsing from File with charset and base URI
    @Test
    public void testParse_fileWithBaseUri_parsesCorrectly() throws IOException {
        File file = tempFolder.newFile("test.html");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("<html><head><title>File Test</title></head><body><p>File Content</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
        assertEquals("File Test", doc.title());
        assertEquals("File Content", doc.select("p").text());
    }

    // Tests parsing from File without base URI
    @Test
    public void testParse_fileWithoutBaseUri_usesFileLocationAsBase() throws IOException {
        File file = tempFolder.newFile("test_nobase.html");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("<html><head><title>File No Base</title></head><body><p>Content</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(file, "UTF-8");
        assertEquals("File No Base", doc.title());
        assertEquals(file.getAbsolutePath(), doc.baseUri());
    }

    // Tests parsing URL with invalid scheme throws MalformedURLException
    @Test(expected = MalformedURLException.class)
    public void testParse_invalidUrlProtocol_throwsMalformedURLException() throws IOException {
        URL url = new URL("ftp://invalid.example.com");
        Jsoup.parse(url, 1000);
    }
}