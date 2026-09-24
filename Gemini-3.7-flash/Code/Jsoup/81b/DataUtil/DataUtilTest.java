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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataUtilTest {

    // Tests parsing input stream with null input returns empty document
    @Test
    public void testParseInputStream_nullInput_returnsDocumentWithBaseUri() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(0, doc.children().size());
    }

    // Tests loading HTML input stream with explicit charset
    @Test
    public void testLoad_explicitCharset_parsesCorrectly() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests loading HTML with empty charset argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testLoad_emptyCharset_throwsException() throws IOException {
        String html = "<p>Hello</p>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        DataUtil.load(in, "", "http://example.com");
    }

    // Tests detecting charset from HTML meta charset tag
    @Test
    public void testParseInputStream_metaCharset_detectsAndReencodes() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>café</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.select("p").text());
    }

    // Tests detecting charset from HTML http-equiv content-type meta tag
    @Test
    public void testParseInputStream_httpEquivMetaCharset_detectsCorrectly() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>été</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("été", doc.select("p").text());
    }

    // Tests detecting charset from XML declaration using xmlParser
    @Test
    public void testParseInputStream_xmlDeclarationEncoding_detectsCorrectly() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><data><item>café</item></data>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.select("item").text());
    }

    // Tests detecting UTF-8 BOM charset and skipping BOM offset
    @Test
    public void testParseInputStream_utf8Bom_detectsAndSkipsBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "<p>Hello BOM</p>".getBytes(StandardCharsets.UTF_8);
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(all), null, "http://example.com");
        assertEquals("Hello BOM", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests detecting UTF-16 BE BOM
    @Test
    public void testParseInputStream_utf16BeBom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] content = "<p>Hello UTF-16</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(all), null, "http://example.com");
        assertEquals("UTF-16", doc.outputSettings().charset().name());
        assertEquals("Hello UTF-16", doc.select("p").text());
    }

    // Tests detecting UTF-16 LE BOM
    @Test
    public void testParseInputStream_utf16LeBom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] content = "<p>Hello UTF-16 LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(all), null, "http://example.com");
        assertEquals("UTF-16", doc.outputSettings().charset().name());
        assertEquals("Hello UTF-16 LE", doc.select("p").text());
    }

    // Tests loading a file
    @Test
    public void testLoad_file_parsesCorrectly() throws IOException {
        File temp = File.createTempFile("jsoup-test", ".html");
        temp.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write("<html><body><p>File content</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(temp, "UTF-8", "http://example.com");
        assertEquals("File content", doc.select("p").text());
    }

    // Tests crossStreams copies all bytes from input to output
    @Test
    public void testCrossStreams_copiesContent() throws IOException {
        byte[] expected = "Testing cross streams data transfer".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertEquals(new String(expected, StandardCharsets.UTF_8), new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    // Tests readToByteBuffer with maxSize limit
    @Test
    public void testReadToByteBuffer_withMaxSize_readsUpToLimit() throws IOException {
        byte[] data = "1234567890".getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data), 5);
        assertEquals(5, buffer.remaining());
        byte[] result = new byte[5];
        buffer.get(result);
        assertEquals("12345", new String(result, StandardCharsets.UTF_8));
    }

    // Tests readToByteBuffer with maxSize 0 reads all
    @Test
    public void testReadToByteBuffer_withZeroMaxSize_readsAll() throws IOException {
        byte[] data = "Full content".getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data));
        assertEquals(data.length, buffer.remaining());
    }

    // Tests readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]), -1);
    }

    // Tests readFileToByteBuffer reads entire file
    @Test
    public void testReadFileToByteBuffer_validFile_returnsByteBuffer() throws IOException {
        File temp = File.createTempFile("jsoup-buffer-test", ".tmp");
        temp.deleteOnExit();
        byte[] data = "File buffer data".getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write(data);
        }

        ByteBuffer buffer = DataUtil.readFileToByteBuffer(temp);
        assertEquals(data.length, buffer.remaining());
        byte[] readBack = new byte[buffer.remaining()];
        buffer.get(readBack);
        assertEquals("File buffer data", new String(readBack, StandardCharsets.UTF_8));
    }

    // Tests emptyByteBuffer returns 0 capacity buffer
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.remaining());
    }

    // Tests getCharsetFromContentType with valid content types
    @Test
    public void testGetCharsetFromContentType_variousFormats() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset='gb2312'"));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-name-12345"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Tests mimeBoundary generates string of length 32 with valid characters
    @Test
    public void testMimeBoundary_generatesValidBoundary() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
        assertTrue(boundary.matches("^[a-zA-Z0-9_-]{32}$"));
    }

    // Tests large stream reading where fullyRead is false on first buffer pass
    @Test
    public void testParseInputStream_largeContent_readsCompletely() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p>");
        for (int i = 0; i < 2000; i++) {
            sb.append("word").append(i).append(" ");
        }
        sb.append("</p></body></html>");
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

        Document doc = DataUtil.load(new ByteArrayInputStream(data), null, "http://example.com");
        assertTrue(doc.text().contains("word1999"));
    }
}