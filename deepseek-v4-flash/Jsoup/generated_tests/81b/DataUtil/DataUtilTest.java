package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests null input -> returns empty Document
    @Test
    public void testParseInputStream_nullInput_returnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.children().isEmpty());
    }

    // Tests empty stream -> returns empty Document
    @Test
    public void testParseInputStream_emptyInput_returnsEmptyDocument() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.children().isEmpty());
    }

    // Tests simple HTML parsing with default charset
    @Test
    public void testParseInputStream_simpleHtml_parsesCorrectly() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    // Tests charset detection from <meta charset="..."> tag, re-parse with different charset
    @Test
    public void testParseInputStream_metaCharset_usesDetectedCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>caf\u00e9</body></html>"; // café encoded in ISO-8859-1
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.body().text());
    }

    // Tests charset explicitly provided via load(InputStream, charsetName, baseUri)
    @Test
    public void testLoad_charsetFromParameter_usesGivenCharset() throws IOException {
        String content = "caf\u00e9"; // é = 0xE9 in ISO-8859-1
        byte[] bytes = content.getBytes(Charset.forName("ISO-8859-1"));
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.load(in, "ISO-8859-1", "http://example.com");
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.text());
    }

    // Tests UTF-8 BOM detection and offset handling
    @Test
    public void testParseInputStream_bomUtf8_usesUtf8AndSkipsBom() throws IOException {
        String content = "Hello";
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[bom.length + contentBytes.length];
        System.arraycopy(bom, 0, data, 0, bom.length);
        System.arraycopy(contentBytes, 0, data, bom.length, contentBytes.length);
        InputStream in = new ByteArrayInputStream(data);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests UTF-16 BOM detection (UTF-16 includes BOM)
    @Test
    public void testParseInputStream_bomUtf16_usesUtf16() throws IOException {
        String content = "Hello";
        byte[] data = ("\uFEFF" + content).getBytes(StandardCharsets.UTF_16);
        InputStream in = new ByteArrayInputStream(data);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests encoding declaration in XML prolog with xml parser
    @Test
    public void testParseInputStream_xmlDeclarationEncoding_usesEncoding() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>caf\u00e9</root>";
        byte[] bytes = xml.getBytes(Charset.forName("ISO-8859-1"));
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.text());
    }

    // Tests that when stream is fully read in first chunk, no re-parse occurs
    @Test
    public void testParseInputStream_fullyRead_doesNotReparse() throws IOException {
        String html = "<p>Hi</p>"; // small input (< firstReadBufferSize)
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hi", doc.body().text());
    }

    // Tests readToByteBuffer with negative maxSize -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests readToByteBuffer with maxSize=0 (unlimited) reads all data
    @Test
    public void testReadToByteBuffer_zeroMaxSize_readsAll() throws IOException {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        java.nio.ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertNotNull(buf);
        byte[] result = new byte[buf.remaining()];
        buf.get(result);
        assertArrayEquals(data, result);
    }

    // Tests getCharsetFromContentType with null input -> null
    @Test
    public void testGetCharsetFromContentType_null_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests getCharsetFromContentType with valid charset -> returns that charset
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsTrimmed() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", result);
    }

    // Tests getCharsetFromContentType with invalid charset -> null
    @Test
    public void testGetCharsetFromContentType_invalidCharset_returnsNull() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=invalid-charset");
        assertNull(result);
    }

    // Tests crossStreams copies data correctly
    @Test
    public void testCrossStreams_copiesDataCorrectly() throws IOException {
        byte[] data = "some bytes to copy".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    // Tests that mimeBoundary returns a string of correct length and valid characters
    @Test
    public void testMimeBoundary_hasLength32() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
        String allowed = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (char c : boundary.toCharArray()) {
            assertTrue("Invalid character in boundary: " + c, allowed.indexOf(c) >= 0);
        }
    }
}