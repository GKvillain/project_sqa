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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests null input stream returning an empty document with base URI
    @Test
    public void testParseInputStream_nullInput_returnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(0, doc.children().size());
    }

    // Tests normal parsing with explicit charset
    @Test
    public void testLoad_withExplicitCharset_parsesCorrectly() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Hello World</p>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Hello World", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests empty charset argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testLoad_emptyCharset_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.load(in, "", "http://example.com");
    }

    // Tests charset detection from HTML5 meta charset tag
    @Test
    public void testLoad_detectsCharsetFromMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>test</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("test", doc.select("p").text());
    }

    // Tests charset detection from meta http-equiv tag
    @Test
    public void testLoad_detectsCharsetFromMetaHttpEquiv() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>test</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("test", doc.select("p").text());
    }

    // Tests charset detection from XML declaration with XML parser
    @Test
    public void testLoad_detectsCharsetFromXmlDeclaration_xmlParser() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><val>café</val>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.select("val").text());
    }

    // Tests charset detection from XML declaration with HTML parser
    @Test
    public void testLoad_detectsCharsetFromXmlDeclaration_htmlParser() throws IOException {
        String xml = "<?xml encoding='ISO-8859-1'?><val>café</val>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("café", doc.select("val").text());
    }

    // Tests UTF-8 BOM detection and offset skipping
    @Test
    public void testLoad_detectsUtf8Bom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>BOM Test</p>".getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(combined), null, "http://example.com");
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("BOM Test", doc.select("p").text());
    }

    // Tests UTF-16BE BOM detection
    @Test
    public void testLoad_detectsUtf16BeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] text = "<p>UTF-16BE</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(combined), null, "http://example.com");
        assertEquals("UTF-16", doc.outputSettings().charset().name());
        assertEquals("UTF-16BE", doc.select("p").text());
    }

    // Tests UTF-16LE BOM detection
    @Test
    public void testLoad_detectsUtf16LeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] text = "<p>UTF-16LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(combined), null, "http://example.com");
        assertEquals("UTF-16", doc.outputSettings().charset().name());
        assertEquals("UTF-16LE", doc.select("p").text());
    }

    // Tests loading from File
    @Test
    public void testLoad_fromFile_parsesCorrectly() throws IOException {
        File temp = File.createTempFile("datautil", ".html");
        temp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("<title>File Test</title>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(temp, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
    }

    // Tests crossStreams copy behavior
    @Test
    public void testCrossStreams_validStreams_copiesAllData() throws IOException {
        byte[] data = "Hello Stream Copy".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    // Tests readToByteBuffer with specified maxSize
    @Test
    public void testReadToByteBuffer_withMaxSize_readsUpToLimit() throws IOException {
        byte[] data = "1234567890".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buffer.remaining());
        byte[] readBytes = new byte[5];
        buffer.get(readBytes);
        assertEquals("12345", new String(readBytes, StandardCharsets.UTF_8));
    }

    // Tests readToByteBuffer with negative maxSize throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests readToByteBuffer without maxSize (unlimited)
    @Test
    public void testReadToByteBuffer_unlimited_readsAll() throws IOException {
        byte[] data = "Full Data Stream".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertEquals(data.length, buffer.remaining());
    }

    // Tests readFileToByteBuffer reads entire file
    @Test
    public void testReadFileToByteBuffer_validFile_readsAllBytes() throws IOException {
        File temp = File.createTempFile("datautil_buf", ".tmp");
        temp.deleteOnExit();
        byte[] data = "Sample File Bytes".getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(data);
        }

        ByteBuffer buffer = DataUtil.readFileToByteBuffer(temp);
        assertEquals(data.length, buffer.remaining());
        byte[] readData = new byte[buffer.remaining()];
        buffer.get(readData);
        assertArrayEquals(data, readData);
    }

    // Tests emptyByteBuffer returns empty buffer
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.remaining());
    }

    // Tests getCharsetFromContentType with valid, invalid, and null inputs
    @Test
    public void testGetCharsetFromContentType_variousInputs_parsesCorrectly() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset='gb2312'"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; no-charset"));
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Tests mimeBoundary produces boundary of length 32
    @Test
    public void testMimeBoundary_generates32CharString() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
        assertTrue(boundary.matches("^[a-zA-Z0-9_-]+$"));
    }
}