package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataUtilTest {

    // Tests parsing null InputStream returns empty document with base URI
    @Test
    public void testParseInputStream_nullInput_returnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(0, doc.children().size());
    }

    // Tests loading basic HTML content with standard UTF-8 charset
    @Test
    public void testLoad_validHtmlInputStream_parsesCorrectly() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello World</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
    }

    // Tests loading file directly through DataUtil
    @Test
    public void testLoad_fromFile_parsesCorrectly() throws IOException {
        File tempFile = File.createTempFile("datautil-test", ".html");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("<html><body><div id='content'>File Test</div></body></html>".getBytes("UTF-8"));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.select("#content").first().text());
    }

    // Tests loading with XML parser parses XML declarations correctly
    @Test
    public void testLoad_withXmlParser_parsesAsXml() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>value</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertEquals("value", doc.select("child").first().text());
    }

    // Tests detection of UTF-8 charset from BOM header
    @Test
    public void testParseInputStream_utf8Bom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>BOM Test</p>".getBytes("UTF-8");
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        assertEquals("BOM Test", doc.select("p").first().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests detection of UTF-16 Big Endian charset from BOM header
    @Test
    public void testParseInputStream_utf16BeBom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] text = "<p>UTF-16 BE</p>".getBytes("UTF-16BE");
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        assertEquals("UTF-16 BE", doc.select("p").first().text());
    }

    // Tests charset detection from HTML meta http-equiv tag
    @Test
    public void testParseInputStream_metaHttpEquivCharset_detectsCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>é</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        assertEquals("é", doc.select("p").first().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests charset detection from HTML5 meta charset tag
    @Test
    public void testParseInputStream_metaCharsetHtml5_detectsCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>é</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        assertEquals("é", doc.select("p").first().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests charset detection from XML declaration in document
    @Test
    public void testParseInputStream_xmlDeclarationEncoding_detectsCharset() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><msg>é</msg></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());

        assertEquals("é", doc.select("msg").first().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests handling of charset that cannot encode (fallback to UTF-8 output settings)
    @Test
    public void testParseInputStream_unencodableCharset_fallsBackToSupportedEncoding() throws IOException {
        String html = "<html><head><meta charset=\"ISO-2022-CN\"></head><body><p>Unencodable</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        assertNotNull(doc);
        assertTrue(doc.outputSettings().charset().canEncode());
    }

    // Tests getCharsetFromContentType with valid charset in header
    @Test
    public void testGetCharsetFromContentType_validHeader_extractsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=\"GB2312\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
    }

    // Tests getCharsetFromContentType with null and malformed content type headers
    @Test
    public void testGetCharsetFromContentType_nullOrInvalid_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
    }

    // Tests readToByteBuffer with maxSize limit
    @Test
    public void testReadToByteBuffer_withMaxSize_limitsReadLength() throws IOException {
        byte[] data = "0123456789ABCDEF".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 10);

        assertEquals(10, byteBuffer.remaining());
        byte[] readBytes = new byte[10];
        byteBuffer.get(readBytes);
        assertEquals("0123456789", new String(readBytes, "UTF-8"));
    }

    // Tests readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsIllegalArgumentException() throws IOException {
        InputStream in = new ByteArrayInputStream("test".getBytes("UTF-8"));
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests emptyByteBuffer returns empty buffer with capacity 0
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacity() {
        ByteBuffer empty = DataUtil.emptyByteBuffer();
        assertNotNull(empty);
        assertEquals(0, empty.capacity());
        assertEquals(0, empty.remaining());
    }

    // Tests mimeBoundary creates a valid random string of correct length
    @Test
    public void testMimeBoundary_generatesRandomCorrectLengthString() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
        assertTrue(boundary.matches("^[-_a-zA-Z0-9]{32}$"));
    }

    // Tests crossStreams accurately copies all bytes between streams
    @Test
    public void testCrossStreams_copiesDataSuccessfully() throws IOException {
        byte[] sourceData = "CrossStreams payload data test".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(sourceData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        byte[] result = out.toByteArray();

        assertEquals(sourceData.length, result.length);
        assertEquals("CrossStreams payload data test", new String(result, "UTF-8"));
    }
}