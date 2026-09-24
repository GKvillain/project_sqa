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

    // Tests extracting valid charset from content type header
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        String contentType = "text/html; charset=utf-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    // Tests extracting charset when charset is enclosed in quotes
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        String contentType = "text/html; charset=\"iso-8859-1\"";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("ISO-8859-1", charset);
    }

    // Tests extracting charset when contentType contains extra attributes
    @Test
    public void testGetCharsetFromContentType_withSemicolonAndExtraParams_returnsCharset() {
        String contentType = "text/html; charset=gb2312; boundary=something";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("GB2312", charset);
    }

    // Tests extracting charset when contentType has spaces around equals sign
    @Test
    public void testGetCharsetFromContentType_spacesAroundEquals_returnsCharset() {
        String contentType = "text/html; charset = UTF-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    // Tests null input to getCharsetFromContentType
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests contentType without charset definition
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    // Tests unsupported charset name in contentType
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        String contentType = "text/html; charset=unsupported-charset-name";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertNull(charset);
    }

    // Tests parsing byte data with explicit charset specified
    @Test
    public void testParseByteData_explicitCharset_parsesCorrectly() {
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertEquals("Test", doc.title());
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    // Tests parsing byte data with empty charset string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer buffer = ByteBuffer.wrap("<html></html>".getBytes());
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    // Tests auto-detecting charset from meta http-equiv tag and re-decoding
    @Test
    public void testParseByteData_metaHttpEquivCharset_detectsAndReDecodes() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"><title>Caf\u00e9</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("Caf\u00e9", doc.title());
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    // Tests auto-detecting charset from HTML5 meta charset tag
    @Test
    public void testParseByteData_metaCharsetTag_detectsAndReDecodes() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>Caf\u00e9</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("Caf\u00e9", doc.title());
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    // Tests default UTF-8 when no meta charset is present
    @Test
    public void testParseByteData_noMetaCharset_defaultsToUtf8() {
        String html = "<html><head><title>Default UTF-8</title></head><body>Content</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("Default UTF-8", doc.title());
    }

    // Tests UTF-8 with Byte Order Mark (BOM) gets stripped properly
    @Test
    public void testParseByteData_withBom_stripsBom() {
        String html = "\uFEFF<html><head><title>BOM Test</title></head><body>Text</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("BOM Test", doc.title());
    }

    // Tests readToByteBuffer reads all stream bytes correctly
    @Test
    public void testReadToByteBuffer_validStream_readsCorrectBytes() throws IOException {
        byte[] expectedBytes = "Jsoup Stream Test Data".getBytes(Charset.forName("UTF-8"));
        InputStream stream = new ByteArrayInputStream(expectedBytes);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(stream);
        assertArrayEquals(expectedBytes, byteBuffer.array());
    }

    // Tests load from InputStream with default html parser
    @Test
    public void testLoad_inputStream_returnsDocument() throws IOException {
        String html = "<html><head><title>Stream Load</title></head><body>Body</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Stream Load", doc.title());
    }

    // Tests load from InputStream with custom XML parser
    @Test
    public void testLoad_inputStreamWithParser_returnsDocument() throws IOException {
        String xml = "<xml><title>XML Content</title></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("XML Content", doc.select("title").first().text());
    }

    // Tests load from File
    @Test
    public void testLoad_file_returnsDocument() throws IOException {
        File tempFile = File.createTempFile("jsoup-test", ".html");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        out.write("<html><head><title>File Load</title></head><body>File Body</body></html>".getBytes(Charset.forName("UTF-8")));
        out.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Load", doc.title());
    }
}