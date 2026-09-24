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

    // Tests extracting standard charset from content type header
    @Test
    public void testGetCharsetFromContentType_standardCharset_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", charset);
    }

    // Tests extracting lowercase charset and handling case conversion
    @Test
    public void testGetCharsetFromContentType_lowerCaseCharset_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1");
        assertEquals("iso-8859-1", charset);
    }

    // Tests extracting double-quoted charset
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\"");
        assertEquals("UTF-8", charset);
    }

    // Tests handling null content type header
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests content type header without charset
    @Test
    public void testGetCharsetFromContentType_noCharsetPresent_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    // Tests unsupported charset returns null
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-name"));
    }

    // Tests illegal/invalid charset syntax in content type header
    @Test
    public void testGetCharsetFromContentType_illegalCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=$$$invalid$$$"));
    }

    // Tests readToByteBuffer with unlimited size (maxSize = 0)
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsFullStream() throws IOException {
        byte[] data = "Hello, World! This is a test.".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 0);

        assertEquals(data.length, buffer.remaining());
        assertArrayEquals(data, buffer.array());
    }

    // Tests readToByteBuffer with capped size smaller than stream
    @Test
    public void testReadToByteBuffer_cappedSize_readsUpToMaxSize() throws IOException {
        byte[] data = "Hello, World!".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        int maxSize = 5;
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, maxSize);

        assertEquals(maxSize, buffer.remaining());
        byte[] result = new byte[maxSize];
        buffer.get(result);
        assertEquals("Hello", new String(result, "UTF-8"));
    }

    // Tests readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream("test".getBytes("UTF-8"));
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests readToByteBuffer convenience method without maxSize parameter
    @Test
    public void testReadToByteBuffer_defaultOverload_readsFullStream() throws IOException {
        byte[] data = "Test content".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);

        assertEquals(data.length, buffer.remaining());
    }

    // Tests parseByteData with null charset and HTML5 meta charset
    @Test
    public void testParseByteData_nullCharsetWithMetaCharset_detectsAndReDecodes() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.select("p").text());
    }

    // Tests parseByteData with null charset and http-equiv meta tag
    @Test
    public void testParseByteData_nullCharsetWithHttpEquivMeta_detectsAndReDecodes() throws IOException {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Hello</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests parseByteData with null charset and no meta tag defaults to UTF-8
    @Test
    public void testParseByteData_nullCharsetNoMeta_defaultsToUtf8() throws IOException {
        String html = "<html><head><title>Default</title></head><body><p>Content</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes("UTF-8"));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Default", doc.title());
    }

    // Tests parseByteData with specified charset
    @Test
    public void testParseByteData_specifiedCharset_decodesCorrectly() throws IOException {
        String html = "<html><body><p>Specified</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com", Parser.htmlParser());

        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Specified", doc.select("p").text());
    }

    // Tests parseByteData stripping BOM character at start of docData
    @Test
    public void testParseByteData_withBom_stripsBom() throws IOException {
        String html = "\uFEFF<html><head><title>BOM Test</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes("UTF-8"));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());

        assertEquals("BOM Test", doc.title());
    }

    // Tests parseByteData with empty charset name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer buffer = ByteBuffer.wrap("<html></html>".getBytes());
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    // Tests load from InputStream
    @Test
    public void testLoad_inputStream_parsesDocument() throws IOException {
        String html = "<html><head><title>Stream</title></head><body></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertEquals("Stream", doc.title());
    }

    // Tests load from InputStream using XML Parser
    @Test
    public void testLoad_inputStreamWithXmlParser_parsesDocument() throws IOException {
        String xml = "<root><item>value</item></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertEquals("value", doc.select("item").text());
    }

    // Tests load from File
    @Test
    public void testLoad_file_parsesDocument() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write("<html><head><title>File Test</title></head><body></body></html>".getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
    }
}