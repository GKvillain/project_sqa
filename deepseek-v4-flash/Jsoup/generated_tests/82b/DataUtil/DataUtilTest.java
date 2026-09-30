package org.jsoup.helper;

import static org.junit.Assert.*;

import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtilTest {

    // Helper to create a small HTML string for tests
    private String simpleHtml() {
        return "<html><head></head><body>Hello</body></html>";
    }

    // Helper to create an InputStream from a string with optional BOM bytes
    private InputStream inputStreamFromString(String html, byte[] bom, String charset) throws UnsupportedEncodingException {
        byte[] content = html.getBytes(charset);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bom != null) baos.write(bom, 0, bom.length);
        baos.write(content, 0, content.length);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    // ======================= parseInputStream tests =======================

    // Tests null input returns empty document with given baseUri
    @Test
    public void testParseInputStream_nullInput_returnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertTrue(doc.children().isEmpty());
    }

    // Tests simple HTML with charset specified
    @Test
    public void testParseInputStream_withCharset_parsesCorrectly() throws IOException {
        String html = simpleHtml();
        InputStream in = inputStreamFromString(html, null, "UTF-8");
        Document doc = DataUtil.parseInputStream(in, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests charset detection from meta tag when charsetName is null and body fits in first read
    @Test
    public void testParseInputStream_noCharset_metaFound_usesDetectedCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>\u00E4</body></html>"; // ä in ISO-8859-1
        byte[] content = html.getBytes("ISO-8859-1");
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("\u00E4", doc.body().text());
    }

    // Tests charset detection from XML declaration when no meta found
    @Test
    public void testParseInputStream_noCharset_xmlDeclaration_usesDetectedCharset() throws IOException {
        String html = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<html><body>\u00E4</body></html>";
        byte[] content = html.getBytes("ISO-8859-1");
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("\u00E4", doc.body().text());
    }

    // Tests that when fully read and no charset found, the first UTF-8 parse is kept
    @Test
    public void testParseInputStream_noCharset_fullyReadNoMeta_keepsFirstParse() throws IOException {
        String html = "<html><body>Hello</body></html>"; // fits in first read
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests that BOM UTF-8 with offset is skipped when re-parsing (fullyRead false)
    @Test
    public void testParseInputStream_bomUtf8_offset_skipped() throws IOException {
        // Provide a long HTML to ensure not fully read (> 5KB)
        // We'll just use a short one but it will be fully read; we need to force not fully read.
        // Since we can't change buffer size, we'll use a stream longer than 5KB.
        String longHtml = "<html><head></head><body>";
        for (int i = 0; i < 10000; i++) longHtml += "a";
        longHtml += "</body></html>";
        byte[] content = longHtml.getBytes("UTF-8");
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bom, 0, bom.length);
        baos.write(content, 0, content.length);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());

        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        String bodyText = doc.body().text();
        // BOM should be stripped; if present, first char would be \uFEFF
        assertFalse("Found BOM character in parsed text", bodyText.startsWith("\uFEFF"));
    }

    // Tests BOM UTF-16 (no offset)
    @Test
    public void testParseInputStream_bomUtf16_noOffset() throws IOException {
        String html = "<html><body>Hello</body></html>";
        byte[] bom = new byte[] {(byte)0xFE, (byte)0xFF}; // UTF-16 BE
        byte[] content = html.getBytes("UTF-16BE");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bom, 0, bom.length);
        baos.write(content, 0, content.length);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());

        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests BOM UTF-32
    @Test
    public void testParseInputStream_bomUtf32() throws IOException {
        String html = "<html><body>Hello</body></html>";
        byte[] bom = new byte[] {0x00, 0x00, (byte)0xFE, (byte)0xFF}; // UTF-32 BE
        byte[] content = html.getBytes("UTF-32BE");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bom, 0, bom.length);
        baos.write(content, 0, content.length);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());

        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests BOM overrides provided charsetName
    @Test
    public void testParseInputStream_bomOverridesProvidedCharset() throws IOException {
        String html = "<html><body>\u00E4</body></html>"; // Latin-1 char in ISO-8859-1
        byte[] content = html.getBytes("ISO-8859-1");
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF}; // UTF-8 BOM
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bom, 0, bom.length);
        baos.write(content, 0, content.length);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());

        // Provide ISO-8859-1 as charset, but BOM should override to UTF-8
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // If BOM overrode, the stream will be read as UTF-8, which is invalid for the bytes, so text may be garbled
        // We just check no exception and document is created; the exact result may be garbage, but we verify that the BOM overrides.
        // A more reliable test: use a document that is valid UTF-8 after BOM, then check it's parsed correctly.
        // We'll do another test with proper UTF-8 content after BOM.
    }

    // Tests BOM UTF-8 with proper content after BOM to verify override works correctly
    @Test
    public void testParseInputStream_bomUtf8WithProperContent_parsesCorrectly() throws IOException {
        String html = "<html><body>Hello</body></html>";
        byte[] content = html.getBytes("UTF-8");
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bom, 0, bom.length);
        baos.write(content, 0, content.length);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());

        // Provide wrong charset, but BOM should override to UTF-8
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests that when charset detected from meta equals default and not fully read, doc is null and re-parsed
    @Test
    public void testParseInputStream_metaCharsetDefault_notFullyRead_reparses() throws IOException {
        // Create a stream that is not fully read (long body) with meta charset = UTF-8
        String htmlPart = "<html><head><meta charset=\"UTF-8\"></head><body>";
        String body = "";
        for (int i = 0; i < 10000; i++) body += "a";
        htmlPart += body;
        htmlPart += "</body></html>";
        InputStream in = new ByteArrayInputStream(htmlPart.getBytes("UTF-8"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals(10000, doc.body().text().length());
    }

    // ======================= getCharsetFromContentType tests =======================

    @Test
    public void testGetCharsetFromContentType_validHeader_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=EUC-JP");
        assertEquals("EUC-JP", charset);
    }

    @Test
    public void testGetCharsetFromContentType_null_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test
    public void testGetCharsetFromContentType_charsetWithQuotes_returnsTrimmed() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\"");
        assertEquals("UTF-8", charset);
    }

    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        // "unknown" is not a valid charset
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unknown"));
    }

    // ======================= mimeBoundary tests =======================

    @Test
    public void testMimeBoundary_returns32CharString() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
        // Should only contain allowed characters
        assertTrue(boundary.matches("[-_0-9a-zA-Z]+"));
    }

    @Test
    public void testMimeBoundary_differentCalls_differentResults() {
        String b1 = DataUtil.mimeBoundary();
        String b2 = DataUtil.mimeBoundary();
        // Very unlikely to collide
        assertNotEquals(b1, b2);
    }

    // ======================= readToByteBuffer tests =======================

    @Test
    public void testReadToByteBuffer_validInput_returnsBuffer() throws IOException {
        byte[] data = "Hello World".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 100);
        assertEquals(11, buffer.remaining());
        byte[] out = new byte[buffer.remaining()];
        buffer.get(out);
        assertArrayEquals(data, out);
    }

    @Test
    public void testReadToByteBuffer_maxSizeZero_returnsAll() throws IOException {
        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 0);
        assertEquals(1000, buffer.remaining());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throws() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[1]), -1);
    }

    @Test
    public void testReadToByteBuffer_limitedMaxSize_stopsAtMax() throws IOException {
        byte[] data = new byte[100];
        new Random().nextBytes(data);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 50);
        assertEquals(50, buffer.remaining());
    }

    // ======================= crossStreams tests =======================

    @Test
    public void testCrossStreams_copiesData() throws IOException {
        byte[] data = "Test data".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testCrossStreams_largeData_copies() throws IOException {
        byte[] data = new byte[100000];
        new Random().nextBytes(data);
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    // ======================= emptyByteBuffer tests =======================

    @Test
    public void testEmptyByteBuffer_returnsEmptyBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertEquals(0, buffer.remaining());
        assertEquals(0, buffer.capacity());
    }

    // ======================= detectCharsetFromBom (private, cannot test directly) =======================
    // We rely on parseInputStream tests covering BOM detection indirectly.

    // ======================= exception path test =======================
    // Test that parseInputStream propagates IOException from underlying stream during first read
    @Test(expected = IOException.class)
    public void testParseInputStream_ioExceptionDuringRead_throws() throws IOException {
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated failure");
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("Simulated failure");
            }
        };
        DataUtil.parseInputStream(failingStream, null, "http://example.com", Parser.htmlParser());
    }
}