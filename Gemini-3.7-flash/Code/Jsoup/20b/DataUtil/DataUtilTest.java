package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests extracting charset from valid Content-Type header with standard formatting
    @Test
    public void testGetCharsetFromContentType_standardHeader_returnsCharset() {
        String contentType = "text/html; charset=utf-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    // Tests extracting charset with quotes and varied whitespace
    @Test
    public void testGetCharsetFromContentType_quotedAndSpaces_returnsCharset() {
        String contentType = "text/html; charset=\"ISO-8859-1\"";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("ISO-8859-1", charset);
    }

    // Tests null content type returns null
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests content type without charset returns null
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; text/plain"));
    }

    // Tests content type with empty charset value returns empty string
    @Test
    public void testGetCharsetFromContentType_emptyCharset_returnsEmpty() {
        String contentType = "text/html; charset=";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("", charset);
    }

    // Tests parsing HTML with UTF-8 Byte Order Mark (BOM) to detect BOM handling defect
    @Test
    public void testParseByteData_utf8WithBom_stripsBomCorrectly() {
        String html = "\uFEFF<html><head><title>BOM Test</title></head><body><p>Hello</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("BOM Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
        assertEquals(0, doc.head().children().select("p").size());
    }

    // Tests parsing HTML with meta content-type specifying alternate charset (re-decoding branch)
    @Test
    public void testParseByteData_metaHttpEquivIso8859_redecodesCorrectly() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>\u00E9\u00E8</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("\u00E9\u00E8", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing HTML with HTML5 meta charset attribute
    @Test
    public void testParseByteData_metaCharsetHtml5_redecodesCorrectly() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>\u00E7\u00E0</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("\u00E7\u00E0", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing with explicitly specified charset (skips meta sniffing)
    @Test
    public void testParseByteData_explicitCharset_parsesCorrectly() {
        String html = "<html><head><title>Explicit Charset</title></head><body><p>\u00FC</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com", Parser.htmlParser());

        assertEquals("Explicit Charset", doc.title());
        assertEquals("\u00FC", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests empty charset argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer buffer = ByteBuffer.wrap("<html></html>".getBytes());
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    // Tests loading HTML from InputStream with default html parser
    @Test
    public void testLoad_inputStream_loadsDocumentCorrectly() throws IOException {
        String html = "<html><head><title>Stream Load</title></head><body>Content</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertEquals("Stream Load", doc.title());
        assertEquals("Content", doc.body().text());
    }

    // Tests loading XML from InputStream with XML parser
    @Test
    public void testLoad_inputStreamWithXmlParser_loadsXmlDocument() throws IOException {
        String xml = "<xml><item>Value</item></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertEquals("Value", doc.select("item").text());
    }

    // Tests loading HTML from a File
    @Test
    public void testLoad_file_loadsDocumentCorrectly() throws IOException {
        File tempFile = File.createTempFile("dataUtilTest", ".html");
        tempFile.deleteOnExit();

        String html = "<html><head><title>File Load</title></head><body>File Body</body></html>";
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(html.getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");

        assertEquals("File Load", doc.title());
        assertEquals("File Body", doc.body().text());
    }

    // Tests readToByteBuffer reads all bytes properly
    @Test
    public void testReadToByteBuffer_validStream_readsAllBytes() throws IOException {
        byte[] expected = "Testing readToByteBuffer helper functionality".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(expected);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in);

        assertEquals(expected.length, buffer.remaining());
        byte[] actual = new byte[buffer.remaining()];
        buffer.get(actual);
        assertArrayEquals(expected, actual);
    }
}